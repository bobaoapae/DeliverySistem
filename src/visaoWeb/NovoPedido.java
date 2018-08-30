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
import com.teamdev.jxbrowser.chromium.dom.DOMInputElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.dom.events.DOMKeyEvent;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleProdutos;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Configuracao;
import modelo.ItemPedido;
import modelo.Pedido;
import modelo.Pizza;
import modelo.Produto;
import utils.JXBrowserCrack;

/**

 @author SYSTEM
 */
public class NovoPedido extends JFrame implements ListChangeListener<Produto> {

    private Browser browser;
    private BrowserView view;
    private ObservableList<Produto> produtosNaTabela;
    private Pedido p;

    public NovoPedido() {
        p = new Pedido();
        init();
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
        produtosNaTabela = FXCollections.observableArrayList();
        produtosNaTabela.addListener(this);
    }

    private void init() {
        if (Configuracao.getInstance().getImg() != null && !Configuracao.getInstance().getImg().isEmpty()) {
            byte[] btDataFile = java.util.Base64.getDecoder().decode(Configuracao.getInstance().getImg().split(",")[1]);
            BufferedImage image;
            try {
                image = ImageIO.read(new ByteArrayInputStream(btDataFile));
                this.setIconImage(image.getScaledInstance(300, 300, Image.SCALE_DEFAULT));
            } catch (IOException ex) {
                Logger.getLogger(NovoPedido.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.setTitle("Realizar Novo Pedido");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/Pedido.html").toString());
            }
        });
        realizarPesquisa("");
        recriarTableItensPedido();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        DOMInputElement element = (DOMInputElement) browser.getDocument().findElement(By.id("myInput2"));
        element.addEventListener(DOMEventType.OnKeyUp, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                if (dome.isKeyboardEvent()) {
                    DOMKeyEvent event = (DOMKeyEvent) dome;
                    if (event.keyCode() == 13) {
                        if (produtosNaTabela.size() == 1) {
                            criarItemPedido(produtosNaTabela.get(0));
                        }
                    } else {
                        realizarPesquisa(element.getValue());
                    }
                }
            }
        }, rootPaneCheckingEnabled);
        this.setVisible(true);
    }

    public void realizarPesquisa(String pesquisa) {
        this.produtosNaTabela.setAll(ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorNome(pesquisa));
        Collections.sort(produtosNaTabela);
    }

    public void criarItemPedido(Produto p) {
        CriarItemPedido criarItemPedido = new CriarItemPedido(p);
        criarItemPedido.abrir();
        if (!criarItemPedido.isCancelado()) {
            this.p.addItemPedido(criarItemPedido.getItem());
            recriarTableItensPedido();
        }
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Produto p : produtosNaTabela) {
            addProduto(table, p);
        }
    }

    private void recalcularValor() {
        DOMElement element = browser.getDocument().findElement(By.id("valorTotal"));
        element.setInnerText(new DecimalFormat("###,###,###.00").format(p.getTotal()));
    }

    private void recriarTableItensPedido() {
        recalcularValor();
        DOMElement table = browser.getDocument().findElement(By.id("myTable2"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (ItemPedido item : p.getProdutos()) {
            addItemPedido(table, item);
        }
    }

    private void addProduto(DOMElement table, Produto p) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", p.getCod() + "");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        tdBotoes.setAttribute("class", "text-center");
        DOMElement btAdicionar = browser.getDocument().createElement("button");
        btAdicionar.setAttribute("class", "btn btn-success");
        btAdicionar.setInnerText("Adicionar");
        tdBotoes.appendChild(btAdicionar);
        btAdicionar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                if (!(p instanceof Pizza)) {
                    criarItemPedido(p);
                }
            }
        }, true);
        tdNome.setInnerText(p.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(p.getValor()));
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void addItemPedido(DOMElement table, ItemPedido item) {
        DOMElement tr = browser.getDocument().createElement("tr");
        DOMElement tdQtd = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdComentario = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        tdBotoes.setAttribute("class", "text-center");
        DOMElement btRemover = browser.getDocument().createElement("button");
        btRemover.setAttribute("class", "btn btn-danger");
        btRemover.setInnerText("X");
        tdBotoes.appendChild(btRemover);
        btRemover.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                table.removeChild(tr);
                p.getProdutos().remove(item);
                recalcularValor();
            }
        }, true);
        tdQtd.setInnerText(item.getQtd() + "");
        tdNome.setInnerText(item.getP().getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(item.getSubTotal()));
        tdComentario.setInnerText(item.getComentario());
        tr.appendChild(tdQtd);
        tr.appendChild(tdNome);
        tr.appendChild(tdComentario);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    @Override
    public void onChanged(Change<? extends Produto> c) {
        recriarTable();
    }

}
