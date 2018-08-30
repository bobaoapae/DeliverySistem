/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import modelo.AdicionalProduto;
import modelo.Configuracao;
import modelo.ItemPedido;
import modelo.Produto;
import utils.JXBrowserCrack;

/**

 @author SYSTEM
 */
public class CriarItemPedido extends JDialog {

    private Produto p;
    private Browser browser;
    private BrowserView view;
    private ItemPedido item;
    private boolean cancelado = false;

    public CriarItemPedido(Produto p) {
        this.p = p;
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
        item = new ItemPedido();
        item.setP(p);
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
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.5)), ((int) (screenSize.getHeight() * 0.5))));
        pack();
    }

    public void concluir(JSObject object) {
        item.setComentario(object.getProperty("obs").asString().getStringValue());
        item.setQtd(object.getProperty("qtd").asNumber().getInteger());
        this.dispose();
    }

    public void cancelar() {
        this.cancelado = true;
        this.dispose();
    }

    public boolean isCancelado() {
        return cancelado;
    }

    public ItemPedido getItem() {
        return item;
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/AdicionarPedido.html").toString());
            }
        });

        this.setTitle("Adicionando " + p.getNome());
        recriarTableAdicionaisDisponiveis();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        this.setVisible(true);
    }

    private void recriarTableAdicionaisDisponiveis() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        //addAdicionalDisponivel(table, ad);
    }

    private void addAdicionalDisponivel(DOMElement table, AdicionalProduto ad) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-adicional", ad.getCod() + "");
        DOMElement tdImage = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        DOMElement btaAdicionar = browser.getDocument().createElement("button");
        btaAdicionar.setAttribute("class", "btn btn-success");
        btaAdicionar.setAttribute("type", "button");
        btaAdicionar.setInnerText("Adicionar");
        tdBotoes.appendChild(btaAdicionar);

        btaAdicionar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                item.addAdicional(ad);
            }
        }, true);
        tdNome.setInnerText(ad.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(ad.getValor()));
        if (ad.getImage() != null && !ad.getImage().isEmpty()) {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + ad.getImage() + "\">");
        } else {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fotos/adicionais/plus.svg\"\">");
        }
        tr.appendChild(tdImage);
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

}
