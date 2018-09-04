/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleImpressao;
import controle.ControleReservas;
import javax.swing.JOptionPane;
import modelo.Chat;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.Message;
import utils.Utilitarios;
import static visaoWeb.Inicio.driver;

/**

 @author jvbor
 */
public class HandlerFinalizarReserva extends HandlerBotDelivery {

    public HandlerFinalizarReserva(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        try {
            ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).salvar(((ChatBotDelivery) chat).getReservaAtual());
            if (!ControleImpressao.getInstance().imprimir(((ChatBotDelivery) chat).getReservaAtual())) {
                try {
                    Chat c = driver.getFunctions().getChatByNumber("554491050665");
                    if (c != null) {
                        c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Falha ao Imprimir Reserva #" + ((ChatBotDelivery) chat).getReservaAtual().getCod());
                    }
                    c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
                    if (c != null) {
                        c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Falha ao Imprimir Reserva #" + ((ChatBotDelivery) chat).getReservaAtual().getCod());
                    }
                } catch (Exception ex) {
                    driver.onError(ex);
                }
                new Thread() {
                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, "Falha ao Imprimir o Reserva #" + ((ChatBotDelivery) chat).getReservaAtual().getCod(), "Erro!", JOptionPane.ERROR_MESSAGE);
                    }
                }.start();
            }
        } catch (Exception ex) {
            chat.getChat().getDriver().onError(ex);
            chat.getChat().sendMessage("Falha ao registrar o pedido de reserva, tente novamente em alguns minutos");
            return true;
        }
        chat.getChat().sendMessage("Ótimo, seu pedido de reserva foi recebido. Agora aguarde o nosso contato para a confirmação da reserva!");
        chat.setHandler(new HandlerPedidoConcluido(chat), true);
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean notificaPedidosFechados() {
        return false;
    }

}
