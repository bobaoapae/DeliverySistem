/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import com.db4o.query.Predicate;
import java.util.List;
import modelo.PersistenciaChats;

/**

 @author jvbor
 */
public class ControlePersistenciaChats extends ControleGenericoBasico<PersistenciaChats> {

    private static ControlePersistenciaChats instance;

    public static ControlePersistenciaChats getInstance(Db4oGenerico db4oGenerico) {
        if (instance == null) {
            instance = new ControlePersistenciaChats(db4oGenerico);
        }
        return instance;
    }

    private ControlePersistenciaChats(Db4oGenerico db4o) {
        super(db4o, PersistenciaChats.class);
    }

    public PersistenciaChats getPersistenciaChat(String chatid) {
        List<PersistenciaChats> persistencias = this.getDb4o().getDb().query(new Predicate<PersistenciaChats>() {
            @Override
            public boolean match(PersistenciaChats et) {
                return et.getChatId().equals(chatid);
            }
        });
        if (persistencias.size() == 1) {
            return persistencias.get(0);
        }
        return null;
    }

}
