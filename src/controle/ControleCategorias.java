/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import com.br.joao.TipoPesquisa;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
import java.util.ArrayList;
import java.util.List;
import modelo.Categoria;

/**

 @author jvbor
 */
public class ControleCategorias extends ControleGenericoBasico<Categoria> {

    private static ControleCategorias instance;

    private ControleCategorias(Db4oGenerico db4o) {
        super(db4o, Categoria.class);
    }

    public static ControleCategorias getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleCategorias(db4o);
        }
        return instance;
    }

    public ArrayList<Categoria> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(Categoria.class, "nome", nome, TipoPesquisa.LIKE);
    }

    public List<Categoria> getRootCategorias() {
        ArrayList<Categoria> lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Categoria.class);
        query.descend("categoriaPai").constrain(null).equal();
        query.descend("ordemExibicao").orderAscending();
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add((Categoria) b);
            }
        }
        return lista;
    }
}
