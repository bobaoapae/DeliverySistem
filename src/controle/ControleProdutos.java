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
import modelo.Produto;

/**

 @author jvbor
 */
public class ControleProdutos extends ControleGenericoBasico<Produto> {

    private static ControleProdutos instance;

    private ControleProdutos(Db4oGenerico db4o) {
        super(db4o, Produto.class);
    }

    public static ControleProdutos getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleProdutos(db4o);
        }
        return instance;
    }

    public ArrayList<Produto> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(Produto.class, "nome", nome, TipoPesquisa.LIKE);
    }

    public List<Produto> getProdutosCategoria(Categoria cat) {
        ArrayList<Produto> lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Produto.class);
        query.descend("categoria").constrain(cat).equal();
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add((Produto) b);
            }
        }
        return lista;
    }
}
