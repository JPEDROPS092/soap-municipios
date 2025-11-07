package com.municipios.soap.server;

import com.municipios.soap.service.MunicipioWebServiceImpl;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.Endpoint;

/**
 * Classe principal para inicializar o servidor SOAP
 */
public class ServidorSOAP {

    private static final String URL = "http://0.0.0.0:8080/ws/municipios";

    public static void main(String[] args) {
        System.out.println("======================================================================");
        System.out.println("  SERVIDOR SOAP - MUNICÍPIOS E UBS");
        System.out.println("======================================================================");
        System.out.println();
        System.out.println("Inicializando servidor SOAP...");
        System.out.println("Endpoint: " + URL);
        System.out.println();

        try {
            // Publicar o Web Service
            Endpoint endpoint = Endpoint.publish(URL, new MunicipioWebServiceImpl());

            // Adicionar filtro CORS
            HttpContext context = (HttpContext) endpoint.getProperties().get("com.sun.xml.ws.http.HttpContext");
            if (context != null) {
                HttpServer server = context.getServer();
                context.getFilters().add(new CORSFilter());
                System.out.println("✓ Filtro CORS configurado!");
            }

            System.out.println("✓ Servidor iniciado com sucesso!");
            System.out.println();
            System.out.println("WSDL disponível em: " + URL + "?wsdl");
            System.out.println();
            System.out.println("======================================================================");
            System.out.println("Servidor em execução. Pressione Ctrl+C para parar.");
            System.out.println("======================================================================");
            System.out.println();

            // Manter o servidor rodando
            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("✗ Erro ao iniciar o servidor SOAP:");
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
