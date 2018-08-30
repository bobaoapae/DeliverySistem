/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerFormaRetirada extends HandlerBotDelivery {

    public HandlerFormaRetirada(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        chat.getChat().sendMessage("Informo que nosso prazo médio para entrega é de "+Configuracao.getInstance().getTempoMedioEntrega()+" à "+(Configuracao.getInstance().getTempoMedioEntrega()+15)+" minutos. Já para retirada cerca de "+(Configuracao.getInstance().getTempoMedioRetirada())+" à "+(Configuracao.getInstance().getTempoMedioRetirada()+5)+" minutos.",2000);
        chat.getChat().sendMessage("Você quer que seu pedido seja para entrega ou retirada no balcão?");
        chat.getChat().sendMessage("*_Obs: Envie somente o número da sua escolha_*");
        chat.getChat().sendMessage("*1* - 🛵 Entrega");
        chat.getChat().sendMessage("*2* - 🛎️ Retirada no balcão");
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        if (msg.getContent().trim().equals("1") || msg.getContent().toLowerCase().trim().contains("entrega")) {
            ((ChatBotDelivery)chat).getPedidoAtual().setEntrega(true);
            chat.getChat().sendMessage("Blz");
            if (((ChatBotDelivery)chat).getCliente().getEndereco() == null || ((ChatBotDelivery)chat).getCliente().getEndereco().isEmpty()) {
                chat.setHandler(new HandlerSolicitarEndereco(chat), true);
            } else {
                chat.setHandler(new HandlerUsarUltimoEndereco(chat), true);
            }
        } else if (msg.getContent().trim().equals("2") || msg.getContent().toLowerCase().trim().contains("retira") || msg.getContent().toLowerCase().trim().contains("busca")) {
            ((ChatBotDelivery)chat).getPedidoAtual().setEntrega(false);
            if (((ChatBotDelivery)chat).getCliente().getCreditosDisponiveis() > 0) {
                chat.setHandler(new HandlerDesejaUtilizarCreditos(chat), true);
            } else {
                chat.setHandler(new HandlerDesejaAgendar(chat), true);
            }
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
