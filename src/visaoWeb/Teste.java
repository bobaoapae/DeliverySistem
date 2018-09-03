/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.br.joao.Db4ObjectSaveGeneric;
import com.br.joao.Db4oGenerico;
import com.db4o.Db4oEmbedded;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.TTransient;
import com.db4o.ta.TransparentActivationSupport;
import controle.ControleProdutos;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import modelo.Pedido;
import modelo.Produto;

/**

 @author SYSTEM
 */
public class Teste {

    public static void main(String[] args) {
        EmbeddedConfiguration configClient = Db4oEmbedded.newConfiguration();
        configClient.common().detectSchemaChanges(true);
        configClient.common().exceptionsOnNotStorable(true);
        configClient.common().internStrings(true);
        configClient.common().allowVersionUpdates(true);
        configClient.common().automaticShutDown(true);
        configClient.common().activationDepth(99999999);
        configClient.common().updateDepth(99999999);
        configClient.common().add(new TransparentActivationSupport());
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).cascadeOnUpdate(true);
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).cascadeOnActivate(true);
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).indexed(true);
        configClient.common().objectClass(Clientes.class).objectField("chatId").indexed(true);
        configClient.common().objectClass(Pedido.class).objectField("estadoPedido").indexed(true);
        configClient.common().objectClass(Pedido.class).objectField("numeroMesa").indexed(true);
        configClient.common().objectClass(Collections.synchronizedList(new ArrayList<>()).getClass()).translate(new TTransient());
        configClient.common().objectClass(FXCollections.synchronizedObservableList(FXCollections.observableList(new ArrayList())).getClass()).translate(new TTransient());
        Db4oGenerico.getInstance("banco", configClient);
        for(Produto p:ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()){
            p.setVisivel(true);
            try {
                Db4oGenerico.getInstance("banco").salvarObjetoNoBanco(p);
            } catch (Exception ex) {
                Logger.getLogger(Teste.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
