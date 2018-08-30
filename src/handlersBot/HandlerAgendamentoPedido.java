/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerAgendamentoPedido extends HandlerBotDelivery {

    public HandlerAgendamentoPedido(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        if (((ChatBotDelivery)chat).getPedidoAtual().isEntrega()) {
            chat.getChat().sendMessage("Para que horas você gostaria que a entrega fosse feita?");
        } else {
            chat.getChat().sendMessage("Que horas você quer vir buscar o seu pedido?");
        }
        chat.getChat().sendMessage("*Obs*: Envie a hora no seguinte formato *hh:mm*. Ex: *18:45*");
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        String dataS = m.getContent().trim().replaceAll(" ", "");
        try {
            LocalTime horaAtual = LocalTime.now();
            LocalTime horaInformada = LocalTime.parse(dataS, DateTimeFormatter.ofPattern("HH:mm"));
            if ((horaInformada.isAfter(horaAtual) || horaInformada.equals(horaAtual)) && (!Configuracao.getInstance().isAbrirFecharPedidosAutomatico() || (Configuracao.getInstance().isTimeBeetwenHorarioFuncionamento(horaInformada) || horaInformada.equals(Configuracao.getInstance().getHoraAutomaticaAbrirPedidos())))) {
                ((ChatBotDelivery)chat).getPedidoAtual().setHoraAgendamento(horaInformada);
                chat.setHandler(new HandlerConcluirPedido(chat), true);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    protected void onError(Message m) {
        chat.getChat().sendMessage("A hora informada é invalida, tente novamente");
        chat.getChat().sendMessage("*Obs¹*: Envie a hora no seguinte formato *hh:mm*. Ex: *18:50*");
        if (Configuracao.getInstance().isAbrirFecharPedidosAutomatico()) {
            chat.getChat().sendMessage("*Obs²*: Os horarios de agendamento disponíveis são apenas para após às *" + Configuracao.getInstance().getHoraAutomaticaAbrirPedidos().format(DateTimeFormatter.ofPattern("HH:mm")) + "*");
        }
        chat.getChat().sendMessage("*Obs³*: Você não pode informar um horario anterior à hora atual: *" + ((ChatBotDelivery)chat).getTimeFormat().format(Calendar.getInstance().getTime()) + "*");
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
