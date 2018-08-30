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
import modelo.Rodizio;

/**

 @author jvbor
 */
public class ControleRodizios extends ControleGenericoBasico<Rodizio> {

    private static ControleRodizios instance;

    private ControleRodizios(Db4oGenerico db4o) {
        super(db4o, Rodizio.class);
    }

    public static ControleRodizios getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleRodizios(db4o);
        }
        return instance;
    }

    public ArrayList<Rodizio> pesquisarPorNome(String nome) {
        return this.getDb4o().pesquisarObjetosNoBanco(Rodizio.class, "nome", nome, TipoPesquisa.LIKE);
    }

    public ArrayList<Rodizio> rodiziosDoDia() {
        ArrayList<Rodizio> rodizios = carregarTodos();
        ArrayList<Rodizio> temp = new ArrayList<>();
        int diaSemana = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1;
        for (Rodizio r : rodizios) {
            if (r.getDiasSemana()[diaSemana]) {
                temp.add(r);
            }
        }
        return temp;
    }

}
