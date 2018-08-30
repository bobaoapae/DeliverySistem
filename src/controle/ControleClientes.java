/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import com.br.joao.TipoPesquisa;
import java.util.ArrayList;
import java.util.Calendar;
import modelo.Chat;
import modelo.Cliente;

/**

 @author jvbor
 */
public class ControleClientes extends ControleGenericoBasico<Cliente> {

    private static ControleClientes instance;

    private ControleClientes(Db4oGenerico db4o) {
        super(db4o, Cliente.class);
    }

    public static ControleClientes getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleClientes(db4o);
        }
        return instance;
    }

    public Cliente findClienteByChat(Chat chat) {
        return findClienteByChat(chat.getId());
    }

    public Cliente findClienteByChat(String chatid) {
        return (Cliente) this.getDb4o().pesquisarObjetoNoBanco(Cliente.class, "chatId", chatid, TipoPesquisa.EQUAL);
    }

    public ArrayList<Cliente> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(Cliente.class, "nome", nome, TipoPesquisa.LIKE);
    }

    public ArrayList<Cliente> findClientesInativosA(int dias) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, dias);
        return this.getDb4o().pesquisarObjetosNoBanco(Cliente.class, "dataUltimaCompra", c.getTime(), TipoPesquisa.SMALLER);
    }

    public ArrayList<Cliente> getAniversariantes() {
        Calendar c = Calendar.getInstance();
        return this.getDb4o().pesquisarObjetosNoBanco(Cliente.class, "dataAniversario", c.getTime(), TipoPesquisa.LIKE);
    }

}
