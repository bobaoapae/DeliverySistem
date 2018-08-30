/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import java.util.ArrayList;
import java.util.List;
import modelo.AdicionalCategoria;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.GrupoAdicionais;
import modelo.Message;
import modelo.Produto;

/**

 @author jvbor
 */
public class HandlerAdicionaisProduto extends HandlerBotDelivery {

    private Produto p;
    private List<AdicionalCategoria> adicionaisDisponiveis;
    private List<GrupoAdicionais> gruposDisponiveis;
    private List<GrupoAdicionais> gruposJaForam;

    public HandlerAdicionaisProduto(Produto p, ChatBot chat) {
        super(chat);
        this.p = p;
        adicionaisDisponiveis = new ArrayList<>();
        gruposDisponiveis = p.getAllGruposAdicionais();
        gruposJaForam = new ArrayList<>();
    }

    @Override
    protected boolean runFirstTime(Message m) {
        for (GrupoAdicionais grupo : gruposDisponiveis) {
            if (gruposJaForam.contains(grupo)) {
                continue;
            }
            gruposJaForam.add(grupo);
            if (grupo.getAdicionais().size() <= grupo.getQtdMin()) {
                ((ChatBotDelivery)chat).getLastPedido().getAdicionais().addAll(grupo.getAdicionais());
                continue;
            }
            chat.setHandler(new HandlerEscolhaAdicionalDoGrupo(grupo, this, chat), true);
            return true;
        }
        chat.setHandler(new HandlerComentarioPedido(chat, new HandlerDesejaMaisCategoria(p.getCategoria(), chat)), true);
        return true;
    }

    @Override
    protected boolean runSecondTime(Message msg) {
        return runFirstTime(msg);
    }

    @Override
    public boolean notificaPedidosFechados() {
        return true;
    }

}
