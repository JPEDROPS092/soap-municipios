package com.municipios.cliente;

import com.municipios.cliente.ws.*;
import com.municipios.cliente.ws.Ubs;


import java.util.List;
import java.util.Scanner;

/**
 * Cliente de Terminal para consulta de municípios e UBS via SOAP
 */
public class ClienteTerminal {

    private static final String WSDL_URL = "http://localhost:8080/municipios?wsdl";
    private static final String NAMESPACE = "http://soap.municipios.com/";
    private static final String SERVICE_NAME = "MunicipioWebServiceImplService";

    private MunicipioWebService service;
    private Scanner scanner;

    public ClienteTerminal() {
        this.scanner = new Scanner(System.in);
        conectarServico();
    }

    /**
     * Conecta ao serviço SOAP
     */
    private void conectarServico() {
        try {
            MunicipioWebServiceImplService serviceFactory = new MunicipioWebServiceImplService();
            this.service = serviceFactory.getMunicipioWebServiceImplPort();
            System.out.println("✓ Conectado ao serviço SOAP com sucesso!\n");
        } catch (Exception e) {
            System.err.println("✗ Erro ao conectar ao serviço SOAP: " + e.getMessage());
            System.err.println("\nCertifique-se de que o servidor SOAP está rodando!");
            System.err.println("Execute em outro terminal:");
            System.err.println("  cd servidor");
            System.err.println("  java -jar target/soap-servidor-1.0.0.jar");
            System.exit(1);
        }
    }

    /**
     * Limpa a tela do terminal
     */
    private void limparTela() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                System.out.print("\033[H\033[2J");
                System.out.flush();
            }
        } catch (Exception e) {
            // Se falhar, apenas imprime linhas em branco
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Exibe cabeçalho formatado
     */
    private void exibirCabecalho(String titulo) {
        System.out.println();
        System.out.println("======================================================================");
        System.out.println("  " + titulo);
        System.out.println("======================================================================");
        System.out.println();
    }

    /**
     * Tela inicial - Seleção de UF
     */
    private String telaInicial() {
        limparTela();
        exibirCabecalho("SISTEMA DE CONSULTA DE MUNICÍPIOS E UBS");

        System.out.println("Digite a sigla do Estado (UF) para consultar os municípios.");
        System.out.println("Exemplos: AM, SP, RJ, MG, BA, etc.\n");

        System.out.print("UF: ");
        String uf = scanner.nextLine().trim().toUpperCase();

        if (uf.length() != 2) {
            System.out.println("\n✗ UF inválida! Digite apenas 2 letras.");
            System.out.print("\nPressione ENTER para continuar...");
            scanner.nextLine();
            return telaInicial();
        }

        return uf;
    }

    /**
     * Tela de listagem de municípios
     */
    private Municipio telaMunicipios(String uf) {
        limparTela();
        exibirCabecalho("MUNICÍPIOS DO ESTADO: " + uf);

        System.out.println("Buscando municípios...\n");

        try {
            MunicipioArray municipioArray = service.listarMunicipiosPorUF(uf);
            List<Municipio> municipios = municipioArray.getItem();

            if (municipios == null || municipios.isEmpty()) {
                System.out.println("✗ Nenhum município encontrado ou erro na consulta.");
                System.out.print("\nPressione ENTER para voltar...");
                scanner.nextLine();
                return null;
            }

            System.out.println("Total de municípios encontrados: " + municipios.size() + "\n");

            // Exibe lista numerada
            for (int i = 0; i < municipios.size(); i++) {
                System.out.printf("%3d. %s%n", (i + 1), municipios.get(i).getNome());
            }

            System.out.println("\n----------------------------------------------------------------------");
            System.out.print("\nDigite o número do município (ou 0 para voltar): ");
            String escolha = scanner.nextLine().trim();

            try {
                int escolhaNum = Integer.parseInt(escolha);
                if (escolhaNum == 0) {
                    return null;
                }
                if (escolhaNum >= 1 && escolhaNum <= municipios.size()) {
                    return municipios.get(escolhaNum - 1);
                } else {
                    System.out.println("\n✗ Número inválido!");
                    System.out.print("\nPressione ENTER para continuar...");
                    scanner.nextLine();
                    return telaMunicipios(uf);
                }
            } catch (NumberFormatException e) {
                System.out.println("\n✗ Digite apenas números!");
                System.out.print("\nPressione ENTER para continuar...");
                scanner.nextLine();
                return telaMunicipios(uf);
            }

        } catch (Exception e) {
            System.err.println("✗ Erro ao buscar municípios: " + e.getMessage());
            System.out.print("\nPressione ENTER para voltar...");
            scanner.nextLine();
            return null;
        }
    }

    /**
     * Tela de dados populacionais do município
     */
    private void telaDadosPopulacionais(Municipio municipio) {
        limparTela();
        exibirCabecalho("DADOS POPULACIONAIS: " + municipio.getNome() + " - " + municipio.getUfSigla());

        System.out.println("Buscando dados populacionais...\n");

        try {
            DadosPopulacionais dados = service.obterDadosPopulacionais(
                    municipio.getId(),
                    municipio.getNome()
            );

            if (dados == null) {
                System.out.println("✗ Erro ao obter dados populacionais.");
                System.out.print("\nPressione ENTER para continuar...");
                scanner.nextLine();
                return;
            }

            System.out.println("Município: " + dados.getMunicipioNome());
            System.out.println("Código IBGE: " + dados.getMunicipioId());
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println("\nDADOS DEMOGRÁFICOS:");
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("  População Total:      %,12d%n", dados.getPopulacaoTotal());
            System.out.printf("  Homens:               %,12d%n", dados.getPopulacaoHomens());
            System.out.printf("  Mulheres:             %,12d%n", dados.getPopulacaoMulheres());
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println("DISTRIBUIÇÃO POR FAIXA ETÁRIA:");
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("  0 a 10 anos:          %,12d%n", dados.getFaixa0A10());
            System.out.printf("  11 a 20 anos:         %,12d%n", dados.getFaixa11A20());
            System.out.printf("  21 a 30 anos:         %,12d%n", dados.getFaixa21A30());
            System.out.printf("  40 anos ou mais:      %,12d%n", dados.getFaixa40Mais());
            System.out.println("----------------------------------------------------------------------");

            System.out.println("\n⚠ NOTA: Dados populacionais são estimativas simuladas.");
            System.out.println("   Em produção, integrar com API SIDRA/IBGE para dados oficiais.");

            System.out.print("\nPressione ENTER para continuar...");
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println("✗ Erro ao obter dados populacionais: " + e.getMessage());
            System.out.print("\nPressione ENTER para continuar...");
            scanner.nextLine();
        }
    }

    /**
     * Tela de dados de UBS do município
     */
    private void telaUBS(Municipio municipio) {
        limparTela();
        exibirCabecalho("UNIDADES BÁSICAS DE SAÚDE: " + municipio.getNome() + " - " + municipio.getUfSigla());

        System.out.println("Buscando dados de UBS...\n");

        try {
            DadosUBS dadosUBS = service.listarUBSMunicipio(
                    municipio.getId(),
                    municipio.getNome()
            );

            if (dadosUBS == null) {
                System.out.println("✗ Erro ao obter dados de UBS.");
                System.out.print("\nPressione ENTER para continuar...");
                scanner.nextLine();
                return;
            }

            // Resumo
            System.out.println("RESUMO GERAL:");
            System.out.println("----------------------------------------------------------------------");
            System.out.printf("  Total de UBS:         %12d%n", dadosUBS.getTotalUbs());
            System.out.printf("  Total de Médicos:     %12d%n", dadosUBS.getTotalMedicos());
            System.out.printf("  Total de Enfermeiros: %12d%n", dadosUBS.getTotalEnfermeiros());
            System.out.println("----------------------------------------------------------------------");

            // Lista de UBS
            System.out.println("\nLISTAGEM DE UBS:");
            System.out.println("----------------------------------------------------------------------");

            List<Ubs> listaUbs = dadosUBS.getListaUbs();
            if (listaUbs != null && !listaUbs.isEmpty()) {
                for (int i = 0; i < listaUbs.size(); i++) {
                    Ubs ubs = listaUbs.get(i);
                    System.out.println("\n" + (i + 1) + ". " + ubs.getNome());
                    System.out.println("   CNES: " + ubs.getCnes());

                    // Consulta endereço pelo CEP
                    if (ubs.getCep() != null && !ubs.getCep().equals("00000-000")) {
                        try {
                            Endereco endereco = service.consultarCEP(ubs.getCep());
                            if (endereco != null && endereco.getLogradouro() != null &&
                                    !endereco.getLogradouro().isEmpty() &&
                                    !endereco.getLogradouro().startsWith("Erro") &&
                                    !endereco.getLogradouro().equals("CEP não encontrado")) {
                                System.out.println("   Endereço: " + endereco.getLogradouro());
                                if (endereco.getBairro() != null && !endereco.getBairro().isEmpty()) {
                                    System.out.println("   Bairro: " + endereco.getBairro());
                                }
                                System.out.println("   CEP: " + endereco.getCep());
                                System.out.println("   Cidade: " + endereco.getLocalidade() + " - " + endereco.getUf());
                            } else {
                                System.out.println("   Endereço: " + ubs.getEndereco());
                                System.out.println("   CEP: " + ubs.getCep());
                            }
                        } catch (Exception e) {
                            System.out.println("   Endereço: " + ubs.getEndereco());
                            System.out.println("   CEP: " + ubs.getCep());
                        }
                    } else {
                        System.out.println("   Endereço: " + ubs.getEndereco());
                        System.out.println("   CEP: " + ubs.getCep());
                    }

                    System.out.println("   Coordenadas: Lat " + ubs.getLatitude() + ", Long " + ubs.getLongitude());
                    System.out.println("   ------------------------------------------------------------------");
                }
            } else {
                System.out.println("\nNenhuma UBS encontrada para este município.");
            }

            System.out.println("\n⚠ NOTA: Dados de UBS são simulados.");
            System.out.println("   Em produção, integrar com base de dados do CNES/DataSUS.");

            System.out.print("\nPressione ENTER para voltar ao menu...");
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println("✗ Erro ao obter dados de UBS: " + e.getMessage());
            System.out.print("\nPressione ENTER para continuar...");
            scanner.nextLine();
        }
    }

    /**
     * Menu de opções para o município selecionado
     */
    private void menuMunicipio(Municipio municipio) {
        while (true) {
            limparTela();
            exibirCabecalho("MUNICÍPIO: " + municipio.getNome() + " - " + municipio.getUfSigla());

            System.out.println("Escolha uma opção:\n");
            System.out.println("  1. Ver dados populacionais");
            System.out.println("  2. Ver Unidades Básicas de Saúde (UBS)");
            System.out.println("  0. Voltar para seleção de município\n");

            System.out.print("Opção: ");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1":
                    telaDadosPopulacionais(municipio);
                    break;
                case "2":
                    telaUBS(municipio);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("\n✗ Opção inválida!");
                    System.out.print("\nPressione ENTER para continuar...");
                    scanner.nextLine();
            }
        }
    }

    /**
     * Executa o fluxo principal da aplicação
     */
    public void executar() {
        while (true) {
            // 1. Seleciona UF
            String uf = telaInicial();

            // 2. Lista municípios e seleciona um
            Municipio municipio = telaMunicipios(uf);

            if (municipio != null) {
                // 3. Menu do município
                menuMunicipio(municipio);
            }

            // Pergunta se quer continuar
            limparTela();
            System.out.print("\nDeseja consultar outro município? (S/N): ");
            String continuar = scanner.nextLine().trim().toUpperCase();
            if (!continuar.equals("S")) {
                break;
            }
        }

        System.out.println("\n======================================================================");
        System.out.println("  Obrigado por usar o Sistema de Consulta de Municípios e UBS!");
        System.out.println("======================================================================\n");

        scanner.close();
    }

    /**
     * Método principal
     */
    public static void main(String[] args) {
        try {
            ClienteTerminal cliente = new ClienteTerminal();
            cliente.executar();
        } catch (Exception e) {
            System.err.println("\nErro inesperado: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
