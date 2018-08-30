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
public class PedidoMesa extends Pedido {

    private ArrayList<PedidoMesa> pedidosRelacionados;

    public PedidoMesa() {
        pedidosRelacionados = new ArrayList<>();
    }

    public PedidoMesa(PedidoMesa... pedidos) {
        this();
        for (PedidoMesa p : pedidos) {
            this.addPedidoRelacionado(p);
        }
    }

    public void addPedidoRelacionado(PedidoMesa p) {
        if (!p.equals(this) && !this.pedidosRelacionados.contains(p)) {
            this.getPedidosRelacionados().add(p);
            p.addPedidoRelacionado(this);
        }
        for (PedidoMesa pedidoJaRelacionado : pedidosRelacionados) {
            if (!pedidoJaRelacionado.getPedidosRelacionados().contains(p)) {
                pedidoJaRelacionado.addPedidoRelacionado(p);
            }
        }
    }

    public ArrayList<PedidoMesa> getPedidosRelacionados() {
        return pedidosRelacionados;
    }

    public void setPedidosRelacionados(ArrayList<PedidoMesa> pedidosRelacionados) {
        this.pedidosRelacionados = pedidosRelacionados;
    }

    public Pedido getPedidoCompleto() {
        Pedido p = new Pedido();
        for (PedidoMesa pp : pedidosRelacionados) {
            for (ItemPedido item : pp.getProdutos()) {
                if (!p.getProdutos().contains(item)) {
                    p.addItemPedido(item);
                }
            }
        }
        for (ItemPedido item : this.getProdutos()) {
            if (!p.getProdutos().contains(item)) {
                p.addItemPedido(item);
            }
        }
        return p;
    }

    public double getTotalComOsRelacionados() {
        return getPedidoCompleto().getTotal();
    }

}
