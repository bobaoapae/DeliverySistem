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
public class Promocao extends Db4ObjectSaveGeneric {

    private Produto p;
    private TipoPromocao tipoPromocao;
    private ArrayList<CategoriaPromocao> categoriasPromocao;
    private double valor;
    private int qtd;

    public Promocao(Produto p, TipoPromocao tipoPromocao, CategoriaPromocao... categorias) {
        categoriasPromocao = new ArrayList<>();
        this.tipoPromocao = tipoPromocao;
        for (CategoriaPromocao c : categorias) {
            if (!categoriasPromocao.contains(c)) {
                categoriasPromocao.add(c);
            }
        }
        this.p = p;
    }

    public Produto getP() {
        return p;
    }

    public void setP(Produto p) {
        this.p = p;
    }

    public ArrayList<CategoriaPromocao> getCategoriasPromocao() {
        return categoriasPromocao;
    }

    public void setCategoriasPromocao(ArrayList<CategoriaPromocao> categoriasPromocao) {
        this.categoriasPromocao = categoriasPromocao;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getQtd() {
        return qtd;
    }

    public void setQtd(int qtd) {
        this.qtd = qtd;
    }

    public TipoPromocao getTipoPromocao() {
        return tipoPromocao;
    }

    public void setTipoPromocao(TipoPromocao tipoPromocao) {
        this.tipoPromocao = tipoPromocao;
    }

}
