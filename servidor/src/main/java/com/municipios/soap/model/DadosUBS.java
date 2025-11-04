package com.municipios.soap;

/**
 * Dados agregados de UBS do município
 */
public class DadosUBS {
    private int totalUbs;
    private int totalMedicos;
    private int totalEnfermeiros;
    private UBS[] listaUbs;

    public DadosUBS() {
    }

    public int getTotalUbs() {
        return totalUbs;
    }

    public void setTotalUbs(int totalUbs) {
        this.totalUbs = totalUbs;
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

    public UBS[] getListaUbs() {
        return listaUbs;
    }

    public void setListaUbs(UBS[] listaUbs) {
        this.listaUbs = listaUbs;
    }
}
