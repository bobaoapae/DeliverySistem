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
import com.teamdev.jxbrowser.chromium.dom.DOMInputElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleCombos;
import controle.ControleProdutos;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Combo;
import modelo.Configuracao;
import modelo.Produto;
import utils.JXBrowserCrack;
import utils.Utilitarios;

/**

 @author jvbor
 */
public class GerenciadorCombos extends JDialog {

    private Browser browser;
    private BrowserView view;
    private String title, defaultImg;
    private Combo comboAtual;
    private ArrayList<Produto> produtosCombo;

    public GerenciadorCombos() {
        this.title = title;
        this.defaultImg = defaultImg;
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
        produtosCombo = new ArrayList<>();
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
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/GerenciamentoCombo.html").toString());
            }
        });
        comboAtual = new Combo();
        this.setTitle("Gerenciar Combos");
        recriarTable();
        recriarTableProdutosDisponiveis();
        recriarTableProdutosCombo();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Combo p : ControleCombos.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            addCombo(table, p);
        }
    }

    public void limparProdutosCombo() {
        produtosCombo.clear();
        recriarTableProdutosCombo();
    }

    private void recriarTableProdutosCombo() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable3"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Produto p : produtosCombo) {
            addProdutoToCombo(table, p);
        }
    }

    private void recriarTableProdutosDisponiveis() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable2"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Produto p : ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            if (p instanceof Combo) {
                continue;
            }
            addProduto(table, p);
        }
    }

    private void addProduto(DOMElement table, Produto p) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", p.getCod() + "");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        DOMElement btAdd = browser.getDocument().createElement("button");
        DOMElement imgAdd = browser.getDocument().createElement("img");
        btAdd.setAttribute("type", "button");
        imgAdd.setAttribute("src", "assets/img/Icon/plus.svg");
        imgAdd.setAttribute("width", "20");
        tdBotoes.appendChild(btAdd);
        btAdd.appendChild(imgAdd);
        btAdd.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                produtosCombo.add(p);
                addProdutoToCombo(browser.getDocument().findElement(By.id("myTable3")), p);
            }
        }, true);
        tdNome.setInnerText(p.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(p.getValor()));
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void addProdutoToCombo(DOMElement table, Produto p) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", p.getCod() + "");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        DOMElement btRemover = browser.getDocument().createElement("button");
        DOMElement imgRemover = browser.getDocument().createElement("img");
        btRemover.setAttribute("type", "button");
        imgRemover.setAttribute("src", "assets/img/Icon/x.svg");
        imgRemover.setAttribute("width", "20");
        tdBotoes.appendChild(btRemover);
        btRemover.appendChild(imgRemover);
        btRemover.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                produtosCombo.remove(p);
                table.removeChild(tr);
            }
        }, true);
        tdNome.setInnerText(p.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(p.getValor()));
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void alterarCombo(Combo p) {
        this.comboAtual = p;
        produtosCombo.addAll(p.getProdutosCombo());
        DOMInputElement nomeHotDog = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement descHotDog = (DOMInputElement) browser.getDocument().findElement(By.id("desc"));
        DOMInputElement valorHotDog = (DOMInputElement) browser.getDocument().findElement(By.id("valor"));
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
        nomeHotDog.setValue(p.getNome());
        descHotDog.setValue(p.getDescricao());
        valorHotDog.setValue(new DecimalFormat("###,###,###.00").format(p.getValor()));
        browser.executeJavaScript("$(\"#nome\").focus()");
        recriarTableProdutosCombo();
    }

    public void cancelarAlteracao() {
        this.comboAtual = new Combo();
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class") + "hide");
    }

    private void addCombo(DOMElement table, Combo c) {
        DOMElement trItem = browser.getDocument().findElement(By.cssSelector("tr[cod-combo='" + c.getCod() + "']"));
        if (trItem != null) {
            updateCombo(trItem, c);
            return;
        }
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-combo", c.getCod() + "");
        DOMElement tdImage = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdProdutos = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement pProdutos = browser.getDocument().createElement("p");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        DOMElement btAlterar = browser.getDocument().createElement("button");
        btAlterar.setAttribute("class", "btn btn-warning");
        btAlterar.setInnerText("Alterar");
        DOMElement btExcluir = browser.getDocument().createElement("button");
        btExcluir.setAttribute("class", "btn btn-danger");
        btExcluir.setInnerText("Excluir");
        tdBotoes.appendChild(btAlterar);
        tdBotoes.appendChild(btExcluir);
        btExcluir.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        if (ControleCombos.getInstance(Db4oGenerico.getInstance("banco")).excluir(c)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(tr);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(GerenciadorCombos.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);
        btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                alterarCombo(c);
            }
        }, true);
        tdNome.setInnerText(c.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(c.getValor()));
        if (c.getImage() != null && !c.getImage().isEmpty()) {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + c.getImage() + "\">");
        } else {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/Icon/combo.svg\">");
        }
        String produtos = "";
        for (Produto p : c.getProdutosCombo()) {
            produtos += p.getNome() + ",";
        }
        produtos = produtos.substring(0, produtos.lastIndexOf(","));
        pProdutos.setInnerText(produtos);
        tdProdutos.appendChild(pProdutos);
        tr.appendChild(tdImage);
        tr.appendChild(tdNome);
        tr.appendChild(tdProdutos);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void updateCombo(DOMElement trItem, Combo c) {

        List<DOMElement> childs = trItem.findElements(By.tagName("td"));
        if (c.getImage() != null && !c.getImage().isEmpty()) {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + c.getImage() + "\">");
        } else {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/Icon/combo.svg\">");
        }
        childs.get(1).setInnerText(c.getNome());
        childs.get(2).getChildren().get(0).setTextContent(c.getDescricao());
        childs.get(3).setInnerText(new DecimalFormat("###,###,###.00").format(c.getValor()));
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Combo l = comboAtual;
            comboAtual.getProdutosCombo().clear();
            comboAtual.getProdutosCombo().addAll(produtosCombo);
            l.setNome(object.getProperty("nome").asString().getStringValue());
            l.setDescricao(object.getProperty("desc").asString().getStringValue());
            l.setValor(object.getProperty("valor").asNumber().getDouble());
            String src = ((DOMInputElement) browser.getDocument().findElement(By.id("inputFile"))).getFile();
            if (!src.isEmpty()) {
                File file = new File(src);
                l.setImage(Utilitarios.fileToBase64(file));
            }
            if (ControleCombos.getInstance(Db4oGenerico.getInstance("banco")).salvar(l)) {
                addCombo(browser.getDocument().findElement(By.id("myTable")), l);
                comboAtual = new Combo();
                produtosCombo.clear();
                cancelarAlteracao();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
