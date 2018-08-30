/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import java.util.ArrayList;
import modelo.Produto;
import modelo.Promocao;

/**

 @author jvbor
 */
public class ControlePromocoes extends ControleGenericoBasico<Promocao> {

    private static ControlePromocoes instance;

    private ControlePromocoes(Db4oGenerico db4o) {
        super(db4o, Promocao.class);
    }

    public static ControlePromocoes getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControlePromocoes(db4o);
        }
        return instance;
    }

    public ArrayList<Promocao> promocoesProduto(Produto p) {
        ArrayList<Promocao> promocoes = new ArrayList<>();
        for (Promocao promocao : carregarTodos()) {
            if (promocao.getCodP() == p.getCod()) {
                promocoes.add(promocao);
            }
        }
        return promocoes;
    }

}
