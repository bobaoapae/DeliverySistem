/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pedido;

/**

 @author jvbor
 */
public class HandlerBoasVindas extends HandlerBotDelivery {

    public HandlerBoasVindas(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        builder.text("Olá, ").text(((ChatBotDelivery)chat).getNome()).newLine();
        builder.textNewLine("Eu sou o " + Configuracao.getInstance().getNomeBot() + ", atendende virtual da " + Configuracao.getInstance().getNomeEstabelecimento() + ", e irei te ajudar a completar seu pedido").
                textNewLine("*_Lembre-se de ler as instruções com atenção_*");
        ((ChatBotDelivery)chat).setPedidoAtual(new Pedido());
        chat.getChat().sendMessage(builder.build());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(HandlerBoasVindas.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (!Configuracao.getInstance().isOpenPedidos()) {
            if (!Configuracao.getInstance().isAgendamentoDePedidos()) {
                if (!Configuracao.getInstance().isAbrirFecharPedidosAutomatico()) {
                    if (!Configuracao.getInstance().isReservasComPedidosFechados()) {
                        chat.getChat().sendMessage("_Obs: Não iniciamos nosso atendimento ainda, porfavor retorne mais tarde_",2000);
                        chat.setHandler(new HandlerAdeus(chat), true);
                    } else {
                        chat.getChat().sendMessage("_Obs: Não iniciamos nosso atendimento ainda, porém você já pode realizar sua reserva de mesa_",2000);
                        chat.setHandler(new HandlerDesejaFazerUmaReserva(chat), true);
                    }
                } else {
                    if (!Configuracao.getInstance().isReservasComPedidosFechados()) {
                        chat.getChat().sendMessage("_Obs: Não iniciamos nosso atendimento ainda, nosso atendimento iniciasse às " + Configuracao.getInstance().getHoraAutomaticaAbrirPedidos().format(DateTimeFormatter.ofPattern("HH:mm")) + "_",3500);
                        chat.setHandler(new HandlerAdeus(chat), true);
                    } else {
                        chat.getChat().sendMessage("_Obs: Não iniciamos nosso atendimento ainda, nosso atendimento iniciasse às " + Configuracao.getInstance().getHoraAutomaticaAbrirPedidos().format(DateTimeFormatter.ofPattern("HH:mm")) + ", porém você já pode realizar sua reserva de mesa_",3500);
                        chat.setHandler(new HandlerDesejaFazerUmaReserva(chat), true);
                    }
                }
            } else {
                chat.getChat().sendMessage("_Obs: Não iniciamos nosso atendimento ainda, porém você pode deixar seu pedido agendado_",3000);
                chat.setHandler(new HandlerMenuPrincipal(chat), true);
            }
        } else {
            chat.getChat().sendMessage("_Obs: Nosso prazo médio para entregas é de "+Configuracao.getInstance().getTempoMedioEntrega()+" à "+(Configuracao.getInstance().getTempoMedioEntrega()+15)+" minutos. Já para retirada cerca de "+(Configuracao.getInstance().getTempoMedioRetirada())+" à "+(Configuracao.getInstance().getTempoMedioRetirada()+5)+" minutos_",3000);
            chat.setHandler(new HandlerMenuPrincipal(chat), true);
        }
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        return runFirstTime(m);
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return false;
    }

}
