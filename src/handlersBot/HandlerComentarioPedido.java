/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import modelo.AdicionalProduto;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.ItemComboPedido;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pizza;
import modelo.SaborPizza;

/**

 @author jvbor
 */
public class HandlerComentarioPedido extends HandlerBotDelivery {

    private HandlerBotDelivery nextHandler;

    public HandlerComentarioPedido(ChatBot chat, HandlerBotDelivery nextHandler) {
        super(chat);
        this.nextHandler = nextHandler;
    }

    @Override
    protected boolean runFirstTime(Message m) {
        if (((ChatBotDelivery)chat).getLastPedido().getP().getCategoria().getExemplosComentarioPedido().isEmpty()) {
            ((ChatBotDelivery)chat).getLastPedido().setComentario("");
            chat.setHandler(nextHandler, true);
            return true;
        }
        if ((((ChatBotDelivery)chat).getLastPedido() instanceof ItemComboPedido)) {
            chat.getChat().sendMessage("Você deseja modificar algo em seu pedido?", 300);
        } else {
            if (!(((ChatBotDelivery)chat).getLastPedido().getP() instanceof Pizza)) {
                if (!((ChatBotDelivery)chat).getLastPedido().getP().getDescricao().isEmpty()) {
                    chat.getChat().sendMessage("_Seu " + ((ChatBotDelivery)chat).getLastPedido().getP().getNome() + "  tem os seguintes ingredientes: " + ((ChatBotDelivery)chat).getLastPedido().getP().getDescricao() + "_", 300);
                }
            } else {
                MessageBuilder builder = new MessageBuilder();
                builder.textNewLine("Sua pizza tem os seguintes sabores:");
                for (AdicionalProduto adicional : ((ChatBotDelivery)chat).getLastPedido().getAdicionais(SaborPizza.class)) {
                    builder.textBold(adicional.getNome()).text(" - ").text(adicional.getDescricao()).newLine();
                }
                chat.getChat().sendMessage(builder.build(), 1000);
            }
            chat.getChat().sendMessage("Você deseja modificar algo em seu pedido?");
            if ((((ChatBotDelivery)chat).getLastPedido().getP() instanceof Pizza)) {
                chat.getChat().sendMessage("Por exemplo: sem palmito, sem cebola... etc");
            } else if (((ChatBotDelivery)chat).getLastPedido().getP().getCategoria().getExemplosComentarioPedido() != null && !((ChatBotDelivery)chat).getLastPedido().getP().getCategoria().getExemplosComentarioPedido().isEmpty()) {
                chat.getChat().sendMessage("Por exemplo: " + ((ChatBotDelivery)chat).getLastPedido().getP().getCategoria().getExemplosComentarioPedido() + "... etc");
            }
        }
        if (((ChatBotDelivery)chat).getLastPedido().getP() instanceof Pizza) {
            chat.getChat().sendMessage("Basta escrever e me enviar, o que você escrever sera repassado para nosso Pizzaiolo 👨‍🍳", 300);
        } else {
            chat.getChat().sendMessage("Basta escrever e me enviar, o que você escrever sera repassado para a àrea de produção", 300);
        }
        chat.getChat().sendMessage("*_Obs¹: Caso não queira modificar nada, basta enviar NÃO_*");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        chat.getChat().sendMessage("*_Obs²: Não use esse campo para pedir pizza, porções ou bebidas aguarde as próximas opções para isso_*");
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        if (msg.getContent().toLowerCase().trim().equals("não") || msg.getContent().toLowerCase().trim().equals("nao") || msg.getContent().toLowerCase().trim().equals("n")) {
            ((ChatBotDelivery)chat).getLastPedido().setComentario("");
        } else {
            ((ChatBotDelivery)chat).getLastPedido().setComentario(msg.getContent().trim());
            chat.getChat().sendMessage("Perfeito, já anotei aqui o que você me disse ✌️😉");
        }
        chat.setHandler(nextHandler, true);
        return true;
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
