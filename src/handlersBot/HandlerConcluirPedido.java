/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleCategorias;
import controle.ControleClientes;
import controle.ControlePedidos;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerConcluirPedido extends HandlerBotDelivery {

    public HandlerConcluirPedido(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        ((ChatBotDelivery) chat).getPedidoAtual().setChat(chat);
        ((ChatBotDelivery) chat).getPedidoAtual().setNomeCliente(((ChatBotDelivery) chat).getNome());
        ((ChatBotDelivery) chat).getPedidoAtual().setCelular(((ChatBotDelivery) chat).getCliente().getTelefoneMovel());
        try {
            chat.setHandler(new HandlerAguardandoPedidoSerImpresso(chat), false);
            ((ChatBotDelivery) chat).getCliente().realizaCompra(((ChatBotDelivery) chat).getPedidoAtual());
            if (ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).salvar(((ChatBotDelivery) chat).getPedidoAtual())) {
                ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).salvar(((ChatBotDelivery) chat).getCliente());
                chat.getChat().sendMessage("Tudo certo então!");
                if (!ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(-2).getProdutosCategoria().isEmpty()) {
                    chat.getChat().sendMessage("Já tenho todas as informações do seu pedido aqui, vou imprimir ele para o nosso Pizzaiolo e já te aviso.");
                } else {
                    chat.getChat().sendMessage("Já tenho todas as informações do seu pedido aqui, vou imprimir ele para a nossa àrea de produção e já te aviso.");
                }
                chat.getChat().sendMessage("😉");
            } else {
                chat.setHandler(this, false);
                chat.getChat().sendMessage("Ouve um erro ao salvar seu pedido!");
                chat.getChat().sendMessage("Tente novamente em alguns minutos ou aguarde nosso Atendente ler suas mensagens.");
            }
        } catch (Exception ex) {
            chat.setHandler(this, false);
            chat.getChat().sendMessage("Ouve um erro ao salvar seu pedido!");
            chat.getChat().sendMessage("Tente novamente em alguns minutos ou aguarde nosso Atendente ler suas mensagens.");
             Logger.getLogger("LogDelivery").log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        return true;
    }

    @Override
    public boolean notificaPedidosFechados() {
        return false;
    }

}
