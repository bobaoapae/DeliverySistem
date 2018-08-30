/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import java.util.HashMap;
import modelo.Configuracao;

/**

 @author jvbor
 */
public class ControleConfiguracao extends ControleGenericoBasico<Configuracao> {

    private static HashMap<Db4oGenerico, ControleConfiguracao> instances = new HashMap<>();

    public static ControleConfiguracao getInstance(Db4oGenerico db4o) {
        if (!instances.containsKey(db4o)) {
            instances.put(db4o, new ControleConfiguracao(db4o));
        }
        return instances.get(db4o);
    }

    private ControleConfiguracao(Db4oGenerico db4o) {
        super(db4o, Configuracao.class);
    }

}
