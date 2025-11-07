package com.municipios.soap.server;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;

/**
 * Filtro HTTP para adicionar suporte a CORS
 */
public class CORSFilter extends Filter {

    @Override
    public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
        // Adicionar headers CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, SOAPAction");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");

        // Se for uma requisição OPTIONS (preflight), responder imediatamente
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
            return;
        }

        // Continuar com a requisição normal
        chain.doFilter(exchange);
    }

    @Override
    public String description() {
        return "CORS Filter";
    }
}
