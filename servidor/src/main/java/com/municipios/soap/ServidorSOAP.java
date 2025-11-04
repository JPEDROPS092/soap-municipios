package com.municipios.soap;

import javax.xml.ws.Endpoint;

/**
 * Classe principal para iniciar o servidor SOAP
 */
public class ServidorSOAP {

    private static final String URL = "http://localhost:8080/municipios";

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  SERVIDOR SOAP - MUNICÍPIOS E UBS");
        System.out.println("======================================================================");
        System.out.println();
        System.out.println("Iniciando servidor SOAP...");
        System.out.println();

        try {
            Endpoint endpoint = Endpoint.publish(URL, new MunicipioWebServiceImpl());

            System.out.println("✓ Servidor SOAP iniciado com sucesso!");
            System.out.println();
            System.out.println("URL do serviço: " + URL);
            System.out.println("WSDL disponível em: " + URL + "?wsdl");
            System.out.println();
            System.out.println("Pressione Ctrl+C para parar o servidor");
            System.out.println("======================================================================");
            System.out.println();

            // Mantém o servidor rodando
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("✗ Erro ao iniciar servidor SOAP: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
