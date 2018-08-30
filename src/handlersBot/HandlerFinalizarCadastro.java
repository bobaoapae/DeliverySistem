/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package handlersBot;

import com.br.joao.Db4oGenerico;
import controle.ControleClientes;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Message;

/**

 @author jvbor
 */
public class HandlerFinalizarCadastro extends HandlerBotDelivery {
    
    public HandlerFinalizarCadastro(ChatBot chat) {
        super(chat);
    }
    
    @Override
    protected boolean runFirstTime(Message m) {
        try {
            ((ChatBotDelivery)chat).getCliente().setCadastroRealizado(true);
            ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).salvar(((ChatBotDelivery)chat).getCliente());
        } catch (Exception ex) {
            chat.getChat().getDriver().onError(ex);
            chat.getChat().sendMessage("Falha ao salvar seu cadastro, tente novamente em alguns minutos");
            return true;
        }
        chat.getChat().sendMessage("Parabéns, você finalizou seu cadastro, agora você podera participar das nossas promoções e descontos exclusivos!");
        chat.setHandler(new HandlerAdeus(chat), true);
        return true;
    }
    
    @Override
    protected boolean runSecondTime(Message m) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public boolean notificaPedidosFechados() {
        return false;
    }
    
}
