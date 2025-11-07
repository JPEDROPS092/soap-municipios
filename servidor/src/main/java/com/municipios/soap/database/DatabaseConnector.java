package com.municipios.soap.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // --- CONFIGURE OS SEUS DADOS AQUI ---

    // URL de conexão JDBC. 'soap_ubs_db' é o nome do banco de dados que criámos.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/soap_ubs_db";

    // O utilizador que criámos no MySQL
    private static final String DB_USER = "Pedro";

    // ⚠️ Altere para a senha que definiu para o 'Pedro'
    private static final String DB_PASSWORD = "admin";

    // ------------------------------------

    /**
     * Tenta estabelecer uma conexão com o banco de dados MySQL.
     * @return um objeto Connection ou null se a conexão falhar.
     */
    public static Connection connect() {
        Connection conn = null;
        try {
            // 1. Registar o driver do MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // 2. Tentar obter a conexão
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

        } catch (SQLException e) {
            System.err.println("Erro de SQL ao conectar ao MySQL: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Erro: Driver JDBC do MySQL (mysql-connector-java) não encontrado.");
            System.err.println("Verifique se a dependência está no pom.xml do servidor.");
            e.printStackTrace();
        }
        return conn;
    }
}