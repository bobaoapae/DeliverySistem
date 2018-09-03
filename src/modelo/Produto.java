/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**

 @author jvbor
 */
public class Produto extends Db4ObjectSaveGeneric implements Comparable<Produto> {

    protected String nome, descricao, image;
    protected double valor;
    protected boolean onlyLocal, visivel;
    protected Categoria categoria;
    protected List<GrupoAdicionais> adicionaisDisponiveis;
    protected RestricaoVisibilidade restricaoVisibilidade;
    
    public Produto() {
        nome = "";
        descricao = "";
        onlyLocal = false;
        adicionaisDisponiveis = new ArrayList<>();
        visivel = true;
    }

    public boolean isVisivel() {
        return visivel;
    }

    public void setVisivel(boolean visivel) {
        this.visivel = visivel;
    }

    public RestricaoVisibilidade getRestricaoVisibilidade() {
        return restricaoVisibilidade;
    }

    public void setRestricaoVisibilidade(RestricaoVisibilidade restricaoVisibilidade) {
        this.restricaoVisibilidade = restricaoVisibilidade;
    }

    public List<GrupoAdicionais> getAdicionaisDisponiveis() {
        if (adicionaisDisponiveis == null) {
            adicionaisDisponiveis = new ArrayList<>();
        }
        return adicionaisDisponiveis;
    }

    public void setAdicionaisDisponiveis(List<GrupoAdicionais> adicionaisDisponiveis) {
        this.adicionaisDisponiveis = adicionaisDisponiveis;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        if (this.categoria != null && this.categoria != categoria) {
            this.categoria.getProdutosCategoria().remove(this);
        }
        if (categoria != this.categoria) {
            categoria.getProdutosCategoria().add(this);
        }
        this.categoria = categoria;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNome() {
        if (nome == null) {
            nome = "";
        }
        return nome;
    }

    public List<GrupoAdicionais> getAllGruposAdicionais() {
        List<GrupoAdicionais> adicionais = new ArrayList<>();
        List<Categoria> categorias = new ArrayList<>();
        Categoria catAtual = this.getCategoria();
        while (true) {
            categorias.add(catAtual);
            if (catAtual.getCategoriaPai() != null) {
                catAtual = catAtual.getCategoriaPai();
            } else {
                break;
            }
        }
        Collections.reverse(categorias);
        for (Categoria c : categorias) {
            adicionais.addAll(c.getGrupoAdicionais());
        }
        adicionais.addAll(this.getAdicionaisDisponiveis());
        return adicionais;
    }

    public String getNomeWithCategories() {
        List<Categoria> categorias = new ArrayList<>();
        Categoria catAtual = this.getCategoria();
        while (true) {
            categorias.add(catAtual);
            if (catAtual.getCategoriaPai() != null) {
                catAtual = catAtual.getCategoriaPai();
            } else {
                break;
            }
        }
        Collections.reverse(categorias);
        String cats = "";
        for (Categoria c : categorias) {
            cats += c.getNomeCategoria() + " - ";
        }
        return cats + this.getNome();
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        if (descricao == null) {
            descricao = "";
        }
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isOnlyLocal() {
        return onlyLocal;
    }

    public void setOnlyLocal(boolean onlyLocal) {
        this.onlyLocal = onlyLocal;
    }

    public int sequenceNr() {
        if (this.getCategoria() != null) {
            return this.getCategoria().getOrdemExibicao();
        } else {
            return 999999999;
        }
    }

    @Override
    public String toString() {
        return "#" + getCod() + " " + nome + " - R$ " + new DecimalFormat("###,###,###.00").format(valor);
    }

    @Override
    public int compareTo(Produto t) {
        return Integer.compare(this.sequenceNr(), t.sequenceNr());
    }

}
