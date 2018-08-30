/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import java.util.ArrayList;
import java.util.Collections;

/**

 @author SYSTEM
 */
public class ItemComboPedido extends ItemPedido {

    private ArrayList<ItemPedido> produtosEscolhidosCombo;

    public ItemComboPedido() {
        this.produtosEscolhidosCombo = new ArrayList<>();
    }

    public ArrayList<ItemPedido> getProdutosEscolhidosCombo() {
        Collections.sort(produtosEscolhidosCombo);
        return produtosEscolhidosCombo;
    }

    public void setProdutosEscolhidosCombo(ArrayList<ItemPedido> produtosEscolhidosCombo) {
        this.produtosEscolhidosCombo = produtosEscolhidosCombo;
    }

}
