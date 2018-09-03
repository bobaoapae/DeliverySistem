/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleCombos;
import java.util.logging.Level;
import java.util.logging.Logger;
import modelo.ChatBot;
import modelo.Combo;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Produto;

/**

 @author jvbor
 */
public class HandlerMenuCombos extends HandlerBotDelivery {

    private long lastCodeCombo;

    public HandlerMenuCombos(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        chat.getChat().sendMessage("Escolha alguma dos Combos abaixo 🍟🍔🍻");
        chat.getChat().sendMessage("*_Obs¹: Envie somente o número da sua escolha_*");
        chat.getChat().sendMessage("*_Obs²: Escolha um item por vez_*");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(HandlerBoasVindas.class.getName()).log(Level.SEVERE, null, ex);
        }
        lastCodeCombo = 0;
        for (Combo l : ControleCombos.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            if (l.isOnlyLocal()) {
                continue;
            }
            builder.textNewLine(l.getCod() + " - 🍟🍔🍻 " + l.getNome() + " - R$" + moneyFormat.format(l.getValor()));
            if (!l.getDescricao().isEmpty()) {
                builder.textNewLine("_" + l.getDescricao() + "_");
            }
            for (Produto p : l.getProdutosCombo()) {

            }
            lastCodeCombo = l.getCod();
        }
        builder.textNewLine(lastCodeCombo + 1 + " - Voltar ao Menu Principal ↩️");
        builder.textNewLine(lastCodeCombo + 2 + " - Cancelar Pedido ❌");
        builder.textNewLine("*_Obs¹: Envie somente o número da sua escolha_*");
        builder.textNewLine("*_Obs²: Escolha um item por vez_*");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        String tdEscolha = m.getContent().trim();
        if (tdEscolha.equals(lastCodeCombo + 1 + "") || tdEscolha.toLowerCase().equals("volta")) {
            chat.setHandler(new HandlerMenuPrincipal(chat), true);
            return true;
        } else if (tdEscolha.equals(lastCodeCombo + 2 + "") || tdEscolha.toLowerCase().equals("cancela")) {
            chat.setHandler(new HandlerAdeus(chat), true);
            return true;
        }
        try {
            return false;
        } catch (Exception ex) {
            return false;
        }
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
