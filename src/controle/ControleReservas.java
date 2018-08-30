/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.ControleGenericoBasico;
import com.br.joao.Db4oGenerico;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
import java.util.ArrayList;
import java.util.Date;
import modelo.Reserva;
import utils.DateUtils;

/**

 @author jvbor
 */
public class ControleReservas extends ControleGenericoBasico<Reserva> {
    
    private static ControleReservas instance;
    
    public static ControleReservas getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleReservas(db4o);
        }
        return instance;
    }
    
    private ControleReservas(Db4oGenerico db4o) {
        super(db4o, Reserva.class);
    }
    
    public ArrayList<Reserva> getReservasAtivas() {
        ArrayList lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Reserva.class);
        query.descend("dataReserva").constrain(DateUtils.clearTime(new Date())).greater().or(query.descend("dataReserva").constrain(DateUtils.clearTime(new Date())).equal());
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add(b);
            }
        }
        return lista;
    }
    
}
