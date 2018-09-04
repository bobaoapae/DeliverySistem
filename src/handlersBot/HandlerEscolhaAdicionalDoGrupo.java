/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.util.ArrayList;
import java.util.List;
import modelo.AdicionalProduto;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.GrupoAdicionais;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerEscolhaAdicionalDoGrupo extends HandlerBotDelivery {

    private List<AdicionalProduto> adicionaisDisponiveis;
    private GrupoAdicionais grupoAtual;
    private HandlerBotDelivery nextHandler;

    public HandlerEscolhaAdicionalDoGrupo(GrupoAdicionais grupoAtual, HandlerBotDelivery nextHandler, ChatBot chat) {
        super(chat);
        this.grupoAtual = grupoAtual;
        this.nextHandler = nextHandler;
        nextHandler.firstRun = true;
        this.adicionaisDisponiveis = new ArrayList<>();
    }

    @Override
    protected boolean runFirstTime(Message m) {
        if (!grupoAtual.getAdicionais().isEmpty()) {
            chat.getChat().sendMessage("Qual " + grupoAtual.getNomeGrupo() + " você quer?");
            if (grupoAtual.getQtdMax() > 1) {
                chat.getChat().sendMessage("*_Obs¹: Você pode escolher no máximo " + grupoAtual.getQtdMax() + ". Envie o número da sua escolha, ou escolhas separadas por virgula. Ex: 1, 2, 3_*");
            } else if (grupoAtual.getQtdMax() == 1) {
                chat.getChat().sendMessage("*_Obs¹: Envie o número da sua escolha._*");
            } else {
                chat.getChat().sendMessage("*_Obs¹: Envie o número da sua escolha, ou escolhas separadas por virgula. Ex: 1, 2, 3_*");
            }
            if (grupoAtual.getQtdMin() == 0) {
                chat.getChat().sendMessage("*_Obs²: Caso não deseje nada, basta enviar NÃO._*");
            }
            String adicionais = "";
            for (AdicionalProduto ad : grupoAtual.getAdicionais()) {
                adicionaisDisponiveis.add(ad);
                if (ad.getValor() > 0) {
                    adicionais += "*" + adicionaisDisponiveis.size() + "* - " + ad.getNome() + " - R$ " + moneyFormat.format(ad.getValor()) + "\n";
                } else {
                    adicionais += "*" + adicionaisDisponiveis.size() + "* - " + ad.getNome() + "\n";
                }
            }
            chat.getChat().sendMessage(adicionais);
        } else {
            chat.setHandler(nextHandler, true);
        }
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        try {
            if (grupoAtual.getQtdMin() == 0 && (msg.getContent().toLowerCase().trim().contains("não") || msg.getContent().toLowerCase().trim().contains("nao") || msg.getContent().toLowerCase().trim().equals("n"))) {
                chat.setHandler(nextHandler, true);
                return true;
            }
            String[] idAdicional = msg.getContent().replaceAll(" ", "").split(",");
            int totalEscolhidos = 0;
            for (String idAtual : idAdicional) {
                if (grupoAtual.getQtdMax() > 0) {
                    if (totalEscolhidos == grupoAtual.getQtdMax()) {
                        break;
                    }
                }
                int escolha = Integer.parseInt(idAtual) - 1;
                if (escolha >= 0 && adicionaisDisponiveis.size() > escolha) {
                    ((ChatBotDelivery)chat).getLastPedido().addAdicional(adicionaisDisponiveis.get(escolha));
                    totalEscolhidos++;
                } else {
                    ((ChatBotDelivery)chat).getLastPedido().getAdicionais().removeAll(adicionaisDisponiveis);
                    return false;
                }
            }
            chat.setHandler(nextHandler, true);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
