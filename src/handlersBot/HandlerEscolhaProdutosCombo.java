/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.ItemComboPedido;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerEscolhaProdutosCombo extends HandlerBotDelivery {

    public HandlerEscolhaProdutosCombo(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        ItemComboPedido item = (ItemComboPedido) ((ChatBotDelivery)chat).getLastPedido();
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        return true;
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
