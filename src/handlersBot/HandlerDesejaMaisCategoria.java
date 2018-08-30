/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleCategorias;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import modelo.Categoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pedido;

/**

 @author jvbor
 */
public class HandlerDesejaMaisCategoria extends HandlerBotDelivery {

    private ArrayList<HandlerBotDelivery> codigosMenu = new ArrayList<>();
    private Categoria c;

    public HandlerDesejaMaisCategoria(Categoria c, ChatBot chat) {
        super(chat);
        this.c = c.getRootCategoria();
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        builder.textNewLine("Blz, o que você quer fazer agora?");
        codigosMenu.add(new HandlerMenuCategoria(c, chat));
        builder.textNewLine("*" + (codigosMenu.size()) + "* - Pedir mais " + c.getNomeCategoria());
        Calendar dataAtual = Calendar.getInstance();
        int diaSemana = dataAtual.get(Calendar.DAY_OF_WEEK) - 1;
        LocalTime horaAtual = LocalTime.now();
        for (Categoria c : ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).getRootCategorias()) {
            if (c.equals(this.c) || !c.isVisivel() || (c.getProdutosCategoria().isEmpty() && c.getCategoriaFilhas().isEmpty())) {
                continue;
            }
            if (c.getRestricaoVisibilidade() != null) {
                if (c.getRestricaoVisibilidade().isRestricaoDia()) {
                    if (!c.getRestricaoVisibilidade().getDiasSemana()[diaSemana]) {
                        continue;
                    }
                }
                if (c.getRestricaoVisibilidade().isRestricaoHorario()) {
                    if (!(horaAtual.isAfter(c.getRestricaoVisibilidade().getHorarioDe()) && horaAtual.isBefore(c.getRestricaoVisibilidade().getHorarioAte()))) {
                        continue;
                    }
                }
            }
            codigosMenu.add(new HandlerMenuCategoria(c, chat));
            builder.textNewLine("*" + (codigosMenu.size()) + "* - Pedir " + c.getNomeCategoria());
        }
        codigosMenu.add(new HandlerAdeus(chat));
        builder.textNewLine("*" + (codigosMenu.size()) + "* - Cancelar Pedido ❌");
        codigosMenu.add(new HandlerVerificaPedidoCorreto(chat));
        builder.textNewLine("*" + (codigosMenu.size()) + "* - Concluir Pedido ✅");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        try {
            int escolha = Integer.parseInt(m.getContent().trim()) - 1;
            if (escolha >= 0 && codigosMenu.size() > escolha) {
                Pedido p = ((ChatBotDelivery) chat).getPedidoAtual();
                p.addItemPedido(((ChatBotDelivery) chat).getLastPedido());
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
