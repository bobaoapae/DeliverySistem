/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleCategorias;
import controle.ControlePizzas;
import java.util.ArrayList;
import modelo.Categoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pedido;

/**

 @author jvbor
 */
public class HandlerDesejaMaisUmCombo extends HandlerBotDelivery {

    private ArrayList<HandlerBotDelivery> codigosMenu = new ArrayList<>();

    public HandlerDesejaMaisUmCombo(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        builder.textNewLine("Blz, o que você quer fazer agora?");
        builder.textNewLine("*_Obs: Envie somente o número da sua escolha_*");
        codigosMenu.add(new HandlerMenuCombos(chat));
        builder.textNewLine((codigosMenu.size()) + " - Pedir mais algum Combo 🍕");
        if (!ControlePizzas.getInstance(Db4oGenerico.getInstance("banco")).isEmpty()) {
            codigosMenu.add(new HandlerMenuPizzas(chat));
            builder.textNewLine((codigosMenu.size()) + " - Pedir uma Pizza 🍟🍔🍻");
        }
        for (Categoria c : ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).getRootCategorias()) {
            codigosMenu.add(new HandlerMenuCategoria(c, chat));
            builder.textNewLine((codigosMenu.size()) + " - Pedir " + c.getNomeCategoria());
        }
        codigosMenu.add(new HandlerAdeus(chat));
        builder.textNewLine((codigosMenu.size()) + " - Cancelar Pedido ❌");
        codigosMenu.add(new HandlerVerificaPedidoCorreto(chat));
        builder.textNewLine((codigosMenu.size()) + " - Concluir Pedido ✅");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        try {
            int escolha = Integer.parseInt(m.getContent().trim()) - 1;
            if (escolha >= 0 && codigosMenu.size() > escolha) {
                Pedido p = ((ChatBotDelivery)chat).getPedidoAtual();
                p.addItemPedido(((ChatBotDelivery)chat).getLastPedido());
                chat.setHandler(codigosMenu.get(escolha), true);
                return true;
            } else {
                return false;
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
