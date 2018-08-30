/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import modelo.Chat;
import modelo.ChatBotDelivery;
import modelo.UserChat;

/**

 @author jvbor
 */
public class ControleChatsAsync {

    private final List<ChatBotDelivery> chats;

    private static ControleChatsAsync instance;

    private ScheduledExecutorService executor;

    private ControleChatsAsync() {
        this.chats = Collections.synchronizedList(new ArrayList<>());
        executor = Executors.newScheduledThreadPool(3);
    }

    public static ControleChatsAsync getInstance() {
        if (instance == null) {
            instance = new ControleChatsAsync();
        }

        return instance;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public void addChat(Chat chat) {
        System.out.println(chat);
        synchronized (chats) {
            try {
                if (chat instanceof UserChat) {
                    ChatBotDelivery chatt = new ChatBotDelivery(chat, true);
                    chatt.onNewMsg(null);
                    chats.add(chatt);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public ChatBotDelivery getChatAsyncByChat(Chat chat) {
        for (ChatBotDelivery chatt : chats) {
            if (chatt.getChat().equals(chat)) {
                return chatt;
            }
        }
        return null;
    }

    public ChatBotDelivery getChatAsyncByChat(String chatid) {
        synchronized (chats) {
            for (ChatBotDelivery chatt : chats) {
                if (chatt.getChat().getId().equals(chatid)) {
                    return chatt;
                }
            }
            return null;
        }
    }

    public List<ChatBotDelivery> getChats() {
        return chats;
    }

}
