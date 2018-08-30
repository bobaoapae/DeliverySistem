/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.util.Random;
import modelo.ChatBot;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerAguardandoPedidoSerImpresso extends HandlerBotDelivery {

    private String[] agradecimentos = {"👍", "🤙", "🤝", "☺️"};

    public HandlerAguardandoPedidoSerImpresso(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        if (m.getContent().toLowerCase().contains("vlw") || m.getContent().toLowerCase().contains("obrigado") || m.getContent().toLowerCase().contains("ok")) {
            chat.getChat().sendMessage(agradecimentos[new Random().nextInt(agradecimentos.length - 1)]);
            return true;
        }
        chat.getChat().sendMessage("Seu pedido ainda esta sendo impresso, aguarde um momento!");
        return true;

    }

    @Override
    protected boolean runSecondTime(Message msg) {
        runFirstTime(msg);
        return true;
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return false;
    }

}
