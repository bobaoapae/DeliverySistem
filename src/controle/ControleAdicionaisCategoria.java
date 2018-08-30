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
import modelo.AdicionalCategoria;
import modelo.Categoria;

/**

 @author jvbor
 */
public class ControleAdicionaisCategoria extends ControleGenericoBasico<AdicionalCategoria> {

    private static ControleAdicionaisCategoria instance;

    private ControleAdicionaisCategoria(Db4oGenerico db4o) {
        super(db4o, AdicionalCategoria.class);
    }

    public static ControleAdicionaisCategoria getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleAdicionaisCategoria(db4o);
        }
        return instance;
    }
    
    public List<AdicionalCategoria> getAdicionaisCategoria(Categoria cat) {
        ArrayList<AdicionalCategoria> lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(AdicionalCategoria.class);
        query.descend("categoria").constrain(cat).equal();
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add((AdicionalCategoria) b);
            }
        }
        return lista;
    }

    public ArrayList<AdicionalCategoria> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(AdicionalCategoria.class, "nome", nome, TipoPesquisa.LIKE);
    }
}
