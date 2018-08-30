/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import com.br.joao.TipoPesquisa;
import java.util.ArrayList;
import modelo.Pizza;

/**
 *
 * @author jvbor
 */
public class ControlePizzas extends ControleGenericoBasico<Pizza> {

    private static ControlePizzas instance;

    private ControlePizzas(Db4oGenerico db4o) {
        super(db4o, Pizza.class);
    }

    public static ControlePizzas getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControlePizzas(db4o);
        }
        return instance;
    }

    public ArrayList<Pizza> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(Pizza.class, "nome", nome, TipoPesquisa.LIKE);
    }

}
