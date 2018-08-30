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
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import modelo.AdicionalProduto;
import modelo.Configuracao;
import modelo.ItemPedido;
import modelo.Pedido;
import modelo.Pizza;
import modelo.SaborPizza;
import utils.DateUtils;

/**

 @author jvbor
 */
public class ControlePedidos extends ControleGenericoBasico<Pedido> {

    private static ControlePedidos instace;

    private ControlePedidos(Db4oGenerico db4o) {
        super(db4o, Pedido.class);
    }

    public static ControlePedidos getInstance(Db4oGenerico db4o) {
        if (instace == null) {
            instace = new ControlePedidos(db4o);
        }
        return instace;
    }

    public ArrayList<Pedido> getPedidosBetweenDate(Date start, Date finish) {
        ArrayList<Pedido> lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Pedido.class);
        query.descend("estadoPedido").constrain(Pedido.EstadoPedido.Cancelado).not().and(query.descend("estadoPedido").constrain(null).not()).and(query.descend("dataPedido").constrain(start).greater().or(query.descend("dataPedido").constrain(start).equal())).and(query.descend("dataPedido").constrain(finish).smaller().or(query.descend("dataPedido").constrain(finish).equal()));
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add((Pedido) b);
            }
        }
        return lista;
    }

    public LinkedHashMap<String, Integer> getVendasBetweenDate(Date start, Date finish) {
        ArrayList<Pedido> lista = this.getPedidosBetweenDate(start, finish);
        HashMap<String, Integer> ranking = new HashMap<>();

        for (Pedido p : lista) {
            synchronized (p.getProdutos()) {
                for (ItemPedido item : p.getProdutos()) {
                    if (item.getP() instanceof Pizza) {
                        ArrayList<AdicionalProduto> saboresPizza = item.getAdicionais(SaborPizza.class);
                        for (AdicionalProduto ad : saboresPizza) {
                            if (ranking.containsKey(item.getP().getNome() + " - " + ad.getNome())) {
                                ranking.put(item.getP().getNome() + " - " + ad.getNome(), ranking.get("Pizza " + item.getP().getNome() + " - " + ad.getNome()) + 1);
                            } else {
                                ranking.put(item.getP().getNome() + " - " + ad.getNome(), 1);
                            }
                        }
                    } else {
                        if (ranking.containsKey(item.getP().getNome())) {
                            ranking.put(item.getP().getNome(), ranking.get(item.getP().getNome()) + item.getQtd());
                        } else {
                            ranking.put(item.getP().getNome(), item.getQtd());
                        }
                    }
                }
            }
        }
        LinkedHashMap<String, Integer> sortedMap
                = ranking.entrySet().stream().sorted(Entry.comparingByValue(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer t, Integer t1) {
                        return Integer.compare(t1, t);
                    }
                })).
                        collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap;
    }

    public LinkedHashMap<String, Integer> getTopVendidosMes(int limit) {
        ArrayList<Pedido> lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Pedido.class);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.setTime(DateUtils.clearTime(c.getTime()));
        query.descend("estadoPedido").constrain(Pedido.EstadoPedido.Cancelado).not().and(query.descend("estadoPedido").constrain(null).not()).and(query.descend("dataPedido").constrain(c.getTime()).greater());
        ObjectSet listaResult = query.execute();
        HashMap<String, Integer> ranking = new HashMap<>();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add((Pedido) b);
            }
        }
        for (int x = 0; x < lista.size(); x++) {
            Pedido p = lista.get(x);
            synchronized (p.getProdutos()) {
                List<ItemPedido> itens = p.getProdutos();
                for (int y = 0; y < itens.size(); y++) {
                    ItemPedido item = itens.get(y);
                    if (item.getP() instanceof Pizza) {
                        ArrayList<AdicionalProduto> saboresPizza = item.getAdicionais(SaborPizza.class);
                        for (AdicionalProduto ad : saboresPizza) {
                            if (ranking.containsKey(item.getP().getNome() + " - " + ad.getNome())) {
                                ranking.put(item.getP().getNome() + " - " + ad.getNome(), ranking.get(item.getP().getNome() + " - " + ad.getNome()) + 1);
                            } else {
                                ranking.put(item.getP().getNome() + " - " + ad.getNome(), item.getQtd());
                            }
                        }
                    } else {
                        if (ranking.containsKey(item.getP().getNome())) {
                            ranking.put(item.getP().getNome(), ranking.get(item.getP().getNome()) + item.getQtd());
                        } else {
                            ranking.put(item.getP().getNome(), item.getQtd());
                        }
                    }
                }
            }
        }
        LinkedHashMap<String, Integer> sortedMap
                = ranking.entrySet().stream().sorted(Entry.comparingByValue(new Comparator<Integer>() {
                    @Override
                    public int compare(Integer t, Integer t1) {
                        return Integer.compare(t1, t);
                    }
                })).limit(limit).
                        collect(Collectors.toMap(Entry::getKey, Entry::getValue,
                                (e1, e2) -> e1, LinkedHashMap::new));
        return sortedMap;
    }

    public ArrayList<Pedido> getPedidosDelivery() {
        ArrayList lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Pedido.class);
        query.descend("numeroMesa").constrain(0).equal();
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add(b);
            }
        }
        return lista;
    }

    public ArrayList<Pedido> getPedidosDeliveryDoDia() {
        ArrayList lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Pedido.class);
        query.descend("numeroMesa").constrain(0).equal().and(query.descend("dataPedido").constrain(Configuracao.getInstance().getHoraAberturaPedidos()).greater());
        ObjectSet listaResult = query.execute();
        while (listaResult.hasNext()) {
            Object b = listaResult.next();
            if (b != null) {
                lista.add(b);
            }
        }
        return lista;
    }

    public ArrayList<Pedido> getPedidosAtivos() {
        ArrayList lista = new ArrayList<>();
        Query query = getDb4o().getDb().query();
        query.constrain(Pedido.class);
        query.descend("estadoPedido").constrain(Pedido.EstadoPedido.Cancelado).not().and(query.descend("estadoPedido").constrain(Pedido.EstadoPedido.Concluido).not()).and(query.descend("estadoPedido").constrain(null).not());
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
