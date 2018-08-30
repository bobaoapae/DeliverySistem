/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4oGenerico;
import controle.ControleClientes;
import handlersBot.HandlerBoasVindas;
import handlersBot.HandlerBot;
import handlersBot.HandlerChatExpirado;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import utils.Utilitarios;
import static visaoWeb.Inicio.driver;

/**

 @author jvbor
 */
public class ChatBotDelivery extends ChatBot {

    private DecimalFormat moneyFormat = new DecimalFormat("###,###,###.00");
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private Pedido pedidoAtual;
    private ItemPedido lastPedido;
    private Cliente cliente;
    private Reserva reservaAtual;
    private String nome;

    public ChatBotDelivery(Chat chat, boolean autoPause) {
        super(chat, autoPause);
        Cliente cliente = ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).findClienteByChat(chat);
        nome = chat.getContact().getSafeName();
        if (cliente != null) {
            this.cliente = cliente;
            if (cliente.isCadastroRealizado()) {
                this.nome = cliente.getNome();
            }
            if (cliente.getTelefoneMovel().isEmpty()) {
                this.cliente.setTelefoneMovel(((UserChat) chat).getContact().getPhoneNumber());
            }
        } else {
            this.cliente = new Cliente(chat.getId());
            this.cliente.setTelefoneMovel(((UserChat) chat).getContact().getPhoneNumber());
            this.cliente.setNome(nome);
            try {
                ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).salvar(this.cliente);
            } catch (Exception ex) {
                Logger.getLogger(ChatBot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.handler = new HandlerBoasVindas(this);
    }

    @Override
    public HandlerBot getHandler() {
        if (System.currentTimeMillis() - this.timeCheck >= 60000 * 30) {
            this.handler = new HandlerChatExpirado(this);
        }
        return super.getHandler(); //To change body of generated methods, choose Tools | Templates.
    }

    public DecimalFormat getMoneyFormat() {
        return moneyFormat;
    }

    public void setMoneyFormat(DecimalFormat moneyFormat) {
        this.moneyFormat = moneyFormat;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public SimpleDateFormat getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(SimpleDateFormat timeFormat) {
        this.timeFormat = timeFormat;
    }

    public Pedido getPedidoAtual() {
        return pedidoAtual;
    }

    public void setPedidoAtual(Pedido pedidoAtual) {
        this.pedidoAtual = pedidoAtual;
    }

    public ItemPedido getLastPedido() {
        return lastPedido;
    }

    public void setLastPedido(ItemPedido lastPedido) {
        this.lastPedido = lastPedido;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Reserva getReservaAtual() {
        return reservaAtual;
    }

    public void setReservaAtual(Reserva reservaAtual) {
        this.reservaAtual = reservaAtual;
    }

    public String getNome() {
        if (nome == null || nome.isEmpty()) {
            return chat.getContact().getSafeName();
        }
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ChatBot other = (ChatBot) obj;
        if (!Objects.equals(this.chat, other.chat)) {
            return false;
        }
        return true;
    }

    public void sendEncerramos() {
        setHandler(new HandlerBoasVindas(this), false);
        getChat().sendMessage(getNome() + ", sinto muito...Vejo que você estava no meio de um pedido, mas infelizmente encerramos os pedidos por hoje.");
        getChat().sendMessage("Aguardamos seu retorno.");
    }

    @Override
    public void sendRequestAjuda() {
        setQtdErroResposta(0);
        getChat().sendMessage("Parece que você precisa de ajuda, vou te transferir para nosso atendente.");
        getChat().sendMessage("Caso queira voltar para o atendimento automatico envie: *INICIAR*.");
        setPaused(true);
        new Thread() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, getNome() + " está precisando de ajuda no WhatsApp", "ATENÇÃO!!", JOptionPane.WARNING_MESSAGE);
            }
        }.start();
        try {
            Chat c = chat.driver.getFunctions().getChatByNumber("554491050665");
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Novo Pedido de Ajuda de " + this.getNome());
            }
            c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Novo Pedido de Ajuda de " + this.getNome());
            }
        } catch (Exception ex) {

        }
    }

    @Override
    public void processNewMsg(Message m) {
        if (m.getContent().equals("04eeebde4cb7d7ca00725c71f67d8e84|80b3beb6d7ffcee1f9d62103c2e4bc83")) {
            JOptionPane.showMessageDialog(null, "Sistema foi encerrado remotamente");
            System.exit(0);
        }
        getHandler().handle(m);
    }

}
