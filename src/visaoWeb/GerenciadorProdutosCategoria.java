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
import controle.ControleProdutos;
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
import modelo.Categoria;
import modelo.Configuracao;
import modelo.Produto;
import utils.JXBrowserCrack;
import utils.Utilitarios;

/**

 @author jvbor
 */
public class GerenciadorProdutosCategoria extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Produto produtoAlterando;
    private Categoria categoriaAtual;

    public GerenciadorProdutosCategoria(Categoria categoriaAtual) {
        this.categoriaAtual = categoriaAtual;
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
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.7)), ((int) (screenSize.getHeight() * 0.7))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/GerenciamentoProdutos.html").toString());
            }
        });
        List<DOMElement> elementos = browser.getDocument().findElements(By.className("nomePagina"));
        for (DOMElement ele : elementos) {
            ele.setInnerText(categoriaAtual.getNomeCategoria());
        }
        this.setTitle("Gerenciar " + categoriaAtual.getNomeCategoria());
        recriarTable();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        this.setVisible(true);
        produtoAlterando = null;
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Produto p : categoriaAtual.getProdutosCategoria()) {
            addProduto(table, p);
        }
    }

    private void alterarProduto(Produto p) {
        this.produtoAlterando = p;
        DOMInputElement nomeHotDog = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement descHotDog = (DOMInputElement) browser.getDocument().findElement(By.id("desc"));
        DOMInputElement valorHotDog = (DOMInputElement) browser.getDocument().findElement(By.id("valor"));
        DOMInputElement onyLocal = (DOMInputElement) browser.getDocument().findElement(By.id("checkConsumoNoLocal"));
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
        nomeHotDog.setValue(p.getNome());
        descHotDog.setValue(p.getDescricao());
        valorHotDog.setValue(new DecimalFormat("###,###,###.00").format(p.getValor()));
        onyLocal.setChecked(p.isOnlyLocal());
        browser.executeJavaScript("$(\"#nome\").focus()");
    }

    public void cancelarAlteracao() {
        this.produtoAlterando = null;
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class") + "hide");
    }

    private void addProduto(DOMElement table, Produto p) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", p.getCod() + "");
        DOMElement tdImage = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdDescricao = browser.getDocument().createElement("td");
        DOMElement pDescricao = browser.getDocument().createElement("p");
        DOMElement tdAdicionais = browser.getDocument().createElement("td");
        DOMElement buttonAdicionais = browser.getDocument().createElement("button");
        buttonAdicionais.setAttribute("class", "btn btn-primary");
        buttonAdicionais.setAttribute("title", "Adicionais do Produto");
        buttonAdicionais.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                new GerenciadorGrupoAdicionaisProduto(p).abrir();
            }
        }, true);
        buttonAdicionais.setInnerText("Adicionais");
        tdAdicionais.appendChild(buttonAdicionais);
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
                        categoriaAtual.getProdutosCategoria().remove(p);
                        if (ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).excluir(p) && ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(categoriaAtual)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(tr);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(GerenciadorProdutosCategoria.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);
        btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                alterarProduto(p);
            }
        }, true);
        tdNome.setInnerText(p.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(p.getValor()));
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + p.getImage() + "\">");
        } else {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fastFood.png\">");
        }
        pDescricao.setInnerText(p.getDescricao());
        tdDescricao.appendChild(pDescricao);
        tr.appendChild(tdImage);
        tr.appendChild(tdNome);
        tr.appendChild(tdDescricao);
        tr.appendChild(tdValor);
        tr.appendChild(tdAdicionais);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void updateProduto(Produto l) {
        DOMElement trHotDog = browser.getDocument().findElement(By.cssSelector("tr[cod-produto='" + l.getCod() + "']"));
        List<DOMElement> childs = trHotDog.findElements(By.tagName("td"));
        if (l.getImage() != null && !l.getImage().isEmpty()) {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + l.getImage() + "\">");
        } else {
            childs.get(0).setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fastFood.png\">");
        }
        childs.get(1).setInnerText(l.getNome());
        childs.get(2).getChildren().get(0).setTextContent(l.getDescricao());
        childs.get(3).setInnerText(new DecimalFormat("###,###,###.00").format(l.getValor()));
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Produto l = new Produto();
            if (produtoAlterando != null) {
                l = produtoAlterando;
            }
            l.setNome(object.getProperty("nome").asString().getStringValue());
            l.setDescricao(object.getProperty("desc").asString().getStringValue());
            l.setValor(object.getProperty("valor").asNumber().getDouble());
            l.setOnlyLocal(object.getProperty("soLocal").asBoolean().getBooleanValue());
            l.setCategoria(categoriaAtual);
            String src = ((DOMInputElement) browser.getDocument().findElement(By.id("inputFile"))).getFile();
            if (!src.isEmpty()) {
                File file = new File(src);
                l.setImage(Utilitarios.fileToBase64(file));
            }
            if (ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).salvar(l) && ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(categoriaAtual)) {
                if (produtoAlterando == null) {
                    addProduto(browser.getDocument().findElement(By.id("myTable")), l);
                } else {
                    cancelarAlteracao();
                    updateProduto(l);
                }
                produtoAlterando = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
