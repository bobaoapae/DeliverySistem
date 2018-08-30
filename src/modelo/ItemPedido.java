/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import java.util.ArrayList;
import java.util.Iterator;

/**

 @author jvbor
 */
public class ItemPedido implements Comparable<ItemPedido> {

    private Produto p;
    private int qtd;
    private String comentario;
    private ArrayList<AdicionalProduto> adicionais;

    public ItemPedido() {
        adicionais = new ArrayList<>();
        comentario = "";
        qtd = 1;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }

    public Produto getP() {
        return p;
    }

    public void setP(Produto p) {
        this.p = p;
    }

    public String getComentario() {
        if (comentario == null) {
            comentario = "";
        }
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public double getSubTotal() {
        if (p == null) {
            return 0;
        }
        double retorno = p.getValor() * qtd;
        if (this.getP() instanceof Pizza) {
            for (AdicionalProduto ad : getAdicionais(SaborPizza.class)) {
                if (ad.getNome().toLowerCase().contains("(especial)")) {
                    retorno = ((Pizza) this.getP()).getValorEspecial() * qtd;
                    break;
                }
            }
        }
        for (AdicionalProduto ad : adicionais) {
            if ((ad instanceof SaborPizza)) {
                continue;
            }
            retorno += ad.getValor() * qtd;
        }
        return retorno;
    }

    public ArrayList<AdicionalProduto> getAdicionais() {
        return adicionais;
    }

    public ArrayList<AdicionalProduto> getAdicionais(Class t) {
        ArrayList<AdicionalProduto> temp = new ArrayList<>();
        Iterator<AdicionalProduto> it = getAdicionais().stream().filter((o) -> (t.isAssignableFrom(o.getClass()))).iterator();
        while (it.hasNext()) {
            temp.add(it.next());
        }
        return temp;
    }

    public void setAdicionais(ArrayList<AdicionalProduto> adicionais) {
        this.adicionais = adicionais;
    }

    public boolean addAdicional(AdicionalProduto ad) {
        if (this.getP() instanceof Pizza) {
            if (ad instanceof SaborPizza) {
                int qtdSabores = ((Pizza) this.getP()).getQtdSabores();
                if (getAdicionais().stream().filter((o) -> (o instanceof SaborPizza)).count() < qtdSabores) {
                    this.getAdicionais().add(ad);
                    return true;
                } else {
                    return false;
                }
            } else if (ad instanceof BordaPizza) {
                boolean canHaveBorda = ((Pizza) this.getP()).isCanHaveBorda();
                if (getAdicionais().stream().filter((o) -> (o instanceof BordaPizza)).count() == 0 && canHaveBorda) {
                    this.getAdicionais().add(ad);
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            this.getAdicionais().add(ad);
            return true;
        }
    }

    @Override
    public int compareTo(ItemPedido t) {
        Integer otherCategory = t.getP().sequenceNr();
        Integer thisCategory = getP().sequenceNr();
        return thisCategory.compareTo(otherCategory);
    }

    @Override
    public String toString() {
        String texto = "<html><head></head><body>" + this.getQtd() + "x " + p.getNome();
        if (!this.getComentario().isEmpty()) {
            texto += " - Obs: " + this.getComentario();
        }
        if (this.getAdicionais().size() > 0) {
            texto += "<br>Adicionais: ";
            for (AdicionalProduto ad : adicionais) {
                texto += ad.getNome() + ", ";
            }
        }
        return texto + "</body></html>";
    }

}
