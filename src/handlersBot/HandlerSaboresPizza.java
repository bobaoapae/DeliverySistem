/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleSaboresPizza;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pizza;
import modelo.SaborPizza;

/**

 @author jvbor
 */
public class HandlerSaboresPizza extends HandlerBotDelivery {

    public HandlerSaboresPizza(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        if (!ControleSaboresPizza.getInstance(Db4oGenerico.getInstance("banco")).isEmpty()) {
            chat.getChat().sendMessage("Ótimo, agora escolha os sabores da sua pizza! 🍕🍕");
            if (((Pizza) ((ChatBotDelivery)chat).getLastPedido().getP()).getQtdSabores() > 1) {
                chat.getChat().sendMessage("*_Obs¹: Você pode escolher até " + ((Pizza) ((ChatBotDelivery)chat).getLastPedido().getP()).getQtdSabores() + " sabores_*");
            } else {
                chat.getChat().sendMessage("*_Obs¹: Você pode escolher apenas 1 sabor_*");
            }
            chat.getChat().sendMessage("*_Obs²: Envie somente o número da sua escolha, ou escolhas separadas por virgula. Ex: 1, 2, 3_*");
            MessageBuilder builder = new MessageBuilder();
            for (SaborPizza ad : ControleSaboresPizza.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
                if (ad.getNome().toLowerCase().contains("(especial)")) {
                    builder.textBold(ad.getCod() + "").textNewLine(" - " + ad.getNome() + " - R$ " + moneyFormat.format(((Pizza) ((ChatBotDelivery)chat).getLastPedido().getP()).getValorEspecial())).textNewLine("_" + ad.getDescricao() + "_");
                } else {
                    builder.textBold(ad.getCod() + "").textNewLine(" - " + ad.getNome()).textNewLine("_" + ad.getDescricao() + "_");
                }
                builder.newLine();
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
            }
            chat.getChat().sendMessage(builder.build());
        } else {
            chat.setHandler(new HandlerBordaPizza(chat), true);
        }
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        String[] idAdicional = msg.getContent().replaceAll(" ", "").split(",");
        for (String idAtual : idAdicional) {
            try {
                int idAdicionalint = Integer.parseInt(idAtual);
                SaborPizza l = ControleSaboresPizza.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(idAdicionalint);
                if (l == null) {
                    ((ChatBotDelivery)chat).getLastPedido().getAdicionais().clear();
                    return false;
                }
                ((ChatBotDelivery)chat).getLastPedido().addAdicional(l);
            } catch (Exception ex) {
                ((ChatBotDelivery)chat).getLastPedido().getAdicionais().clear();
                return false;
            }
        }
        chat.setHandler(new HandlerVerificaSaboresPizzaCorreto(chat, this), true);
        return true;
    }

    @Override
    protected void onError(Message m) {
        MessageBuilder builder = new MessageBuilder();
        builder.textNewLine("Essa opção não é valida 😕");
        builder.textNewLine("*_Caso deseje conversar com um atendente envie: Ajuda_*");
        chat.getChat().sendMessage(builder.build());
        chat.getChat().sendMessage("Por favor, informe o número da sua escolha novamente, e envie os numeros separados APENAS por vírgula. Ex: 12, 35, 10");
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
