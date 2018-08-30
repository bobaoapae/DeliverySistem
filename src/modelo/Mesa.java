/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4oGenerico;
import controle.ControlePromocoes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**

 @author jvbor
 */
public class Mesa extends Observable {

    private int numeroMesa;
    private ObservableList<Pedido> pedidos;
    private boolean aberta;

    public Mesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
        this.aberta = true;
        pedidos = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());
    }

    public boolean isAberta() {
        return aberta;
    }

    public void setAberta(boolean aberta) {
        this.aberta = aberta;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        setChanged();
        notifyObservers();
        this.numeroMesa = numeroMesa;
    }

    public ObservableList<Pedido> getPedidos() {
        return pedidos;
    }

    public double getTotal() {
        double total = 0;
        double desconto = 0;
        HashMap<Produto, Integer> hashMap = new HashMap<>();
        synchronized (pedidos) {
            for (Pedido p : pedidos) {
                for (ItemPedido item : p.getProdutos()) {
                    ArrayList<Promocao> promocoes = ControlePromocoes.getInstance(Db4oGenerico.getInstance("banco")).promocoesProduto(item.getP());
                    for (Promocao promo : promocoes) {
                        for (CategoriaPromocao catPro : promo.getCategoriasPromocao()) {
                            if (catPro == CategoriaPromocao.POR_QUANTIDADE) {
                                if (!hashMap.containsKey(item.getP())) {
                                    hashMap.put(item.getP(), item.getQtd());
                                } else {
                                    hashMap.put(item.getP(), hashMap.get(item.getP()) + item.getQtd());
                                }
                                while (hashMap.get(item.getP()) >= promo.getQtd()) {
                                    desconto += promo.getValor();
                                    hashMap.put(item.getP(), hashMap.get(item.getP()) - promo.getQtd());
                                }
                            }
                        }
                    }
                }
                total += p.getTotal();
            }
        }
        return total - desconto;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.numeroMesa;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Mesa other = (Mesa) obj;
        if (this.numeroMesa != other.numeroMesa) {
            return false;
        }
        return true;
    }

}
