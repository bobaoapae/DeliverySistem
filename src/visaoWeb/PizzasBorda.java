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
import controle.ControleBordasPizza;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.BordaPizza;
import modelo.Configuracao;
import utils.JXBrowserCrack;
import utils.Utilitarios;

/**

 @author jvbor
 */
public class PizzasBorda extends JDialog {

    private Browser browser;
    private BrowserView view;
    private BordaPizza pizzaAlterando;

    public PizzasBorda() {
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
                browser.loadURL(this.getClass().getClassLoader().getResource("html/PizzaBorda.html").toString());
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
        for (BordaPizza l : ControleBordasPizza.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            addBordaPizza(table, l);
        }
    }

    private void alterarBordaPizza(BordaPizza l) {
        this.pizzaAlterando = l;
        DOMInputElement nomeBordaPizza = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement desc = (DOMInputElement) browser.getDocument().findElement(By.id("desc"));
        DOMInputElement valor = (DOMInputElement) browser.getDocument().findElement(By.id("valor"));
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
        nomeBordaPizza.setValue(l.getNome());
        desc.setValue(l.getDescricao());
        valor.setValue(new DecimalFormat("###,###,###.00").format(l.getValor()));;
        browser.executeJavaScript("$(\"#nome\").focus()");
    }

    public void cancelarAlteracao() {
        this.pizzaAlterando = null;
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class") + "hide");
    }

    private void addBordaPizza(DOMElement table, BordaPizza l) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", l.getCod() + "");
        DOMElement tdImage = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdDescricao = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
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
                        if (ControleBordasPizza.getInstance(Db4oGenerico.getInstance("banco")).excluir(l)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(tr);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(PizzasBorda.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);
        btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                alterarBordaPizza(l);
            }
        }, true);
        tdNome.setInnerText(l.getNome());
        tdDescricao.setInnerText(l.getDescricao());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(l.getValor()));
        if (l.getImage() != null && !l.getImage().isEmpty()) {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + l.getImage() + "\">");
        } else {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fotos/pizzas/pizza-pedaco-cor.svg\">");
        }
        tr.appendChild(tdImage);
        tr.appendChild(tdNome);
        tr.appendChild(tdDescricao);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void updateBordaPizza(BordaPizza l) {
        DOMElement trBordaPizza = browser.getDocument().findElement(By.cssSelector("tr[cod-produto='" + l.getCod() + "']"));
        List<DOMElement> childs = trBordaPizza.findElements(By.tagName("td"));
        if (l.getImage() != null && !l.getImage().isEmpty()) {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + l.getImage() + "\">");
        } else {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fotos/pizzas/pizza-pedaco-cor.svg\">");
        }
        childs.get(1).setInnerText(l.getNome());
        childs.get(2).setInnerText(l.getDescricao());
        childs.get(3).setInnerText(new DecimalFormat("###,###,###.00").format(l.getValor()));
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            BordaPizza l = new BordaPizza();
            if (pizzaAlterando != null) {
                l = pizzaAlterando;
            }
            l.setNome(object.getProperty("nome").asString().getStringValue());
            l.setDescricao(object.getProperty("desc").asString().getStringValue());
            l.setValor(object.getProperty("valor").asNumber().getDouble());
            String src = ((DOMInputElement) browser.getDocument().findElement(By.id("inputFile"))).getFile();
            if (!src.isEmpty()) {
                File file = new File(src);
                l.setImage(Utilitarios.fileToBase64(file));
            }
            if (ControleBordasPizza.getInstance(Db4oGenerico.getInstance("banco")).salvar(l)) {
                if (pizzaAlterando == null) {
                    addBordaPizza(browser.getDocument().findElement(By.id("myTable")), l);
                } else {
                    updateBordaPizza(l);
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
