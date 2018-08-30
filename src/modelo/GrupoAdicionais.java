/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import java.util.ArrayList;
import java.util.List;

/**

 @author jvbor
 */
public class GrupoAdicionais {

    private String nomeGrupo, descricaoGrupo;
    private List<AdicionalProduto> adicionais;
    private int qtdMin, qtdMax;

    public GrupoAdicionais() {
        this.nomeGrupo = "";
        this.descricaoGrupo = "";
        this.adicionais = new ArrayList<>();
        this.qtdMin = 0;
        this.qtdMax = 0;
    }

    public int getQtdMin() {
        return qtdMin;
    }

    public void setQtdMin(int qtdMin) {
        this.qtdMin = qtdMin;
    }

    public int getQtdMax() {
        return qtdMax;
    }

    public void setQtdMax(int qtdMax) {
        this.qtdMax = qtdMax;
    }

    public String getDescricaoGrupo() {
        return descricaoGrupo;
    }

    public void setDescricaoGrupo(String descricaoGrupo) {
        this.descricaoGrupo = descricaoGrupo;
    }

    public String getNomeGrupo() {
        return nomeGrupo;
    }

    public void setNomeGrupo(String nomeGrupo) {
        this.nomeGrupo = nomeGrupo;
    }

    public List<AdicionalProduto> getAdicionais() {
        return adicionais;
    }

    public void setAdicionais(List<AdicionalProduto> adicionais) {
        this.adicionais = adicionais;
    }

}
