/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import modelo.Categoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.ItemPedido;
import modelo.Message;
import modelo.MessageBuilder;
import modelo.Produto;

/**

 @author jvbor
 */
public class HandlerMenuCategoria extends HandlerBotDelivery {

    private Categoria c;
    private ArrayList<HandlerBotDelivery> codigosMenu = new ArrayList<>();

    public HandlerMenuCategoria(Categoria c, ChatBot chat) {
        super(chat);
        this.c = c;
    }

    @Override
    protected boolean runFirstTime(Message m) {
        MessageBuilder builder = new MessageBuilder();
        chat.getChat().sendMessage("Segue as opções de: " + c.getNomeCategoria() + ".");
        chat.getChat().sendMessage("*_Obs¹: Envie somente o número da sua escolha_*");
        chat.getChat().sendMessage("*_Obs²: Escolha um item por vez_*", 2000);
        if (!c.isFazEntrega()) {
            chat.getChat().sendMessage("*_Obs³: Não é feita a entrega dos produtos à baixo_*", 3000);
        } else {
            List<ItemPedido> pedidos = new ArrayList<>();
            Collections.copy(((ChatBotDelivery) chat).getPedidoAtual().getProdutos(), pedidos);
            boolean temCategoriaPrecisa = false;
            boolean msg = false;
            List<Categoria> categoriasCompradas = new ArrayList<>();
            for (ItemPedido item2 : ((ChatBotDelivery) chat).getPedidoAtual().getProdutos()) {
                if (!categoriasCompradas.contains(item2.getP().getCategoria().getRootCategoria())) {
                    categoriasCompradas.add(item2.getP().getCategoria().getRootCategoria());
                }
            }

            for (Categoria catPrecisa : c.getRootCategoria().getCategoriasParaPoderPedir()) {
                if (categoriasCompradas.contains(catPrecisa)) {
                    temCategoriaPrecisa = true;
                    break;
                }
            }
            if (!temCategoriaPrecisa || c.getRootCategoria().getQtdMinEntrega() > ((ChatBotDelivery) chat).getPedidoAtual().getProdutos(c).size()) {
                msg = true;
            }
            if (c.getQtdMinEntrega() > 1 && !c.isPrecisaPedirOutraCategoria() && msg) {
                chat.getChat().sendMessage("*_Obs³: A entrega só e feita se você pedir no minimo " + c.getQtdMinEntrega() + " itens_*", 3000);
            } else if (c.getQtdMinEntrega() > 1 && c.isPrecisaPedirOutraCategoria() && msg) {
                chat.getChat().sendMessage("*_Obs³: A entrega só e feita se você pedir no minimo " + c.getQtdMinEntrega() + " itens ou pedir junto algum produto de outro cardapio_*", 3000);
            } else if (c.isPrecisaPedirOutraCategoria() && msg) {
                chat.getChat().sendMessage("*_Obs³: A entrega só e feita se você pedir junto algum produto de outro cardapio_*", 3000);
            }
        }
        gerarMenu(c, builder);
        builder.textNewLine("---------");
        codigosMenu.add(new HandlerMenuPrincipal(chat));
        builder.textNewLine("*" + (codigosMenu.size()) + "* - Voltar ao Menu Principal ↩️");
        codigosMenu.add(new HandlerAdeus(chat));
        builder.textNewLine("*" + (codigosMenu.size()) + "* - Cancelar Pedido ❌");
        builder.textNewLine("*_Obs¹: Envie somente o número da sua escolha_*");
        builder.textNewLine("*_Obs²: Escolha um item por vez_*");
        chat.getChat().sendMessage(builder.build());
        return true;
    }

    @Override
    protected boolean runSecondTime(Message m
    ) {
        try {
            int escolha = Integer.parseInt(m.getContent().trim()) - 1;
            if (escolha >= 0 && codigosMenu.size() > escolha) {
                chat.setHandler(codigosMenu.get(escolha), true);
                return true;
            } else {
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }

    private void gerarMenu(Categoria c, MessageBuilder builder) {
        Calendar dataAtual = Calendar.getInstance();
        int diaSemana = dataAtual.get(Calendar.DAY_OF_WEEK) - 1;
        LocalTime horaAtual = LocalTime.now();
        if (!c.equals(this.c)) {
            builder.text(".          *-" + c.getNomeCategoria() + "-*");
            builder.newLine();
            if (c.getRootCategoria().isFazEntrega()) {
                if (!c.isFazEntrega()) {
                    builder.text("(*Não é feita a entrega*)");
                } else {
                    List<ItemPedido> pedidos = new ArrayList<>();
                    Collections.copy(((ChatBotDelivery) chat).getPedidoAtual().getProdutos(), pedidos);
                    boolean temCategoriaPrecisa = false;
                    boolean msg = false;
                    List<Categoria> categoriasCompradas = new ArrayList<>();
                    for (ItemPedido item2 : ((ChatBotDelivery) chat).getPedidoAtual().getProdutos()) {
                        if (!categoriasCompradas.contains(item2.getP().getCategoria().getRootCategoria())) {
                            categoriasCompradas.add(item2.getP().getCategoria().getRootCategoria());
                        }
                    }

                    for (Categoria catPrecisa : c.getRootCategoria().getCategoriasParaPoderPedir()) {
                        if (categoriasCompradas.contains(catPrecisa)) {
                            temCategoriaPrecisa = true;
                            break;
                        }
                    }
                    if (!temCategoriaPrecisa || c.getRootCategoria().getQtdMinEntrega() > ((ChatBotDelivery) chat).getPedidoAtual().getProdutos(c).size()) {
                        msg = true;
                    }
                    if (c.getQtdMinEntrega() > 1 && !c.isPrecisaPedirOutraCategoria() && msg) {
                        builder.text("(*A entrega só e feita se você pedir no minimo " + c.getQtdMinEntrega() + " itens*)");
                    } else if (c.getQtdMinEntrega() > 1 && c.isPrecisaPedirOutraCategoria() && msg) {
                        builder.text("(*A entrega só e feita se você pedir no minimo " + c.getQtdMinEntrega() + " itens ou pedir junto algum produto de outro cardapio*)");
                    } else if (c.isPrecisaPedirOutraCategoria() && msg) {
                        builder.text("(*A entrega só e feita se você pedir junto algum produto de outro cardapio*)");
                    }
                }
            }
        }
        for (Categoria cF : c.getCategoriaFilhas()) {
            if (!cF.isVisivel()) {
                continue;
            }
            gerarMenu(cF, builder);
        }
        builder.newLine();
        for (Produto l : c.getProdutosCategoria()) {
            if (l.isOnlyLocal()) {
                continue;
            }
            if (!l.isVisivel()) {
                continue;
            }
            if (l.getRestricaoVisibilidade() != null) {
                if (l.getRestricaoVisibilidade().isRestricaoDia()) {
                    if (!l.getRestricaoVisibilidade().getDiasSemana()[diaSemana]) {
                        continue;
                    }
                }
                if (l.getRestricaoVisibilidade().isRestricaoHorario()) {
                    if (!(horaAtual.isAfter(l.getRestricaoVisibilidade().getHorarioDe()) && horaAtual.isBefore(l.getRestricaoVisibilidade().getHorarioAte()))) {
                        continue;
                    }
                }
            }
            if (l.getCategoria().getRootCategoria().getCod() != -2) {
                codigosMenu.add(new HandlerVerificaEscolhaCorreta(l, chat, this, new HandlerAdicionaisProduto(l, chat)));
            } else {
                codigosMenu.add(new HandlerVerificaEscolhaCorreta(l, chat, this, new HandlerSaboresPizza(chat)));
            }
            if (l.getValor() > 0) {
                builder.textNewLine("*" + (codigosMenu.size()) + " - " + l.getNome() + " - R$" + moneyFormat.format(l.getValor()) + "*");
            } else {
                builder.textNewLine("*" + (codigosMenu.size()) + " - " + l.getNome() + "*");
            }
            if (!l.getDescricao().trim().isEmpty()) {
                builder.textNewLine("       _" + l.getDescricao() + "_");
            }
            builder.newLine();
        }
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
