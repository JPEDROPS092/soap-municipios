package com.municipios.soap;

/**
 * Modelo de dados para Município
 */
public class Municipio {
    private int id;
    private String nome;
    private String ufSigla;
    private String ufNome;

    public Municipio() {
    }

    public Municipio(int id, String nome, String ufSigla, String ufNome) {
        this.id = id;
        this.nome = nome;
        this.ufSigla = ufSigla;
        this.ufNome = ufNome;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getUfSigla() {
        return ufSigla;
    }

    public void setUfSigla(String ufSigla) {
        this.ufSigla = ufSigla;
    }

    public String getUfNome() {
        return ufNome;
    }

    public void setUfNome(String ufNome) {
        this.ufNome = ufNome;
    }
}
