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
import modelo.Combo;

/**

 @author jvbor
 */
public class ControleCombos extends ControleGenericoBasico<Combo> {

    private static ControleCombos instance;

    private ControleCombos(Db4oGenerico db4o) {
        super(db4o, Combo.class);
    }

    public static ControleCombos getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleCombos(db4o);
        }
        return instance;
    }

    public ArrayList<Combo> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(Combo.class, "nome", nome, TipoPesquisa.LIKE);
    }
}
