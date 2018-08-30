/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.br.joao.Db4oGenerico;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleImpressao;
import controle.ControlePedidos;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Cliente;
import modelo.Configuracao;
import modelo.Pedido;
import utils.JXBrowserCrack;

/**

 @author SYSTEM
 */
public class HistoricoPedidos extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Cliente c;
    private SimpleDateFormat formatadorComAno = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatadorComAnoIngles = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatadorSemAno = new SimpleDateFormat("dd/MM");

    public HistoricoPedidos() {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
    }

    public HistoricoPedidos(Cliente c) {
        this();
        this.c = c;
        this.setTitle("Historico de Pedidos de " + c.getNome());
    }

    private void init() {
        if (Configuracao.getInstance().getImg() != null && !Configuracao.getInstance().getImg().isEmpty()) {
            byte[] btDataFile = java.util.Base64.getDecoder().decode(Configuracao.getInstance().getImg().split(",")[1]);
            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(btDataFile));
                this.setIconImage(image.getScaledInstance(300, 300, Image.SCALE_DEFAULT));
            } catch (IOException ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.setTitle("Historico de Pedidos");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/HistoricoPedidos.html").toString());
            }
        });
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        try {
            recriarTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        if (c == null) {
            for (Pedido p : ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
                addPedido(table, p);
            }
        } else {
            for (Pedido p : c.getPedidosCliente()) {
                addPedido(table, p);
            }
        }
    }

    private void addPedido(DOMElement table, Pedido p) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-pedido", p.getCod() + "");
        DOMElement tdCod = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdStatus = browser.getDocument().createElement("td");
        DOMElement tdData = browser.getDocument().createElement("td");
        DOMElement tdVerPedido = browser.getDocument().createElement("td");
        DOMElement btVerPedido = browser.getDocument().createElement("button");
        DOMElement btImprimirPedido = browser.getDocument().createElement("button");
        btImprimirPedido.setAttribute("class", "btn btn-default");
        btImprimirPedido.setInnerText("Imprimir");
        DOMElement spanStatus = browser.getDocument().createElement("span");
        btVerPedido.setAttribute("class", "btn btn-info");
        btVerPedido.setInnerText("Ver Pedido");
        tdVerPedido.appendChild(btVerPedido);
        tdVerPedido.appendChild(btImprimirPedido);
        tdStatus.appendChild(spanStatus);
        btImprimirPedido.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                int result = JOptionPane.showConfirmDialog(null, "Deseja realmente imprimir o pedido?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    imprimirPedido(p);
                }
            }
        }, true);
        btVerPedido.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                VerPedido verPedido = new VerPedido(p);
                verPedido.abrir();
            }
        }, true);
        tdCod.setInnerText("#" + p.getCod());
        if (p.getNumeroMesa() == 0) {
            tdNome.setInnerText(p.getNomeCliente());
        } else {
            tdNome.setInnerText("Mesa " + p.getNumeroMesa());
        }
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(p.getTotal()));
        if (p.getEstadoPedido() == Pedido.EstadoPedido.Concluido) {
            spanStatus.setAttribute("class", "label label-success");
            spanStatus.setInnerText("Entregue");
        } else if (p.getEstadoPedido() == Pedido.EstadoPedido.Novo) {
            spanStatus.setAttribute("class", "label label-info");
            spanStatus.setInnerText("Ativo");
        } else if (p.getEstadoPedido() == Pedido.EstadoPedido.SaiuEntrega) {
            spanStatus.setAttribute("class", "label label-warning");
            spanStatus.setInnerText("Saiu p/ Entrega");
        } else if (p.getEstadoPedido() == Pedido.EstadoPedido.Cancelado) {
            spanStatus.setAttribute("class", "label label-danger");
            spanStatus.setInnerText("Cancelado");
        }
        if (p.getDataPedido() != null) {
            tdData.setInnerText(formatadorComAno.format(p.getDataPedido()));
        }
        tr.appendChild(tdCod);
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdData);
        tr.appendChild(tdStatus);
        tr.appendChild(tdVerPedido);
        table.appendChild(tr);
    }

    private void imprimirPedido(Pedido p) {
        new Thread() {
            public void run() {
                if (!ControleImpressao.getInstance().imprimir(p)) {
                    JOptionPane.showMessageDialog(null, "Falha ao imprimir o Pedido #" + p.getCod());
                } else {
                    if (!p.isImpresso()) {
                        p.setImpresso(true);
                        try {
                            ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).alterar(p);
                        } catch (Exception ex) {
                            p.setImpresso(false);
                            Logger.getLogger(HistoricoPedidos.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }.start();
    }
}
