/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import modelo.AdicionalProduto;
import modelo.BordaPizza;
import modelo.Categoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.ItemPedido;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Pedido;
import modelo.Pizza;
import modelo.SaborPizza;

/**

 @author jvbor
 */
public class HandlerVerificaPedidoCorreto extends HandlerBotDelivery {

    public HandlerVerificaPedidoCorreto(ChatBot chat) {
        super(chat);
    }

    @Override
    protected boolean runFirstTime(Message m) {
        chat.getChat().sendMessage("Vou mandar um resumo do seu pedido para que você verifique se está tudo certo, okay ☺️?!");
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
        }
        Pedido p = ((ChatBotDelivery)chat).getPedidoAtual();
        MessageBuilder builder = new MessageBuilder();
        for (int x = 0; x < p.getProdutos().size(); x++) {
            ItemPedido produto = p.getProdutos().get(x);
            if (!(produto.getP() instanceof Pizza)) {
                builder.textNewLine(produto.getP().getNomeWithCategories()+ (produto.getComentario().isEmpty() ? "" : " Obs: " + produto.getComentario()));
                if (produto.getAdicionais().size() > 0) {
                    String adicionais = "";
                    for (int y = 0; y < produto.getAdicionais().size(); y++) {
                        AdicionalProduto adicional = produto.getAdicionais().get(y);
                        adicionais += adicional.getNome();
                        if (y < produto.getAdicionais().size() - 1) {
                            adicionais += ",";
                        }
                    }
                    if (adicionais.endsWith(",")) {
                        adicionais = adicionais.substring(0, adicionais.lastIndexOf(","));
                    }
                    builder.textNewLine("Adicionais: " + adicionais);
                }
            } else {
                builder.textNewLine("Pizza - " + produto.getP().getNomeWithCategories() + (produto.getComentario().isEmpty() ? "" : " Obs: " + produto.getComentario()));
                ArrayList<AdicionalProduto> sabores = produto.getAdicionais(SaborPizza.class);
                ArrayList<AdicionalProduto> bordas = produto.getAdicionais(BordaPizza.class);
                if (sabores.size() > 0) {
                    String adicionais = "";
                    for (int y = 0; y < sabores.size(); y++) {
                        AdicionalProduto adicional = sabores.get(y);
                        adicionais += adicional.getNome();
                        if (y < produto.getAdicionais().size() - 1) {
                            adicionais += ",";
                        }
                    }
                    if (adicionais.endsWith(",")) {
                        adicionais = adicionais.substring(0, adicionais.lastIndexOf(","));
                    }
                    builder.textNewLine("Sabores: " + adicionais);
                }
                if (bordas.size() > 0) {
                    builder.textNewLine("Borda: " + bordas.get(0).getNome());
                }
            }
        }
        builder.textNewLine("Total: R$" + moneyFormat.format(p.getTotal()) + " 💵");
        chat.getChat().sendMessage(builder.build(), 4000);
        chat.getChat().sendMessage("Está tudo certo? 🤞");
        chat.getChat().sendMessage("*_Obs: Envie somente o número da sua escolha_*");
        builder = new MessageBuilder();
        builder.textNewLine("*1* - Sim").
                textNewLine("*2* - Não  (Inicia o Pedido Novamente)");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        if (msg.getContent().trim().equals("1") || msg.getContent().toLowerCase().trim().equals("sim") || msg.getContent().toLowerCase().trim().equals("s")) {
            List<ItemPedido> pedidos = new ArrayList<>();
            Collections.copy(((ChatBotDelivery)chat).getPedidoAtual().getProdutos(), pedidos);
            for (ItemPedido item : pedidos) {
                Categoria c = item.getP().getCategoria();
                if (!c.isFazEntrega() || !c.getRootCategoria().isFazEntrega()) {
                    ((ChatBotDelivery)chat).getPedidoAtual().setEntrega(false);
                    chat.getChat().sendMessage("O seu pedido foi marcado automaticamente como para retirada, pois algum produto que você pediu não pode ser entregue, caso queira cancelar basta enviar *CANCELAR* a qualquer momento", 2000);
                    if (((ChatBotDelivery)chat).getCliente().getCreditosDisponiveis() > 0) {
                        chat.setHandler(new HandlerDesejaUtilizarCreditos(chat), true);
                    } else {
                        chat.setHandler(new HandlerDesejaAgendar(chat), true);
                    }
                    return true;
                }
                if (c.getRootCategoria().getQtdMinEntrega() > ((ChatBotDelivery)chat).getPedidoAtual().getProdutos(c).size() && !c.getRootCategoria().isPrecisaPedirOutraCategoria()) {
                    ((ChatBotDelivery)chat).getPedidoAtual().setEntrega(false);
                    chat.getChat().sendMessage("O seu pedido foi marcado automaticamente como para retirada, pois algum produto que você pediu não pode ser entregue, caso queira cancelar basta enviar *CANCELAR* a qualquer momento", 2000);
                    if (((ChatBotDelivery)chat).getCliente().getCreditosDisponiveis() > 0) {
                        chat.setHandler(new HandlerDesejaUtilizarCreditos(chat), true);
                    } else {
                        chat.setHandler(new HandlerDesejaAgendar(chat), true);
                    }
                    return true;
                } else {
                    List<Categoria> categoriasCompradas = new ArrayList<>();
                    for (ItemPedido item2 : ((ChatBotDelivery)chat).getPedidoAtual().getProdutos()) {
                        if (!categoriasCompradas.contains(item2.getP().getCategoria().getRootCategoria())) {
                            categoriasCompradas.add(item2.getP().getCategoria().getRootCategoria());
                        }
                    }
                    categoriasCompradas.remove(item.getP().getCategoria().getRootCategoria());
                    boolean temCategoriaPrecisa = false;
                    for (Categoria catPrecisa : item.getP().getCategoria().getRootCategoria().getCategoriasParaPoderPedir()) {
                        if (categoriasCompradas.contains(catPrecisa)) {
                            temCategoriaPrecisa = true;
                            break;
                        }
                    }
                    if (!temCategoriaPrecisa || c.getRootCategoria().getQtdMinEntrega() > ((ChatBotDelivery)chat).getPedidoAtual().getProdutos(c).size()) {
                        ((ChatBotDelivery)chat).getPedidoAtual().setEntrega(false);
                        chat.getChat().sendMessage("O seu pedido foi marcado automaticamente para retirada no balcão, pois algum produto que você pediu não pode ser entregue, caso queira cancelar basta enviar *CANCELAR* a qualquer momento", 2000);
                        if (((ChatBotDelivery)chat).getCliente().getCreditosDisponiveis() > 0) {
                            chat.setHandler(new HandlerDesejaUtilizarCreditos(chat), true);
                        } else {
                            chat.setHandler(new HandlerDesejaAgendar(chat), true);
                        }
                        return true;
                    }
                }
            }
            chat.getChat().sendMessage("Ótimo, agora só falta você me dizer como deseja retirar o seu pedido. 😁", 2000);
            chat.setHandler(new HandlerFormaRetirada(chat), true);
        } else if (msg.getContent().trim().equals("2") || msg.getContent().toLowerCase().trim().equals("não") || msg.getContent().toLowerCase().trim().equals("nao") || msg.getContent().toLowerCase().trim().equals("n")) {
            chat.getChat().sendMessage("Oh ☹️, sinto muito.");
            chat.getChat().sendMessage("Vamos começar novamente, espero que agora de tudo certo. 🤞😄");
            chat.setHandler(new HandlerBoasVindas(chat), true);
        } else {
            return false;
        }
        return true;
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
