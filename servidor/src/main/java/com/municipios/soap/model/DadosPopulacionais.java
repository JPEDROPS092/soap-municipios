package com.municipios.soap;

/**
 * Modelo de dados populacionais do município
 */
public class DadosPopulacionais {
    private int municipioId;
    private String municipioNome;
    private int populacaoTotal;
    private int populacaoHomens;
    private int populacaoMulheres;
    private int faixa0a10;
    private int faixa11a20;
    private int faixa21a30;
    private int faixa40Mais;

    public DadosPopulacionais() {
    }

    public int getMunicipioId() {
        return municipioId;
    }

    public void setMunicipioId(int municipioId) {
        this.municipioId = municipioId;
    }

    public String getMunicipioNome() {
        return municipioNome;
    }

    public void setMunicipioNome(String municipioNome) {
        this.municipioNome = municipioNome;
    }

    public int getPopulacaoTotal() {
        return populacaoTotal;
    }

    public void setPopulacaoTotal(int populacaoTotal) {
        this.populacaoTotal = populacaoTotal;
    }

    public int getPopulacaoHomens() {
        return populacaoHomens;
    }

    public void setPopulacaoHomens(int populacaoHomens) {
        this.populacaoHomens = populacaoHomens;
    }

    public int getPopulacaoMulheres() {
        return populacaoMulheres;
    }

    public void setPopulacaoMulheres(int populacaoMulheres) {
        this.populacaoMulheres = populacaoMulheres;
    }

    public int getFaixa0a10() {
        return faixa0a10;
    }

    public void setFaixa0a10(int faixa0a10) {
        this.faixa0a10 = faixa0a10;
    }

    public int getFaixa11a20() {
        return faixa11a20;
    }

    public void setFaixa11a20(int faixa11a20) {
        this.faixa11a20 = faixa11a20;
    }

    public int getFaixa21a30() {
        return faixa21a30;
    }

    public void setFaixa21a30(int faixa21a30) {
        this.faixa21a30 = faixa21a30;
    }

    public int getFaixa40Mais() {
        return faixa40Mais;
    }

    public void setFaixa40Mais(int faixa40Mais) {
        this.faixa40Mais = faixa40Mais;
    }
}
