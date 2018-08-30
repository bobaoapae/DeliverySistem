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
import modelo.BordaPizza;

/**
 *
 * @author jvbor
 */
public class ControleBordasPizza extends ControleGenericoBasico<BordaPizza> {

    private static ControleBordasPizza instance;

    private ControleBordasPizza(Db4oGenerico db4o) {
        super(db4o, BordaPizza.class);
    }

    public static ControleBordasPizza getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleBordasPizza(db4o);
        }
        return instance;
    }

    public ArrayList<BordaPizza> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(BordaPizza.class, "nome", nome, TipoPesquisa.LIKE);
    }
}
