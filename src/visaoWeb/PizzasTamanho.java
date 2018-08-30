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
import controle.ControleCategorias;
import controle.ControlePizzas;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Categoria;
import modelo.Configuracao;
import modelo.Pizza;
import utils.JXBrowserCrack;

/**

 @author jvbor
 */
public class PizzasTamanho extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Pizza pizzaAlterando;

    public PizzasTamanho() {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
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
                browser.loadURL(this.getClass().getClassLoader().getResource("html/PizzaTamanho.html").toString());
            }
        });
        this.setTitle(browser.getDocument().findElement(By.tagName("title")).getInnerText());
        recriarTable();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
                pizzaAlterando = null;

        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Pizza l : ControlePizzas.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            addPizza(table, l);
        }
    }

    private void alterarPizza(Pizza l) {
        this.pizzaAlterando = l;
        DOMInputElement nomePizza = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement valor = (DOMInputElement) browser.getDocument().findElement(By.id("valor"));
        DOMInputElement valorEspecial = (DOMInputElement) browser.getDocument().findElement(By.id("valorEspecial"));
        DOMInputElement qtdSabores = (DOMInputElement) browser.getDocument().findElement(By.id("qtdSabores"));
        DOMInputElement qtdPedacos = (DOMInputElement) browser.getDocument().findElement(By.id("qtdPedacos"));
        DOMInputElement borda = (DOMInputElement) browser.getDocument().findElement(By.id("checkBorda"));
        DOMInputElement local = (DOMInputElement) browser.getDocument().findElement(By.id("checkLocal"));
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
        nomePizza.setValue(l.getNome());
        valor.setValue(new DecimalFormat("###,###,###.00").format(l.getValor()));
        qtdPedacos.setValue(l.getQtdPedacos() + "");
        qtdSabores.setValue(l.getQtdSabores() + "");
        valorEspecial.setValue(new DecimalFormat("###,###,###.00").format(l.getValorEspecial()));
        local.setChecked(l.isOnlyLocal());
        borda.setChecked(l.isCanHaveBorda());
        browser.executeJavaScript("$(\"#nome\").focus()");
    }

    public void cancelarAlteracao() {
        this.pizzaAlterando = null;
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class") + "hide");
    }

    private void addPizza(DOMElement table, Pizza l) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", l.getCod() + "");
        DOMElement tdImage = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdValorEspecial = browser.getDocument().createElement("td");
        DOMElement tdQtdSabores = browser.getDocument().createElement("td");
        DOMElement tdQtdPedacos = browser.getDocument().createElement("td");
        DOMElement tdBorda = browser.getDocument().createElement("td");
        DOMElement labelBorda = browser.getDocument().createElement("label");
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
                        if (ControlePizzas.getInstance(Db4oGenerico.getInstance("banco")).excluir(l)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(tr);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(PizzasTamanho.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);
        btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                alterarPizza(l);
            }
        }, true);
        tdNome.setInnerText(l.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(l.getValor()));
        if (l.getImage() != null && !l.getImage().isEmpty()) {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + l.getImage() + "\">");
        } else {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fotos/pizzas/pizza-inteira-cor.svg\">");
        }
        tdQtdPedacos.setInnerText(l.getQtdPedacos() + "");
        tdQtdSabores.setInnerText(l.getQtdSabores() + "");
        if (l.isCanHaveBorda()) {
            labelBorda.setAttribute("class", "label label-success");
            labelBorda.setInnerText("SIM");
        } else {
            labelBorda.setAttribute("class", "label label-danger");
            labelBorda.setInnerText("NÃO");
        }
        tdValorEspecial.setInnerText(new DecimalFormat("###,###,###.00").format(l.getValorEspecial()));
        tdBorda.appendChild(labelBorda);
        tr.appendChild(tdImage);
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdValorEspecial);
        tr.appendChild(tdQtdSabores);
        tr.appendChild(tdQtdPedacos);
        tr.appendChild(tdBorda);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void updatePizza(Pizza l) {
        DOMElement trPizza = browser.getDocument().findElement(By.cssSelector("tr[cod-produto='" + l.getCod() + "']"));
        List<DOMElement> childs = trPizza.findElements(By.tagName("td"));
        if (l.getImage() != null && !l.getImage().isEmpty()) {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + l.getImage() + "\">");
        } else {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fotos/pizzas/pizza-inteira-cor.svg\">");
        }
        childs.get(1).setInnerText(l.getNome());
        childs.get(2).setInnerText(new DecimalFormat("###,###,###.00").format(l.getValor()));
        childs.get(3).setInnerText(new DecimalFormat("###,###,###.00").format(l.getValorEspecial()));
        childs.get(4).setInnerText(l.getQtdSabores() + "");
        childs.get(5).setInnerText(l.getQtdPedacos() + "");
        childs.get(6).getChildren().get(0).setTextContent(l.isCanHaveBorda() ? "SIM" : "NÃO");
        DOMElement elemtn = (DOMElement) childs.get(6).getChildren().get(0);
        elemtn.setAttribute("class", l.isCanHaveBorda() ? "label label-success" : "label label-danger");
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Pizza l = new Pizza();
            if (pizzaAlterando != null) {
                l = pizzaAlterando;
            }
            Categoria catPizza = ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(-2);
            l.setCategoria(catPizza);
            l.setNome(object.getProperty("nome").asString().getStringValue());
            l.setValor(object.getProperty("valor").asNumber().getDouble());
            l.setValorEspecial(object.getProperty("valorEspecial").asNumber().getDouble());
            l.setOnlyLocal(object.getProperty("soLocal").asBoolean().getBooleanValue());
            l.setCanHaveBorda(object.getProperty("borda").asBoolean().getBooleanValue());
            l.setQtdPedacos(object.getProperty("qtdPedacos").asNumber().getInteger());
            l.setQtdSabores(object.getProperty("qtdSabores").asNumber().getInteger());
            if (ControlePizzas.getInstance(Db4oGenerico.getInstance("banco")).salvar(l)) {
                if (pizzaAlterando == null) {
                    addPizza(browser.getDocument().findElement(By.id("myTable")), l);
                } else {
                    updatePizza(l);
                }
                pizzaAlterando = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
