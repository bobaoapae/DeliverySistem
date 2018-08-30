/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.text.DecimalFormat;

/**

 @author jvbor
 */
public class AdicionalProduto extends Db4ObjectSaveGeneric {

    private String nome;
    private String descricao, image;
    private double valor;
    private boolean ativo;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getNome() {
        if(nome==null){
            return "";
        }
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    @Override
    public String toString() {
        return "#" + getCod() + " " + nome + " - R$ " + new DecimalFormat("###,###,###.00").format(valor);
    }

}
