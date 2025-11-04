package com.municipios.cliente;

import com.municipios.cliente.ws.*;
import com.municipios.cliente.ws.Ubs;

import java.util.List;
import java.util.Scanner;

/**
 * Cliente de Terminal para consulta de municípios e UBS via SOAP
 * Versão Melhorada com melhor estética e experiência do usuário
 */
public class ClienteTerminal {

    private static final String WSDL_URL = "http://localhost:8080/municipios?wsdl";
    private static final String NAMESPACE = "http://soap.municipios.com/";
    private static final String SERVICE_NAME = "MunicipioWebServiceImplService";

    // Cores ANSI para terminal
    private static final String RESET = "\u001B[0m";
    private static final String BOLD = "\u001B[1m";
    private static final String GREEN = "\u001B[32m";
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String WHITE_BRIGHT = "\u001B[97m";
    private static final String BG_BLUE = "\u001B[44m";

    // Símbolos Unicode
    private static final String CHECK = "✓";
    private static final String CROSS = "✗";
    private static final String ARROW = "→";
    private static final String BULLET = "•";
    private static final String STAR = "★";
    private static final String INFO = "ℹ";
    private static final String WARNING = "⚠";

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
            printColoredBox("CONECTANDO AO SERVIDOR SOAP", CYAN);
            Thread.sleep(500);

            MunicipioWebServiceImplService serviceFactory = new MunicipioWebServiceImplService();
            this.service = serviceFactory.getMunicipioWebServiceImplPort();

            System.out.println(GREEN + BOLD + "\n  " + CHECK + " Conectado ao serviço SOAP com sucesso!" + RESET);
            Thread.sleep(800);

        } catch (Exception e) {
            System.err.println(RED + BOLD + "\n  " + CROSS + " Erro ao conectar ao serviço SOAP" + RESET);
            System.err.println(YELLOW + "\n  " + WARNING + " Certifique-se de que o servidor SOAP está rodando!" + RESET);
            System.err.println("\n  Execute em outro terminal:");
            System.err.println(CYAN + "    cd servidor" + RESET);
            System.err.println(CYAN + "    java -jar target/soap-servidor-1.0.0.jar" + RESET);
            System.exit(1);
        }
    }

    /**
     * Imprime uma caixa colorida com título
     */
    private void printColoredBox(String titulo, String cor) {
        int largura = 70;
        int padding = (largura - titulo.length() - 2) / 2;

        System.out.println("\n" + cor + BOLD + "╔" + "═".repeat(largura) + "╗" + RESET);
        System.out.println(cor + BOLD + "║" + " ".repeat(padding) + titulo + " ".repeat(largura - padding - titulo.length()) + "║" + RESET);
        System.out.println(cor + BOLD + "╚" + "═".repeat(largura) + "╝" + RESET);
    }

    /**
     * Imprime separador estilizado
     */
    private void printSeparator() {
        System.out.println(BLUE + "─".repeat(72) + RESET);
    }

    /**
     * Imprime separador duplo
     */
    private void printDoubleSeparator() {
        System.out.println(CYAN + BOLD + "═".repeat(72) + RESET);
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
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    /**
     * Exibe cabeçalho formatado melhorado
     */
    private void exibirCabecalho(String titulo) {
        System.out.println();
        printColoredBox(titulo, CYAN);
        System.out.println();
    }

    /**
     * Aguarda com animação
     */
    private void aguardarComAnimacao(String mensagem) {
        try {
            System.out.print(YELLOW + "  " + mensagem);
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
            System.out.println(RESET);
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Tela inicial - Seleção de UF
     */
    private String telaInicial() {
        limparTela();

        // Banner ASCII art
        System.out.println(CYAN + BOLD);
        System.out.println("  ███████╗██╗███████╗████████╗███████╗███╗   ███╗ █████╗     ██╗   ██╗██████╗ ███████╗");
        System.out.println("  ██╔════╝██║██╔════╝╚══██╔══╝██╔════╝████╗ ████║██╔══██╗    ██║   ██║██╔══██╗██╔════╝");
        System.out.println("  ███████╗██║███████╗   ██║   █████╗  ██╔████╔██║███████║    ██║   ██║██████╔╝███████╗");
        System.out.println("  ╚════██║██║╚════██║   ██║   ██╔══╝  ██║╚██╔╝██║██╔══██║    ██║   ██║██╔══██╗╚════██║");
        System.out.println("  ███████║██║███████║   ██║   ███████╗██║ ╚═╝ ██║██║  ██║    ╚██████╔╝██████╔╝███████║");
        System.out.println("  ╚══════╝╚═╝╚══════╝   ╚═╝   ╚══════╝╚═╝     ╚═╝╚═╝  ╚═╝     ╚═════╝ ╚═════╝ ╚══════╝");
        System.out.println(RESET);

        System.out.println(WHITE_BRIGHT + BOLD + "            Sistema de Consulta de Municípios e Unidades de Saúde" + RESET);
        printDoubleSeparator();

        System.out.println(YELLOW + "\n  " + INFO + " Digite a sigla do Estado (UF) para consultar os municípios" + RESET);
        System.out.println(BLUE + "  " + BULLET + " Exemplos: " + WHITE_BRIGHT + "AM, SP, RJ, MG, BA, RS, PR, SC" + RESET);

        printSeparator();
        System.out.print(GREEN + BOLD + "\n  " + ARROW + " UF: " + RESET);
        String uf = scanner.nextLine().trim().toUpperCase();

        if (uf.length() != 2) {
            System.out.println(RED + "\n  " + CROSS + " UF inválida! Digite apenas 2 letras." + RESET);
            System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
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

        aguardarComAnimacao("Buscando municípios");

        try {
            MunicipioArray municipioArray = service.listarMunicipiosPorUF(uf);
            List<Municipio> municipios = municipioArray.getItem();

            if (municipios == null || municipios.isEmpty()) {
                System.out.println(RED + "  " + CROSS + " Nenhum município encontrado ou erro na consulta." + RESET);
                System.out.print(YELLOW + "\n  Pressione ENTER para voltar..." + RESET);
                scanner.nextLine();
                return null;
            }

            System.out.println(GREEN + BOLD + "  " + CHECK + " Total de municípios encontrados: " +
                    WHITE_BRIGHT + municipios.size() + RESET + "\n");
            printSeparator();

            // Exibe lista numerada com cores alternadas
            for (int i = 0; i < municipios.size(); i++) {
                String cor = (i % 2 == 0) ? CYAN : BLUE;
                System.out.printf(cor + "  %3d" + RESET + " " + BULLET + " " +
                                WHITE_BRIGHT + "%s" + RESET + "%n",
                        (i + 1), municipios.get(i).getNome());
            }

            printDoubleSeparator();
            System.out.print(GREEN + BOLD + "\n  " + ARROW + " Digite o número do município " +
                    YELLOW + "(ou 0 para voltar)" + GREEN + ": " + RESET);
            String escolha = scanner.nextLine().trim();

            try {
                int escolhaNum = Integer.parseInt(escolha);
                if (escolhaNum == 0) {
                    return null;
                }
                if (escolhaNum >= 1 && escolhaNum <= municipios.size()) {
                    return municipios.get(escolhaNum - 1);
                } else {
                    System.out.println(RED + "\n  " + CROSS + " Número inválido!" + RESET);
                    System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
                    scanner.nextLine();
                    return telaMunicipios(uf);
                }
            } catch (NumberFormatException e) {
                System.out.println(RED + "\n  " + CROSS + " Digite apenas números!" + RESET);
                System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
                scanner.nextLine();
                return telaMunicipios(uf);
            }

        } catch (Exception e) {
            System.err.println(RED + "  " + CROSS + " Erro ao buscar municípios: " + e.getMessage() + RESET);
            System.out.print(YELLOW + "\n  Pressione ENTER para voltar..." + RESET);
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

        aguardarComAnimacao("Buscando dados populacionais");

        try {
            DadosPopulacionais dados = service.obterDadosPopulacionais(
                    municipio.getId(),
                    municipio.getNome()
            );

            if (dados == null) {
                System.out.println(RED + "  " + CROSS + " Erro ao obter dados populacionais." + RESET);
                System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
                scanner.nextLine();
                return;
            }

            System.out.println(CYAN + BOLD + "  " + INFO + " Município: " + WHITE_BRIGHT + dados.getMunicipioNome() + RESET);
            System.out.println(CYAN + "  " + INFO + " Código IBGE: " + WHITE_BRIGHT + dados.getMunicipioId() + RESET);

            printDoubleSeparator();
            System.out.println(MAGENTA + BOLD + "\n  " + STAR + " DADOS DEMOGRÁFICOS" + RESET);
            printSeparator();

            System.out.printf(CYAN + "  %-25s" + RESET + WHITE_BRIGHT + BOLD + "%,15d" + RESET + "\n",
                    "População Total:", dados.getPopulacaoTotal());
            System.out.printf(BLUE + "  %-25s" + RESET + WHITE_BRIGHT + "%,15d" + RESET + "\n",
                    "  " + BULLET + " Homens:", dados.getPopulacaoHomens());
            System.out.printf(BLUE + "  %-25s" + RESET + WHITE_BRIGHT + "%,15d" + RESET + "\n",
                    "  " + BULLET + " Mulheres:", dados.getPopulacaoMulheres());

            printSeparator();
            System.out.println(MAGENTA + BOLD + "\n  " + STAR + " DISTRIBUIÇÃO POR FAIXA ETÁRIA" + RESET);
            printSeparator();

            System.out.printf(GREEN + "  %-25s" + RESET + WHITE_BRIGHT + "%,15d" + RESET + "\n",
                    "  0 a 10 anos:", dados.getFaixa0A10());
            System.out.printf(GREEN + "  %-25s" + RESET + WHITE_BRIGHT + "%,15d" + RESET + "\n",
                    "  11 a 20 anos:", dados.getFaixa11A20());
            System.out.printf(GREEN + "  %-25s" + RESET + WHITE_BRIGHT + "%,15d" + RESET + "\n",
                    "  21 a 30 anos:", dados.getFaixa21A30());
            System.out.printf(GREEN + "  %-25s" + RESET + WHITE_BRIGHT + "%,15d" + RESET + "\n",
                    "  40 anos ou mais:", dados.getFaixa40Mais());

            printDoubleSeparator();
            System.out.println(YELLOW + "\n  " + WARNING + " NOTA: Dados do Censo 2022 (IBGE)" + RESET);

            System.out.print(CYAN + "\n  Pressione ENTER para continuar..." + RESET);
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println(RED + "  " + CROSS + " Erro ao obter dados populacionais: " + e.getMessage() + RESET);
            System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
            scanner.nextLine();
        }
    }

    /**
     * Tela de dados de UBS do município
     */
    private void telaUBS(Municipio municipio) {
        limparTela();
        exibirCabecalho("UNIDADES BÁSICAS DE SAÚDE: " + municipio.getNome() + " - " + municipio.getUfSigla());

        aguardarComAnimacao("Buscando dados de UBS");

        try {
            DadosUBS dadosUBS = service.listarUBSMunicipio(
                    municipio.getId(),
                    municipio.getNome()
            );

            if (dadosUBS == null) {
                System.out.println(RED + "  " + CROSS + " Erro ao obter dados de UBS." + RESET);
                System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
                scanner.nextLine();
                return;
            }

            // Resumo com destaque
            System.out.println(MAGENTA + BOLD + "  " + STAR + " RESUMO GERAL" + RESET);
            printSeparator();
            System.out.printf(CYAN + "  %-30s" + RESET + WHITE_BRIGHT + BOLD + "%8d" + RESET + "\n",
                    "Total de UBS:", dadosUBS.getTotalUbs());
            System.out.printf(GREEN + "  %-30s" + RESET + WHITE_BRIGHT + BOLD + "%8d" + RESET + "\n",
                    "Total de Médicos:", dadosUBS.getTotalMedicos());
            System.out.printf(BLUE + "  %-30s" + RESET + WHITE_BRIGHT + BOLD + "%8d" + RESET + "\n",
                    "Total de Enfermeiros:", dadosUBS.getTotalEnfermeiros());

            printDoubleSeparator();

            // Lista de UBS
            List<Ubs> listaUbs = dadosUBS.getListaUbs();
            if (listaUbs != null && !listaUbs.isEmpty()) {
                System.out.println(MAGENTA + BOLD + "\n  " + STAR + " LISTAGEM COMPLETA DE UBS" + RESET + "\n");

                for (int i = 0; i < listaUbs.size(); i++) {
                    Ubs ubs = listaUbs.get(i);

                    printSeparator();
                    System.out.println(CYAN + BOLD + "\n  [" + (i + 1) + "] " + WHITE_BRIGHT + ubs.getNome() + RESET);
                    System.out.println(BLUE + "      " + BULLET + " CNES: " + RESET + ubs.getCnes());

                    // Consulta endereço pelo CEP
                    if (ubs.getCep() != null && !ubs.getCep().equals("00000-000")) {
                        try {
                            Endereco endereco = service.consultarCEP(ubs.getCep());
                            if (endereco != null && endereco.getLogradouro() != null &&
                                    !endereco.getLogradouro().isEmpty() &&
                                    !endereco.getLogradouro().startsWith("Erro") &&
                                    !endereco.getLogradouro().equals("CEP não encontrado")) {
                                System.out.println(GREEN + "      " + BULLET + " Endereço: " + RESET + endereco.getLogradouro());
                                if (endereco.getBairro() != null && !endereco.getBairro().isEmpty()) {
                                    System.out.println(GREEN + "      " + BULLET + " Bairro: " + RESET + endereco.getBairro());
                                }
                                System.out.println(GREEN + "      " + BULLET + " CEP: " + RESET + endereco.getCep());
                                System.out.println(GREEN + "      " + BULLET + " Cidade: " + RESET +
                                        endereco.getLocalidade() + " - " + endereco.getUf());
                            } else {
                                System.out.println(GREEN + "      " + BULLET + " Endereço: " + RESET + ubs.getEndereco());
                                System.out.println(GREEN + "      " + BULLET + " CEP: " + RESET + ubs.getCep());
                            }
                        } catch (Exception e) {
                            System.out.println(GREEN + "      " + BULLET + " Endereço: " + RESET + ubs.getEndereco());
                            System.out.println(GREEN + "      " + BULLET + " CEP: " + RESET + ubs.getCep());
                        }
                    } else {
                        System.out.println(GREEN + "      " + BULLET + " Endereço: " + RESET + ubs.getEndereco());
                        System.out.println(GREEN + "      " + BULLET + " CEP: " + RESET + ubs.getCep());
                    }

                    System.out.println(YELLOW + "      " + BULLET + " Coordenadas: " + RESET +
                            "Lat " + ubs.getLatitude() + ", Long " + ubs.getLongitude());
                }

                printDoubleSeparator();
            } else {
                System.out.println(YELLOW + "\n  " + WARNING + " Nenhuma UBS encontrada para este município." + RESET);
            }

            System.out.println(YELLOW + "\n  " + WARNING + " NOTA: Dados do CNES (Cadastro Nacional de Estabelecimentos de Saúde)" + RESET);

            System.out.print(CYAN + "\n  Pressione ENTER para voltar ao menu..." + RESET);
            scanner.nextLine();

        } catch (Exception e) {
            System.err.println(RED + "  " + CROSS + " Erro ao obter dados de UBS: " + e.getMessage() + RESET);
            System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
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

            System.out.println(WHITE_BRIGHT + BOLD + "  Escolha uma opção:\n" + RESET);
            System.out.println(CYAN + "  [1]" + RESET + " " + BULLET + " Ver dados populacionais");
            System.out.println(GREEN + "  [2]" + RESET + " " + BULLET + " Ver Unidades Básicas de Saúde (UBS)");
            System.out.println(RED + "  [0]" + RESET + " " + BULLET + " Voltar para seleção de município\n");

            printSeparator();
            System.out.print(YELLOW + BOLD + "\n  " + ARROW + " Opção: " + RESET);
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
                    System.out.println(RED + "\n  " + CROSS + " Opção inválida!" + RESET);
                    System.out.print(YELLOW + "\n  Pressione ENTER para continuar..." + RESET);
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
            printColoredBox("CONSULTAR OUTRO MUNICÍPIO?", YELLOW);
            System.out.print(CYAN + "\n  " + ARROW + " Deseja consultar outro município? " +
                    WHITE_BRIGHT + "(S/N): " + RESET);
            String continuar = scanner.nextLine().trim().toUpperCase();
            if (!continuar.equals("S")) {
                break;
            }
        }

        limparTela();
        System.out.println(GREEN + BOLD);
        System.out.println("\n  ╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("  ║                                                                    ║");
        System.out.println("  ║    " + CHECK + " Obrigado por usar o Sistema de Consulta de UBS!        ║");
        System.out.println("  ║                                                                    ║");
        System.out.println("  ╚════════════════════════════════════════════════════════════════════╝");
        System.out.println(RESET);

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
            System.err.println(RED + BOLD + "\n  " + CROSS + " Erro inesperado: " + e.getMessage() + RESET);
            e.printStackTrace();
            System.exit(1);
        }
    }
}