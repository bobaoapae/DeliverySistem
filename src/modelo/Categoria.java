/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.util.ArrayList;

/**

 @author jvbor
 */
public class Categoria extends Db4ObjectSaveGeneric implements Comparable<Categoria> {

    private Categoria categoriaPai;
    private ArrayList<Categoria> categoriaFilhas;
    private ArrayList<Categoria> categoriasParaPoderPedir;
    private ArrayList<Produto> produtosCategoria;
    private ArrayList<GrupoAdicionais> grupoAdicionais;
    private ArrayList<AdicionalCategoria> adicionaisCategoria;
    private String nomeCategoria;
    private String exemplosComentarioPedido;
    private int qtdMinEntrega, ordemExibicao;
    private boolean fazEntrega, precisaPedirOutraCategoria, visivel;
    private RestricaoVisibilidade restricaoVisibilidade;

    public Categoria() {
        this.produtosCategoria = new ArrayList<>();
        this.categoriaFilhas = new ArrayList<>();
        this.categoriasParaPoderPedir = new ArrayList<>();
        this.grupoAdicionais = new ArrayList<>();
        this.qtdMinEntrega = 1;
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

    public ArrayList<AdicionalCategoria> getAdicionaisCategoria() {
        return adicionaisCategoria;
    }

    public void setAdicionaisCategoria(ArrayList<AdicionalCategoria> adicionaisCategoria) {
        this.adicionaisCategoria = adicionaisCategoria;
    }

    public ArrayList<GrupoAdicionais> getGrupoAdicionais() {
        if (grupoAdicionais == null) {
            grupoAdicionais = new ArrayList<>();
        }
        return grupoAdicionais;
    }

    public void setGrupoAdicionais(ArrayList<GrupoAdicionais> grupoAdicionais) {
        this.grupoAdicionais = grupoAdicionais;
    }

    public ArrayList<Categoria> getCategoriasParaPoderPedir() {
        return categoriasParaPoderPedir;
    }

    public void setCategoriasParaPoderPedir(ArrayList<Categoria> categoriasParaPoderPedir) {
        this.categoriasParaPoderPedir = categoriasParaPoderPedir;
    }

    public boolean isPrecisaPedirOutraCategoria() {
        return precisaPedirOutraCategoria;
    }

    public void setPrecisaPedirOutraCategoria(boolean precisaPedirOutraCategoria) {
        this.precisaPedirOutraCategoria = precisaPedirOutraCategoria;
    }

    public int getOrdemExibicao() {
        return ordemExibicao;
    }

    public void setOrdemExibicao(int ordemExibicao) {
        this.ordemExibicao = ordemExibicao;
    }

    public boolean isFazEntrega() {
        return fazEntrega;
    }

    public void setFazEntrega(boolean fazEntrega) {
        this.fazEntrega = fazEntrega;
    }

    public int getQtdMinEntrega() {
        return qtdMinEntrega;
    }

    public void setQtdMinEntrega(int qtdMinEntrega) {
        this.qtdMinEntrega = qtdMinEntrega;
    }

    public String getExemplosComentarioPedido() {
        return exemplosComentarioPedido;
    }

    public void setExemplosComentarioPedido(String exemplosComentarioPedido) {
        this.exemplosComentarioPedido = exemplosComentarioPedido;
    }

    public boolean isRootCategoria() {
        return categoriaPai == null;
    }

    public Categoria getCategoriaPai() {
        return categoriaPai;
    }

    public void setCategoriaPai(Categoria categoriaPai) {
        if (this.categoriaPai != null && this.categoriaPai != categoriaPai) {
            this.categoriaPai.getCategoriaFilhas().remove(this);
        }
        if (categoriaPai != this.categoriaPai) {
            categoriaPai.getCategoriaFilhas().add(this);
        }
        this.categoriaPai = categoriaPai;
    }

    public ArrayList<Categoria> getCategoriaFilhas() {
        return categoriaFilhas;
    }

    public void setCategoriaFilhas(ArrayList<Categoria> categoriaFilhas) {
        this.categoriaFilhas = categoriaFilhas;
    }

    public ArrayList<Produto> getProdutosCategoria() {
        return produtosCategoria;
    }

    public void setProdutosCategoria(ArrayList<Produto> produtosCategoria) {
        this.produtosCategoria = produtosCategoria;
    }

    public String getNomeCategoria() {
        return nomeCategoria;
    }

    public void setNomeCategoria(String nomeCategoria) {
        this.nomeCategoria = nomeCategoria;
    }

    public Categoria getRootCategoria() {
        if (this.categoriaPai == null) {
            return this;
        } else {
            Categoria catAtual = this;
            while (true) {
                if (catAtual.getCategoriaPai() != null) {
                    catAtual = catAtual.getCategoriaPai();
                } else {
                    return catAtual;
                }
            }
        }
    }

    @Override
    public int compareTo(Categoria t) {
        return Integer.compare(this.ordemExibicao, t.ordemExibicao);
    }

}
