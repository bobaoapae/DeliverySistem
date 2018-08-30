/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import java.util.ArrayList;

/**

 @author SYSTEM
 */
public class Combo extends Produto {

    private ArrayList<Produto> produtosCombo;

    public Combo() {
        this.produtosCombo = new ArrayList<>();
    }

    @Override
    public String getNome() {
        if (!nome.startsWith("Combo - ")) {
            return "Combo - " + super.getNome(); //To change body of generated methods, choose Tools | Templates.
        } else {
            return super.getNome();
        }
    }

    public ArrayList<Produto> getProdutosCombo() {
        return produtosCombo;
    }

    public void setProdutosCombo(ArrayList<Produto> produtosCombo) {
        this.produtosCombo = produtosCombo;
    }

    @Override
    public int sequenceNr() {
        return 1;
    }

}
