/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControlePizzas;
import modelo.ChatBot;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pizza;

/**
 *
 * @author jvbor
 */
public class HandlerMenuPizzas extends HandlerBotDelivery {

    private long lastCodePizzas;

    public HandlerMenuPizzas(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        chat.getChat().sendMessage("Escolha alguma das Pizzas abaixo 🍕🍕");
        chat.getChat().sendMessage("*_Obs¹: Envie somente o número da sua escolha_*");
        chat.getChat().sendMessage("*_Obs²: Escolha um item por vez_*");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
        }
        lastCodePizzas = 0;
        for (Pizza l : ControlePizzas.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            if (l.isOnlyLocal()) {
                continue;
            }
            builder.textNewLine("*"+l.getCod() + "* - 🍕 " + l.getNome() + " - " + l.getQtdPedacos() + " Pedaços" + " - " + l.getQtdSabores() + " Sabores - R$" + moneyFormat.format(l.getValor()));
            if (!l.getDescricao().isEmpty()) {
                builder.textNewLine("_" + l.getDescricao() + "_");
            }
            lastCodePizzas = l.getCod();
        }
        builder.textNewLine("*"+(lastCodePizzas + 1) + "* - Voltar ao Menu Principal ↩️");
        builder.textNewLine("*"+(lastCodePizzas + 2) + "* - Cancelar Pedido ❌");
        builder.textNewLine("*_Obs¹: Envie somente o número da sua escolha_*");
        builder.textNewLine("*_Obs²: Escolha um item por vez_*");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        String idLanche = m.getContent().trim();
        if (idLanche.equals(lastCodePizzas + 1 + "") || idLanche.toLowerCase().equals("volta")) {
            chat.setHandler(new HandlerMenuPrincipal(chat), true);
            return true;
        } else if (idLanche.equals(lastCodePizzas + 2 + "") || idLanche.toLowerCase().equals("cancela")) {
            chat.setHandler(new HandlerAdeus(chat), true);
            return true;
        }
        try {
            int idLancheInt = Integer.parseInt(idLanche);
            Pizza l = ControlePizzas.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(idLancheInt);
            if (l == null) {
                return false;
            } else {
                chat.setHandler(new HandlerVerificaEscolhaCorreta(l,chat, this, new HandlerSaboresPizza(chat)), true);
                return true;
            }
        } catch (Exception ex) {
            return false;
        }
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
