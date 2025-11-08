package com.municipios.soap.model;

import java.util.List;

/**
 * Modelo para retornar estabelecimentos separados por tipo
 */
public class EstabelecimentosPorTipo {
    
    private List<UBS> estabelecimentosUBS;
    private List<UBS> estabelecimentosOutros;
    private int totalUBS;
    private int totalOutros;
    private int totalGeral;
    private int totalMedicos;
    private int totalEnfermeiros;
    
    public EstabelecimentosPorTipo() {
    }

    public List<UBS> getEstabelecimentosUBS() {
        return estabelecimentosUBS;
    }

    public void setEstabelecimentosUBS(List<UBS> estabelecimentosUBS) {
        this.estabelecimentosUBS = estabelecimentosUBS;
    }

    public List<UBS> getEstabelecimentosOutros() {
        return estabelecimentosOutros;
    }

    public void setEstabelecimentosOutros(List<UBS> estabelecimentosOutros) {
        this.estabelecimentosOutros = estabelecimentosOutros;
    }

    public int getTotalUBS() {
        return totalUBS;
    }

    public void setTotalUBS(int totalUBS) {
        this.totalUBS = totalUBS;
    }

    public int getTotalOutros() {
        return totalOutros;
    }

    public void setTotalOutros(int totalOutros) {
        this.totalOutros = totalOutros;
    }

    public int getTotalGeral() {
        return totalGeral;
    }

    public void setTotalGeral(int totalGeral) {
        this.totalGeral = totalGeral;
    }

    public int getTotalMedicos() {
        return totalMedicos;
    }

    public void setTotalMedicos(int totalMedicos) {
        this.totalMedicos = totalMedicos;
    }

    public int getTotalEnfermeiros() {
        return totalEnfermeiros;
    }

    public void setTotalEnfermeiros(int totalEnfermeiros) {
        this.totalEnfermeiros = totalEnfermeiros;
    }
}
