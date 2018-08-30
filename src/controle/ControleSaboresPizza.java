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
import modelo.SaborPizza;

/**
 *
 * @author jvbor
 */
public class ControleSaboresPizza extends ControleGenericoBasico<SaborPizza> {

    private static ControleSaboresPizza instance;

    private ControleSaboresPizza(Db4oGenerico db4o) {
        super(db4o, SaborPizza.class);
    }

    public static ControleSaboresPizza getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleSaboresPizza(db4o);
        }
        return instance;
    }

    public ArrayList<SaborPizza> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(SaborPizza.class, "nome", nome, TipoPesquisa.LIKE);
    }
}
