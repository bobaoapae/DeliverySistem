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
import controle.ControleProdutos;
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
import modelo.AdicionalProduto;
import modelo.Configuracao;
import modelo.GrupoAdicionais;
import modelo.Produto;
import utils.JXBrowserCrack;

/**

 @author jvbor
 */
public class GerenciadorGrupoAdicionaisProduto extends JDialog {

    private Browser browser;
    private BrowserView view;
    private GrupoAdicionais grupoAtual;
    private Produto produtoAtual;

    public GerenciadorGrupoAdicionaisProduto(Produto categoriaAtual) {
        this.produtoAtual = categoriaAtual;
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
                browser.loadURL(this.getClass().getClassLoader().getResource("html/GerenciamentoGrupoAdicionais.html").toString());
            }
        });
        List<DOMElement> elementos = browser.getDocument().findElements(By.className("nomePagina"));
        for (DOMElement ele : elementos) {
            ele.setInnerText(produtoAtual.getNome());
        }
        this.setTitle("Gerenciar Adicionais " + produtoAtual.getNome());
        recriarTable();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        grupoAtual = null;
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("grupos"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (GrupoAdicionais p : produtoAtual.getAdicionaisDisponiveis()) {
            try {
                addGrupo(table, p);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addGrupo(DOMElement table, GrupoAdicionais gp) {
        DOMElement ul = browser.getDocument().createElement("ul");
        ul.setAttribute("class", "list-group col-xs-4");
        {
            {//cabecario grupo
                DOMElement li = browser.getDocument().createElement("li");
                li.setAttribute("class", "list-group-item active");
                {
                    DOMElement strong = browser.getDocument().createElement("strong");
                    strong.setInnerText(gp.getNomeGrupo());
                    DOMElement spanRemover = browser.getDocument().createElement("span");
                    spanRemover.setAttribute("class", "glyphicon glyphicon-remove pull-right");
                    spanRemover.setAttribute("title", "Remover");
                    spanRemover.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                        @Override
                        public void handleEvent(DOMEvent dome) {
                            int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION) {
                                try {
                                    produtoAtual.getAdicionaisDisponiveis().remove(gp);
                                    if (ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).salvar(produtoAtual)) {
                                        JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                                        table.removeChild(ul);
                                    }
                                } catch (Exception ex) {
                                    Logger.getLogger(GerenciadorCategorias.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }, true);
                    DOMElement spanEditar = browser.getDocument().createElement("span");
                    spanEditar.setAttribute("class", "glyphicon glyphicon-pencil pull-left");
                    spanEditar.setAttribute("title", "Editar");
                    spanEditar.setAttribute("data-toggle", "modal");
                    spanEditar.setAttribute("data-target", "#modalNovoGrupo");
                    spanEditar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                        @Override
                        public void handleEvent(DOMEvent dome) {
                            editarGrupo(gp);
                        }
                    }, true);
                    DOMElement spanNovoAdicional = browser.getDocument().createElement("span");
                    spanNovoAdicional.setAttribute("class", "glyphicon glyphicon-plus pull-right");
                    spanNovoAdicional.setAttribute("title", "Criar novo Adicional");
                    spanNovoAdicional.setAttribute("data-toggle", "modal");
                    spanNovoAdicional.setAttribute("data-target", "#modalNovoAdicional");
                    spanNovoAdicional.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                        @Override
                        public void handleEvent(DOMEvent dome) {
                            grupoAtual = gp;
                        }
                    }, true);
                    li.appendChild(strong);
                    li.appendChild(spanEditar);
                    li.appendChild(spanRemover);
                    li.appendChild(spanNovoAdicional);
                    if (!gp.getDescricaoGrupo().isEmpty()) {
                        DOMElement smallDescricao = browser.getDocument().createElement("small");
                        smallDescricao.setAttribute("class", "center-block");
                        smallDescricao.setInnerText(gp.getDescricaoGrupo());
                        li.appendChild(smallDescricao);
                    }
                }
                ul.appendChild(li);
            }
            for (AdicionalProduto adicional : gp.getAdicionais()) {
                DOMElement li = browser.getDocument().createElement("li");
                li.setAttribute("class", "list-group-item");
                {
                    DOMElement strong = browser.getDocument().createElement("strong");
                    strong.setInnerText(adicional.getNome());
                    DOMElement spanRemover = browser.getDocument().createElement("span");
                    spanRemover.setAttribute("class", "glyphicon glyphicon-remove pull-right");
                    spanRemover.setAttribute("title", "Remover");
                    spanRemover.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                        @Override
                        public void handleEvent(DOMEvent dome) {
                            int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                            if (result == JOptionPane.YES_OPTION) {
                                try {
                                    gp.getAdicionais().remove(adicional);
                                    if (ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).salvar(produtoAtual)) {
                                        JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                                        ul.removeChild(li);
                                    }
                                } catch (Exception ex) {
                                    Logger.getLogger(GerenciadorCategorias.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }, true);
                    DOMElement spanValor = browser.getDocument().createElement("span");
                    spanValor.setAttribute("class", "badge");
                    if (adicional.getValor() > 0) {
                        spanValor.setInnerText(new DecimalFormat("###,###,###.00").format(adicional.getValor()));
                    }
                    li.appendChild(strong);
                    li.appendChild(spanRemover);
                    li.appendChild(spanValor);
                    if (!adicional.getDescricao().isEmpty()) {
                        DOMElement smallDescricao = browser.getDocument().createElement("small");
                        smallDescricao.setAttribute("class", "center-block");
                        smallDescricao.setInnerText(adicional.getDescricao());
                        li.appendChild(smallDescricao);
                    }
                }
                ul.appendChild(li);
            }
        }
        table.appendChild(ul);
    }

    public void iniciarRegistro() {
        this.grupoAtual = null;
    }

    private void editarGrupo(GrupoAdicionais grupo) {
        grupoAtual = grupo;
        DOMInputElement nome = (DOMInputElement) browser.getDocument().findElement(By.id("nomeGrupo"));
        DOMElement descricaoGrupo = browser.getDocument().findElement(By.id("descricaoGrupo"));
        DOMInputElement minQtd = (DOMInputElement) browser.getDocument().findElement(By.id("minQtd"));
        DOMInputElement maxQtd = (DOMInputElement) browser.getDocument().findElement(By.id("maxQtd"));
        nome.setValue(grupo.getNomeGrupo());
        descricaoGrupo.setInnerText(grupo.getDescricaoGrupo());
        minQtd.setValue(grupo.getQtdMin() + "");
        maxQtd.setValue(grupo.getQtdMax() + "");
    }

    public boolean registroGrupo(JSObject object) {
        try {
            GrupoAdicionais l = new GrupoAdicionais();
            if (grupoAtual != null) {
                l = grupoAtual;
            } else {
                produtoAtual.getAdicionaisDisponiveis().add(l);
            }
            l.setNomeGrupo(object.getProperty("nome").asString().getStringValue());
            l.setDescricaoGrupo(object.getProperty("desc").asString().getStringValue());
            l.setQtdMin(object.getProperty("minQtd").asNumber().getInteger());
            l.setQtdMax(object.getProperty("maxQtd").asNumber().getInteger());
            if (ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).salvar(produtoAtual)) {
                recriarTable();
                grupoAtual = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean registroAdicional(JSObject object) {
        try {
            AdicionalProduto ad = new AdicionalProduto();
            ad.setNome(object.getProperty("nome").asString().getStringValue());
            ad.setDescricao(object.getProperty("desc").asString().getStringValue());
            ad.setValor(object.getProperty("valor").asNumber().getDouble());
            grupoAtual.getAdicionais().add(ad);
            if (ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).salvar(produtoAtual)) {
                recriarTable();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
