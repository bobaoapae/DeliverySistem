/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleBordasPizza;
import modelo.BordaPizza;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pizza;

/**

 @author jvbor
 */
public class HandlerBordaPizza extends HandlerBotDelivery {

    public HandlerBordaPizza(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        Pizza p = (Pizza) ((ChatBotDelivery)chat).getLastPedido().getP();
        if (!p.isCanHaveBorda()) {
            chat.setHandler(new HandlerComentarioPedido(chat, new HandlerDesejaMaisCategoria(p.getCategoria().getRootCategoria(),chat)), true);
        } else {
            MessageBuilder builder = new MessageBuilder();
            chat.getChat().sendMessage("Deseja borda recheada na sua pizza? Caso queira basta escolher uma das opções abaixo, caso não basta enviar *NÃO*");
            chat.getChat().sendMessage("*_Obs¹: Envie somente o número da sua escolha_*");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException ex) {
            }
            for (BordaPizza borda : ControleBordasPizza.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
                if (borda.getValor() > 0) {
                    builder.textBold(borda.getCod() + "").textNewLine(" - " + borda.getNome() + " - R$ " + moneyFormat.format(borda.getValor()));
                } else {
                    builder.textBold(borda.getCod() + "").textNewLine(" - " + borda.getNome());
                }
            }
            chat.getChat().sendMessage(builder.build());
        }
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m) {
        if (m.getContent().toLowerCase().trim().equals("não") || m.getContent().toLowerCase().trim().equals("nao") || m.getContent().toLowerCase().trim().equals("n")) {
            chat.setHandler(new HandlerComentarioPedido(chat, new HandlerDesejaMaisCategoria(((ChatBotDelivery)chat).getLastPedido().getP().getCategoria().getRootCategoria(),chat)), true);
            return true;
        }
        String idAdicional = m.getContent().replaceAll(" ", "");
        try {
            int idAdicionalint = Integer.parseInt(idAdicional);
            BordaPizza l = ControleBordasPizza.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(idAdicionalint);
            if (l == null) {
                return false;
            }
            ((ChatBotDelivery)chat).getLastPedido().addAdicional(l);
        } catch (Exception ex) {
            return false;
        }
        chat.setHandler(new HandlerComentarioPedido(chat, new HandlerDesejaMaisCategoria(((ChatBotDelivery)chat).getLastPedido().getP().getCategoria().getRootCategoria(),chat)), true);
        return true;
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return false;
    }

}
