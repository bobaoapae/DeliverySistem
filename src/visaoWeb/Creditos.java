/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.br.joao.Db4oGenerico;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleClientes;
import driver.WebWhatsDriver;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Chat;
import modelo.Cliente;
import modelo.Configuracao;
import modelo.EstadoDriver;
import modelo.RecargaCliente;
import utils.JXBrowserCrack;

/**

 @author SYSTEM
 */
public class Creditos extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Cliente clienteAtual;
    private SimpleDateFormat formatadorComAno = new SimpleDateFormat("dd/MM/yyyy");
    private WebWhatsDriver driver;

    public Creditos(Cliente cliente, WebWhatsDriver driver) {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
        this.clienteAtual = cliente;
        this.driver = driver;
    }

    private void init() {
        if (Configuracao.getInstance().getImg() != null && !Configuracao.getInstance().getImg().isEmpty()) {
            byte[] btDataFile = Base64.getDecoder().decode(Configuracao.getInstance().getImg().split(",")[1]);
            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(btDataFile));
                this.setIconImage(image.getScaledInstance(300, 300, Image.SCALE_DEFAULT));
            } catch (IOException ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.setTitle("Cadastrar Reserva");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/CreditosCliente.html").toString());
            }
        });
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        recriarTable();
        browser.getDocument().findElement(By.id("nomeCliente")).setInnerText(clienteAtual.getNome());
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (RecargaCliente r : clienteAtual.getRegargas()) {
            addRecarga(table, r);
        }
        browser.getDocument().findElement(By.id("saldoAtual")).setInnerText(new DecimalFormat("###,###,###.00").format(clienteAtual.getCreditosDisponiveis()));
    }

    private void addRecarga(DOMElement table, RecargaCliente recarga) {
        DOMElement tr = browser.getDocument().createElement("tr");
        DOMElement tdTipo = browser.getDocument().createElement("td");
        DOMElement spanTipo = browser.getDocument().createElement("span");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdData = browser.getDocument().createElement("td");

        if (recarga.getValorRecarga() > 0) {
            spanTipo.setAttribute("class", "label label-success");
            spanTipo.setInnerText("Recarga");
        } else {
            spanTipo.setAttribute("class", "label label-danger");
            spanTipo.setInnerText("Retirada");
        }
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(recarga.getValorRecarga()));
        tdData.setInnerText(formatadorComAno.format(recarga.getDataRecarga()));

        tdTipo.appendChild(spanTipo);
        tr.appendChild(tdTipo);
        tr.appendChild(tdValor);
        tr.appendChild(tdData);
        table.appendChild(tr);
    }

    public boolean realizarRecarga(JSObject object) {
        try {
            if (object.getProperty("valor").asNumber().getDouble() == 0) {
                JOptionPane.showMessageDialog(null, "O valor da recarga não pode ser igual a 0");
                return false;
            }
            clienteAtual.realizarRecarga(object.getProperty("valor").asNumber().getDouble());
            ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).alterar(clienteAtual);
            recriarTable();
            if (clienteAtual.getChatId() != null && !clienteAtual.getChatId().isEmpty() && driver != null && driver.getEstadoDriver() != null && driver.getEstadoDriver() == EstadoDriver.LOGGED) {
                Chat c = driver.getFunctions().getChatById(clienteAtual.getChatId());
                if (c != null) {
                    if (object.getProperty("valor").asNumber().getDouble() > 0) {
                        c.sendMessage("Você Possui Uma Nova Recarga No Valor De R$" + new DecimalFormat("###,###,###.00").format(object.getProperty("valor").asNumber().getDouble()));
                    } else {
                        c.sendMessage("Você Possui Uma Retirada De Recarga No Valor De R$" + new DecimalFormat("###,###,###.00").format(object.getProperty("valor").asNumber().getDouble()));
                    }
                    c.sendMessage("Seu Novo Saldo é De R$" + new DecimalFormat("###,###,###.00").format(clienteAtual.getCreditosDisponiveis()));
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
