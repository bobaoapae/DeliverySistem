/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.util.ArrayList;
import modelo.AdicionalProduto;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.SaborPizza;

/**

 @author jvbor
 */
public class HandlerVerificaSaboresPizzaCorreto extends HandlerBotDelivery {

    private HandlerBotDelivery oldHandler;

    public HandlerVerificaSaboresPizzaCorreto(ChatBot chat, HandlerBotDelivery oldHandler) {
        super(chat);
        this.oldHandler = oldHandler;
    }

    @Override
    protected boolean runFirstTime(Message m) {
        ArrayList<AdicionalProduto> sabores = ((ChatBotDelivery)chat).getLastPedido().getAdicionais(SaborPizza.class);
        String adicionais = "";
        for (int x = 0; x < sabores.size(); x++) {
            AdicionalProduto adicional = sabores.get(x);
            adicionais += adicional.getNome();
            if (x < sabores.size() - 1) {
                adicionais += ",";
            }
        }
        if (adicionais.endsWith(",")) {
            adicionais = adicionais.substring(0, adicionais.lastIndexOf(","));
        }
        chat.getChat().sendMessage("Você escolheu os seguintes sabores : " + adicionais);
        chat.getChat().sendMessage("Os sabores escolhidos estão corretos? 🤞");
        chat.getChat().sendMessage("*_Obs: Envie somente o número da sua escolha_*");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
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
            chat.setHandler(new HandlerBordaPizza(chat), true);
        } else if (msg.getContent().trim().toLowerCase().equals("2") || msg.getContent().trim().toLowerCase().equals("nao") || msg.getContent().trim().toLowerCase().equals("n") || msg.getContent().trim().toLowerCase().equals("não")) {
            chat.getChat().sendMessage("Certo, informe sua escolha novamente por favor");
            ((ChatBotDelivery)chat).getLastPedido().getAdicionais().clear();
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
