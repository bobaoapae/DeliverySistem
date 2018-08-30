/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.util.HashMap;

/**

 @author jvbor
 */
public class PersistenciaChats extends Db4ObjectSaveGeneric{

    private String chatId;
    private HashMap<String, Object> values;

    public PersistenciaChats(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public HashMap<String, Object> getValues() {
        if (values == null) {
            values = new HashMap<>();
        }
        return values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }

}
