/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleCategorias;
import controle.ControleCombos;
import controle.ControleRodizios;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import modelo.Categoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.Message;
import modelo.MessageBuilder;

/**

 @author jvbor
 */
public class HandlerMenuPrincipal extends HandlerBotDelivery {

    private ArrayList<HandlerBotDelivery> codigosMenu = new ArrayList<>();

    public HandlerMenuPrincipal(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        chat.getChat().sendMessage("Qual cardapio você gostaria de olhar?", 2000);
        builder.textNewLine("*_Obs: Envie somente o número da sua escolha_*");
        if (!ControleCombos.getInstance(Db4oGenerico.getInstance("banco")).isEmpty()) {
            codigosMenu.add(new HandlerMenuCombos(chat));
            builder.textNewLine("*" + (codigosMenu.size()) + "* - Combos 🍟🍔🍻");
        }
        Calendar dataAtual = Calendar.getInstance();
        int diaSemana = dataAtual.get(Calendar.DAY_OF_WEEK) - 1;
        LocalTime horaAtual = LocalTime.now();
        for (Categoria c : ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).getRootCategorias()) {
            if (c.getProdutosCategoria().isEmpty() && c.getCategoriaFilhas().isEmpty()) {
                continue;
            }
            if (!c.isVisivel()) {
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
            builder.textNewLine("*" + (codigosMenu.size()) + "* - " + c.getNomeCategoria());
        }
        if (Configuracao.getInstance().isReservas()) {
            codigosMenu.add(new HandlerRealizarReserva(chat));
            builder.textNewLine("*" + (codigosMenu.size()) + "* - Realizar Reserva");
        }
        if (!ControleRodizios.getInstance(Db4oGenerico.getInstance("banco")).isEmpty()) {
            codigosMenu.add(new HandlerMenuRodizios(chat));
            builder.textNewLine("*" + (codigosMenu.size()) + "* - Ver Rodizios");
        }
        codigosMenu.add(new HandlerAdeus(chat));
        builder.textNewLine("*" + (codigosMenu.size()) + "* - Cancelar Pedido ❌");
        if (((ChatBotDelivery) chat).getPedidoAtual() != null && ((ChatBotDelivery) chat).getPedidoAtual().getProdutos().size() > 0) {
            codigosMenu.add(new HandlerVerificaPedidoCorreto(chat));
            builder.textNewLine("*" + (codigosMenu.size()) + "* - Concluir Pedido ✅");
        }
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        try {
            int escolha = Integer.parseInt(m.getContent().trim()) - 1;
            if (escolha >= 0 && codigosMenu.size() > escolha) {
                chat.setHandler(codigosMenu.get(escolha), true);
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
