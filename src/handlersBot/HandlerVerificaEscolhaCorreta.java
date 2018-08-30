/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.Categoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.ItemPedido;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Produto;

/**

 @author jvbor
 */
public class HandlerVerificaEscolhaCorreta extends HandlerBotDelivery {

    private HandlerBotDelivery nextHandler, oldHandler;
    private Produto produtoEscolhido;

    public HandlerVerificaEscolhaCorreta(Produto produtoEscolhido, ChatBot chat, HandlerBotDelivery oldHandler, HandlerBotDelivery nextHandler) {
        super(chat);
        this.nextHandler = nextHandler;
        this.oldHandler = oldHandler;
        this.produtoEscolhido = produtoEscolhido;
    }

    @Override
    protected boolean runFirstTime(Message m) {
        chat.getChat().sendMessage("Você escolheu: " + this.produtoEscolhido.getNomeWithCategories());
        chat.getChat().sendMessage("O item escolhido está correto? 🤞");
        chat.getChat().sendMessage("*_Obs¹: Envie somente o número da sua escolha_*");
        boolean flagMsg = false;
        if (!this.produtoEscolhido.getCategoria().isFazEntrega() || !this.produtoEscolhido.getCategoria().getRootCategoria().isFazEntrega()) {
            chat.getChat().sendMessage("*_Obs²: Não é feita a entrega dos produtos à baixo_*", 3000);
        } else {
            List<ItemPedido> pedidos = new ArrayList<>();
            Collections.copy(((ChatBotDelivery)chat).getPedidoAtual().getProdutos(), pedidos);
            for (ItemPedido item : pedidos) {
                Categoria c = item.getP().getCategoria();
                if (!c.isFazEntrega() || !c.getRootCategoria().isFazEntrega()) {
                    flagMsg = true;
                }
                if (c.getRootCategoria().getQtdMinEntrega() > ((ChatBotDelivery)chat).getPedidoAtual().getProdutos(c).size() && !c.getRootCategoria().isPrecisaPedirOutraCategoria()) {
                    flagMsg = true;
                } else {
                    List<Categoria> categoriasCompradas = new ArrayList<>();
                    for (ItemPedido item2 : ((ChatBotDelivery)chat).getPedidoAtual().getProdutos()) {
                        if (!categoriasCompradas.contains(item2.getP().getCategoria().getRootCategoria())) {
                            categoriasCompradas.add(item2.getP().getCategoria().getRootCategoria());
                        }
                    }
                    categoriasCompradas.remove(item.getP().getCategoria().getRootCategoria());
                    boolean temCategoriaPrecisa = false;
                    for (Categoria catPrecisa : item.getP().getCategoria().getRootCategoria().getCategoriasParaPoderPedir()) {
                        if (categoriasCompradas.contains(catPrecisa)) {
                            temCategoriaPrecisa = true;
                            break;
                        }
                    }
                    if (!temCategoriaPrecisa || c.getRootCategoria().getQtdMinEntrega() > ((ChatBotDelivery)chat).getPedidoAtual().getProdutos(c).size()) {
                        flagMsg = true;
                    }
                }
            }
            if (flagMsg) {
                String categoriasComprar = "";
                for (Categoria c : this.produtoEscolhido.getCategoria().getRootCategoria().getCategoriasParaPoderPedir()) {
                    categoriasComprar += c.getNomeCategoria() + ",";
                }
                if (!categoriasComprar.isEmpty()) {
                    categoriasComprar = categoriasComprar.substring(0, categoriasComprar.lastIndexOf(","));
                }
                if (this.produtoEscolhido.getCategoria().getRootCategoria().getQtdMinEntrega() > 1 && !this.produtoEscolhido.getCategoria().getRootCategoria().isPrecisaPedirOutraCategoria()) {
                    chat.getChat().sendMessage("*_Obs²: A entrega só e feita se você pedir no minimo " + this.produtoEscolhido.getCategoria().getRootCategoria().getQtdMinEntrega() + " itens_*", 3000);
                } else if (this.produtoEscolhido.getCategoria().getRootCategoria().getQtdMinEntrega() > 1 && this.produtoEscolhido.getCategoria().getRootCategoria().isPrecisaPedirOutraCategoria()) {
                    chat.getChat().sendMessage("*_Obs²: A entrega só e feita se você pedir no minimo " + this.produtoEscolhido.getCategoria().getRootCategoria().getQtdMinEntrega() + " itens ou pedir junto algum produto de outro cardapio (" + categoriasComprar + ")_*", 3000);
                } else if (this.produtoEscolhido.getCategoria().getRootCategoria().isPrecisaPedirOutraCategoria()) {
                    chat.getChat().sendMessage("*_Obs²: A entrega só e feita se você pedir junto algum produto de outro cardapio (" + categoriasComprar + ")_*", 3000);
                }
            }
        }
        MessageBuilder builder = new MessageBuilder();
        builder.textNewLine("*1* - Sim").
                textNewLine("*2* - Não").
                textNewLine("*3* - Cancelar Pedido");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        if (msg.getContent().trim().toLowerCase().equals("1") || msg.getContent().trim().toLowerCase().equals("sim") || msg.getContent().trim().toLowerCase().equals("s")) {
            ItemPedido item = new ItemPedido();
            item.setP(produtoEscolhido);
            ((ChatBotDelivery)chat).setLastPedido(item);
            chat.setHandler(nextHandler, true);
        } else if (msg.getContent().trim().toLowerCase().equals("2") || msg.getContent().trim().toLowerCase().equals("nao") || msg.getContent().trim().toLowerCase().equals("n") || msg.getContent().trim().toLowerCase().equals("não")) {
            chat.getChat().sendMessage("Certo, informe sua escolha novamente por favor");
            chat.setHandler(oldHandler, false);
        } else if (msg.getContent().trim().toLowerCase().equals("3") || msg.getContent().trim().toLowerCase().contains("cancela")) {
            chat.setHandler(new HandlerAdeus(chat), true);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
