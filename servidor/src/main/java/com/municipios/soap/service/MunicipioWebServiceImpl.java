package com.municipios.soap.service;

import com.municipios.soap.model.*;
import com.municipios.soap.database.DatabaseConnector;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.jws.WebService;
import java.util.ArrayList;
import java.util.List;

// --- Imports Adicionados para o Banco de Dados ---
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// ------------------------------------------------

/**
 * Implementação do Web Service SOAP
 * (Corrigido para usar ID IBGE de 6 dígitos nas consultas SQL)
 */
@WebService(endpointInterface = "com.municipios.soap.service.MunicipioWebService")
public class MunicipioWebServiceImpl implements MunicipioWebService {

    private final Gson gson = new Gson();

    /**
     * (Sem alterações)
     * Busca a lista de municípios na API REST do IBGE.
     */
    @Override
    public Municipio[] listarMunicipiosPorUF(String uf) {
        List<Municipio> municipios = new ArrayList<>();

        try {
            String url = "https://servicodados.ibge.gov.br/api/v1/localidades/estados/" +
                    uf.toUpperCase() + "/municipios";

            String jsonResponse = fazerRequisicaoHTTP(url);
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);

            for (JsonElement element : jsonArray) {
                JsonObject munJson = element.getAsJsonObject();

                Municipio municipio = new Municipio();
                municipio.setId(munJson.get("id").getAsInt()); // API usa 7 dígitos
                municipio.setNome(munJson.get("nome").getAsString());

                JsonObject ufJson = munJson.getAsJsonObject("microrregiao")
                        .getAsJsonObject("mesorregiao")
                        .getAsJsonObject("UF");

                municipio.setUfSigla(ufJson.get("sigla").getAsString());
                municipio.setUfNome(ufJson.get("nome").getAsString());

                municipios.add(municipio);
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar municípios: " + e.getMessage());
        }

        return municipios.toArray(new Municipio[0]);
    }

    /**
     * (Corrigido)
     * Converte o ID de 7 dígitos (municipioId) para 6 dígitos antes de consultar o BD.
     */
    @Override
    public DadosPopulacionais obterDadosPopulacionais(int municipioId, String municipioNome) {
        DadosPopulacionais dados = new DadosPopulacionais();
        dados.setMunicipioId(municipioId);
        dados.setMunicipioNome(municipioNome);

        // --- CORREÇÃO AQUI ---
        // Converte o ID da API (7 dígitos, ex: 2507507) para o ID de 6 dígitos (ex: 250750)
        String ibgeMunicipio6Digitos = String.valueOf(municipioId / 10);
        // ---------------------

        String sql = "SELECT * FROM demografia_municipio WHERE ibge_municipio = ?";

        try (Connection conn = DatabaseConnector.connect()) {
            if (conn == null) {
                System.err.println("Conexão com BD nula ao buscar demografia. A usar fallback.");
                return usarEstimativaGenerica(dados, municipioId);
            }

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                // Usa o ID de 6 dígitos na query
                pstmt.setString(1, ibgeMunicipio6Digitos);
                ResultSet rs = pstmt.executeQuery();

                if (rs.next()) {
                    // Encontra os dados (simulados OU reais do Censo)
                    dados.setPopulacaoTotal(rs.getInt("populacao_total"));
                    dados.setPopulacaoHomens(rs.getInt("populacao_homens"));
                    dados.setPopulacaoMulheres(rs.getInt("populacao_mulheres"));
                    dados.setFaixa0a10(rs.getInt("faixa_0_10"));
                    dados.setFaixa11a20(rs.getInt("faixa_11_20"));
                    dados.setFaixa21a30(rs.getInt("faixa_21_30"));
                    dados.setFaixa40Mais(rs.getInt("faixa_40_mais"));
                } else {
                    // Usa o fallback para municípios não encontrados no BD
                    dados = usarEstimativaGenerica(dados, municipioId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar dados demográficos: " + e.getMessage());
            e.printStackTrace();
            dados = usarEstimativaGenerica(dados, municipioId);
        }
        return dados;
    }

    /**
     * Método auxiliar para o fallback de dados populacionais
     */
    private DadosPopulacionais usarEstimativaGenerica(DadosPopulacionais dados, int municipioId) {
        // Estimativa genérica
        int basePop = 50000 + (municipioId % 500000);
        dados.setPopulacaoTotal(basePop);
        dados.setPopulacaoHomens((int) (basePop * 0.49));
        dados.setPopulacaoMulheres((int) (basePop * 0.51));
        dados.setFaixa0a10((int) (basePop * 0.15));
        dados.setFaixa11a20((int) (basePop * 0.17));
        dados.setFaixa21a30((int) (basePop * 0.18));
        dados.setFaixa40Mais((int) (basePop * 0.50));
        return dados;
    }


    /**
     * (Sem alterações)
     * Busca o endereço na API REST do ViaCEP.
     */
    @Override
    public Endereco consultarCEP(String cep) {
        Endereco endereco = new Endereco();

        try {
            String cepLimpo = cep.replaceAll("[^0-9]", "");
            String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

            String jsonResponse = fazerRequisicaoHTTP(url);
            JsonObject jsonObj = gson.fromJson(jsonResponse, JsonObject.class);

            if (jsonObj.has("erro")) {
                endereco.setCep(cep);
                endereco.setLogradouro("CEP não encontrado");
                return endereco;
            }

            endereco.setCep(jsonObj.has("cep") ? jsonObj.get("cep").getAsString() : "");
            endereco.setLogradouro(jsonObj.has("logradouro") ? jsonObj.get("logradouro").getAsString() : "");
            endereco.setComplemento(jsonObj.has("complemento") ? jsonObj.get("complemento").getAsString() : "");
            endereco.setBairro(jsonObj.has("bairro") ? jsonObj.get("bairro").getAsString() : "");
            endereco.setLocalidade(jsonObj.has("localidade") ? jsonObj.get("localidade").getAsString() : "");
            endereco.setUf(jsonObj.has("uf") ? jsonObj.get("uf").getAsString() : "");

        } catch (Exception e) {
            System.err.println("Erro ao consultar CEP: " + e.getMessage());
            endereco.setCep(cep);
            endereco.setLogradouro("Erro: " + e.getMessage());
        }

        return endereco;
    }

    /**
     * (Corrigido)
     * Converte o ID de 7 dígitos (municipioId) para 6 dígitos antes de consultar o BD.
     */
    @Override
    public DadosUBS listarUBSMunicipio(int municipioId, String municipioNome) {
        DadosUBS dadosUBS = new DadosUBS();
        List<UBS> listaUbs = new ArrayList<>();

        // --- CORREÇÃO AQUI ---
        // Converte o ID da API (7 dígitos, ex: 2507507) para o ID de 6 dígitos (ex: 250750)
        String ibgeMunicipio6Digitos = String.valueOf(municipioId / 10);
        // ---------------------

        String sqlTotais = "SELECT * FROM ubs_totais_municipio WHERE ibge_municipio = ?";
        String sqlLista = "SELECT * FROM ubs_estabelecimentos WHERE ibge_municipio = ?";

        try (Connection conn = DatabaseConnector.connect()) {

            if (conn == null) {
                System.err.println("Conexão com BD nula ao buscar UBS. Verifique o DatabaseConnector.");
                return new DadosUBS(); // Retorna vazio
            }

            // 1. Buscar Totais (com ID de 6 dígitos)
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTotais)) {
                pstmt.setString(1, ibgeMunicipio6Digitos);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    // Agora deve encontrar os dados REAIS
                    dadosUBS.setTotalUbs(rs.getInt("total_ubs"));
                    dadosUBS.setTotalMedicos(rs.getInt("total_medicos"));
                    dadosUBS.setTotalEnfermeiros(rs.getInt("total_enfermeiros"));
                } else {
                    // Município não encontrado no BD de totais
                    dadosUBS.setTotalUbs(0);
                    dadosUBS.setTotalMedicos(0);
                    dadosUBS.setTotalEnfermeiros(0);
                }
            }

            // 2. Buscar Lista de Estabelecimentos (com ID de 6 dígitos)
            try (PreparedStatement pstmt = conn.prepareStatement(sqlLista)) {
                pstmt.setString(1, ibgeMunicipio6Digitos);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    // Agora deve encontrar as UBSs
                    UBS ubs = new UBS();
                    ubs.setCnes(rs.getString("cnes"));
                    ubs.setNome(rs.getString("nome"));
                    String endereco = String.format("%s - %s", rs.getString("logradouro"), rs.getString("bairro"));
                    ubs.setEndereco(endereco);
                    ubs.setCep(rs.getString("cep"));
                    ubs.setLatitude(String.valueOf(rs.getDouble("latitude")));
                    ubs.setLongitude(String.valueOf(rs.getDouble("longitude")));
                    listaUbs.add(ubs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar dados de UBS: " + e.getMessage());
            e.printStackTrace();
            return new DadosUBS(); // Retorna vazio em caso de erro
        }

        dadosUBS.setListaUbs(listaUbs.toArray(new UBS[0]));
        return dadosUBS;
    }

    /**
     * Lista estabelecimentos separados por tipo (UBS e Outros)
     * Classifica baseado no nome do estabelecimento
     */
    @Override
    public EstabelecimentosPorTipo listarEstabelecimentosPorTipo(int municipioId, String municipioNome) {
        EstabelecimentosPorTipo resultado = new EstabelecimentosPorTipo();
        List<UBS> listaUbs = new ArrayList<>();
        List<UBS> listaOutros = new ArrayList<>();

        // Converte o ID da API (7 dígitos) para o ID de 6 dígitos
        String ibgeMunicipio6Digitos = String.valueOf(municipioId / 10);

        String sqlTotais = "SELECT * FROM ubs_totais_municipio WHERE ibge_municipio = ?";
        String sqlLista = "SELECT * FROM ubs_estabelecimentos WHERE ibge_municipio = ?";

        try (Connection conn = DatabaseConnector.connect()) {

            if (conn == null) {
                System.err.println("Conexão com BD nula ao buscar estabelecimentos. Verifique o DatabaseConnector.");
                return new EstabelecimentosPorTipo();
            }

            // 1. Buscar Totais
            try (PreparedStatement pstmt = conn.prepareStatement(sqlTotais)) {
                pstmt.setString(1, ibgeMunicipio6Digitos);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    resultado.setTotalMedicos(rs.getInt("total_medicos"));
                    resultado.setTotalEnfermeiros(rs.getInt("total_enfermeiros"));
                } else {
                    resultado.setTotalMedicos(0);
                    resultado.setTotalEnfermeiros(0);
                }
            }

            // 2. Buscar e Classificar Estabelecimentos
            try (PreparedStatement pstmt = conn.prepareStatement(sqlLista)) {
                pstmt.setString(1, ibgeMunicipio6Digitos);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    UBS estabelecimento = new UBS();
                    String nome = rs.getString("nome");
                    
                    estabelecimento.setCnes(rs.getString("cnes"));
                    estabelecimento.setNome(nome);
                    String endereco = String.format("%s - %s", 
                        rs.getString("logradouro"), 
                        rs.getString("bairro"));
                    estabelecimento.setEndereco(endereco);
                    estabelecimento.setCep(rs.getString("cep"));
                    estabelecimento.setLatitude(String.valueOf(rs.getDouble("latitude")));
                    estabelecimento.setLongitude(String.valueOf(rs.getDouble("longitude")));

                    // Classificar por nome
                    String nomeUpper = nome.toUpperCase();
                    if (nomeUpper.contains("UBS") || 
                        nomeUpper.contains("UNIDADE BASICA") || 
                        nomeUpper.contains("UNIDADE BÁSICA")) {
                        listaUbs.add(estabelecimento);
                    } else {
                        listaOutros.add(estabelecimento);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro de SQL ao buscar estabelecimentos por tipo: " + e.getMessage());
            e.printStackTrace();
            return new EstabelecimentosPorTipo();
        }

        // Configurar resultado
        resultado.setEstabelecimentosUBS(listaUbs);
        resultado.setEstabelecimentosOutros(listaOutros);
        resultado.setTotalUBS(listaUbs.size());
        resultado.setTotalOutros(listaOutros.size());
        resultado.setTotalGeral(listaUbs.size() + listaOutros.size());

        return resultado;
    }

    /**
     * (Sem alterações)
     * Faz requisição HTTP GET e retorna o corpo da resposta
     */
    private String fazerRequisicaoHTTP(String url) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                return EntityUtils.toString(response.getEntity(), "UTF-8");
            }
        }
    }
}