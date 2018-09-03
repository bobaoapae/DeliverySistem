/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.br.joao.Db4oGenerico;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSArray;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMInputElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.DOMOptionElement;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.dom.internal.SelectElement;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleCategorias;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Categoria;
import modelo.Configuracao;
import modelo.RestricaoVisibilidade;
import utils.JXBrowserCrack;

/**

 @author jvbor
 */
public class GerenciadorCategorias extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Categoria categoriaAtual;

    public GerenciadorCategorias() {
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
                browser.loadURL(this.getClass().getClassLoader().getResource("html/GerenciamentoCategorias.html").toString());
            }
        });
        this.setTitle("Gerenciar Categorias");
        recriarTable();
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("categorias"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        browser.executeJavaScript("$(\"#outrasCategorias\").html(\"\");");
        browser.executeJavaScript("$(\"#outrasCategoriasEdit\").html(\"\");");
        for (Categoria c : ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).getRootCategorias()) {
            addCategoria(table, c);
            browser.executeJavaScript("$(\"#outrasCategorias\").append($(\"<option />\").val(" + c.getCod() + ").text(\"" + c.getNomeCategoria() + "\"));");
            browser.executeJavaScript("$(\"#outrasCategoriasEdit\").append($(\"<option />\").val(" + c.getCod() + ").text(\"" + c.getNomeCategoria() + "\"));");
        }
    }

    private void addCategoria(DOMElement containner, Categoria c) {
        System.out.println(c);
        DOMElement ulCat = browser.getDocument().createElement("ul");
        ulCat.setAttribute("cod-categoria", c.getCod() + "");
        ulCat.setAttribute("class", "list-group");
        {
            DOMElement liCat = browser.getDocument().createElement("li");
            liCat.setAttribute("class", "list-group-item " + (c.isRootCategoria() ? "cat" : "subcat"));
            {//header
                DOMElement divDropdown = browser.getDocument().createElement("div");
                divDropdown.setAttribute("class", "dropdown");
                {//nome
                    DOMElement spanNomeCategoria = browser.getDocument().createElement("span");
                    spanNomeCategoria.setAttribute("class", "btn");
                    spanNomeCategoria.setAttribute("data-toggle", "dropdown");
                    spanNomeCategoria.setAttribute("aria-haspopup", "true");
                    spanNomeCategoria.setAttribute("aria-expanded", "false");
                    spanNomeCategoria.setAttribute("id", "drop-categoria" + c.getCod());
                    spanNomeCategoria.setInnerText(c.getNomeCategoria());
                    {//caret
                        DOMElement spanCaret = browser.getDocument().createElement("span");
                        spanCaret.setAttribute("class", "caret");
                        spanNomeCategoria.appendChild(spanCaret);
                    }
                    divDropdown.appendChild(spanNomeCategoria);
                }
                {//eye visibilidade
                    DOMElement spanEye = browser.getDocument().createElement("span");
                    if (c.isVisivel()) {
                        spanEye.setAttribute("class", "glyphicon glyphicon-eye-open");
                        spanEye.setAttribute("title", "Tornar Invisivel");
                    } else {
                        spanEye.setAttribute("class", "glyphicon glyphicon-eye-close");
                        spanEye.setAttribute("title", "Tornar Visivel");
                    }
                    spanEye.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                        @Override
                        public void handleEvent(DOMEvent dome) {
                            c.setVisivel(!c.isVisivel());
                            try {
                                ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(c);
                                recriarTable();
                            } catch (Exception ex) {
                                Logger.getLogger(GerenciadorCategorias.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }, true);
                    divDropdown.appendChild(spanEye);
                }
                {//botoes
                    DOMElement ulBotoes = browser.getDocument().createElement("ul");
                    ulBotoes.setAttribute("class", "dropdown-menu");
                    ulBotoes.setAttribute("aria-labelledby", "drop-categoria" + c.getCod());
                    {//botaoProdutos
                        DOMElement button = browser.getDocument().createElement("li");
                        button.setAttribute("class", "list-group-item list-group-item-success");
                        if (c.getCod() >= 0 || c.getCod() == -3) {
                            button.setInnerText("Produtos");
                        } else if (c.getCod() == -2) {
                            button.setInnerText("Tamanhos");
                        }
                        button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                            @Override
                            public void handleEvent(DOMEvent dome) {
                                if (c.getCod() >= 0) {
                                    new GerenciadorProdutosCategoria(c).abrir();
                                } else if (c.getCod() == -2) {
                                    new PizzasTamanho().abrir();
                                } else if (c.getCod() == -3) {
                                    new GerenciadorCombos().abrir();
                                }
                            }
                        }, true);
                        ulBotoes.appendChild(button);
                    }
                    {//botaoAdicionais
                        DOMElement button = browser.getDocument().createElement("li");
                        button.setAttribute("class", "list-group-item list-group-item-info");
                        if (c.getCod() >= 0 || c.getCod() == -3) {
                            button.setInnerText("Adicionais");
                        } else if (c.getCod() == -2) {
                            button.setInnerText("Sabores");
                        }
                        button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                            @Override
                            public void handleEvent(DOMEvent dome) {
                                if (c.getCod() >= 0 || c.getCod() == -3) {
                                    new GerenciadorGrupoAdicionais(c).abrir();
                                } else if (c.getCod() == -2) {
                                    new PizzasSabor().abrir();
                                }
                            }
                        }, true);
                        ulBotoes.appendChild(button);
                    }
                    if (c.getCod() == -2) {
                        DOMElement button = browser.getDocument().createElement("li");
                        button.setAttribute("class", "list-group-item list-group-item-primary");
                        button.setInnerText("Bordas");
                        button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                            @Override
                            public void handleEvent(DOMEvent dome) {
                                new PizzasBorda().abrir();
                            }
                        }, true);
                        ulBotoes.appendChild(button);
                    }
                    if (c.getCod() >= 0) {
                        {//botaoNovaCategoria
                            DOMElement button = browser.getDocument().createElement("li");
                            button.setAttribute("class", "list-group-item list-group-item-primary");
                            button.setAttribute("data-toggle", "modal");
                            button.setAttribute("data-target", "#modalCadastroSubCategoria");
                            button.setInnerText("Nova Subcategoria");
                            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                @Override
                                public void handleEvent(DOMEvent dome) {
                                    criarSubCategoria(c);
                                }
                            }, true);
                            ulBotoes.appendChild(button);
                        }
                        {//botaoEditar
                            DOMElement button = browser.getDocument().createElement("li");
                            button.setAttribute("class", "list-group-item list-group-item-warning");
                            button.setAttribute("data-toggle", "modal");
                            button.setAttribute("data-target", "#modalEditar");
                            button.setInnerText("Editar");
                            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                @Override
                                public void handleEvent(DOMEvent dome) {
                                    alterarCategoria(c);
                                }
                            }, true);
                            ulBotoes.appendChild(button);
                        }
                        {//botaoRemover
                            DOMElement button = browser.getDocument().createElement("li");
                            button.setAttribute("class", "list-group-item list-group-item-danger");
                            button.setInnerText("Remover");
                            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                @Override
                                public void handleEvent(DOMEvent dome) {
                                    int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                                    if (result == JOptionPane.YES_OPTION) {
                                        try {
                                            if (ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).excluir(c)) {
                                                JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                                                containner.removeChild(ulCat);
                                                if (!c.isRootCategoria()) {
                                                    c.getCategoriaPai().getCategoriaFilhas().remove(c);
                                                    ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(c.getCategoriaPai());
                                                }
                                            }
                                        } catch (Exception ex) {
                                            Logger.getLogger(GerenciadorCategorias.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                }
                            }, true);
                            ulBotoes.appendChild(button);
                        }
                    } else {
                        {//botaoEditar
                            DOMElement button = browser.getDocument().createElement("li");
                            button.setAttribute("class", "list-group-item list-group-item-warning");
                            button.setAttribute("data-toggle", "modal");
                            button.setAttribute("data-target", "#modalEditar");
                            button.setInnerText("Editar");
                            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                @Override
                                public void handleEvent(DOMEvent dome) {
                                    alterarCategoria(c);
                                }
                            }, true);
                            ulBotoes.appendChild(button);
                        }
                    }
                    divDropdown.appendChild(ulBotoes);
                }
                liCat.appendChild(divDropdown);
            }
            for (Categoria cF : c.getCategoriaFilhas()) {
                addCategoria(liCat, cF);
            }
            ulCat.appendChild(liCat);
        }
        containner.appendChild(ulCat);
    }

    private void criarSubCategoria(Categoria p) {
        this.categoriaAtual = p;
        browser.executeJavaScript("$(\"#nomeSubCat\").focus()");
    }

    public void cancelarAddSubCat() {
        this.categoriaAtual = null;
    }

    private void alterarCategoria(Categoria p) {
        this.categoriaAtual = p;
        DOMInputElement nomeCat = (DOMInputElement) browser.getDocument().findElement(By.id("nomeAlterarCategoria"));
        nomeCat.setValue(p.getNomeCategoria());
        DOMInputElement comentarioCat = (DOMInputElement) browser.getDocument().findElement(By.id("exemplosComentarioAlterar"));
        comentarioCat.setValue(p.getExemplosComentarioPedido());
        DOMInputElement ordemExibicao = (DOMInputElement) browser.getDocument().findElement(By.id("ordemExibicaoEdit"));
        ordemExibicao.setValue(p.getOrdemExibicao() + "");
        DOMInputElement checkFazEntrega = (DOMInputElement) browser.getDocument().findElement(By.id("checkFazEntregaEdit"));
        checkFazEntrega.setChecked(p.isFazEntrega());
        DOMInputElement checkQuatidade = (DOMInputElement) browser.getDocument().findElement(By.id("checkQuatidadeEdit"));
        DOMInputElement checkOutrasCategorias = (DOMInputElement) browser.getDocument().findElement(By.id("checkOutrasCategoriasEdit"));
        DOMInputElement checkRegrasVisibilidade = (DOMInputElement) browser.getDocument().findElement(By.id("checkRegrasVisibilidadeEdit"));
        DOMInputElement checkVisibilidadeHorario = (DOMInputElement) browser.getDocument().findElement(By.id("checkVisibilidadeHorarioEdit"));
        DOMInputElement checkVisibilidadeDia = (DOMInputElement) browser.getDocument().findElement(By.id("checkVisibilidadeDiaEdit"));
        DOMInputElement horarioDeEdit = (DOMInputElement) browser.getDocument().findElement(By.id("horarioDeEdit"));
        DOMInputElement horarioAteEdit = (DOMInputElement) browser.getDocument().findElement(By.id("horarioAteEdit"));
        SelectElement diasDaSemanaEdit = (SelectElement) browser.getDocument().findElement(By.id("diasDaSemanaEdit"));
        SelectElement outrasCategoriasEdit = (SelectElement) browser.getDocument().findElement(By.id("outrasCategoriasEdit"));
        if (p.isFazEntrega()) {
            checkQuatidade.setChecked(true);
            if (p.getQtdMinEntrega() > 1) {
                DOMInputElement qtdMinima = (DOMInputElement) browser.getDocument().findElement(By.id("qtdMinimaEdit"));
                qtdMinima.setValue(p.getQtdMinEntrega() + "");
            } else {
                checkQuatidade.setChecked(false);
            }
            if (p.isPrecisaPedirOutraCategoria()) {
                checkOutrasCategorias.setChecked(true);
                for (DOMOptionElement opt : outrasCategoriasEdit.getOptions()) {
                    opt.setSelected(p.getCategoriasParaPoderPedir().stream().anyMatch(o -> ((Categoria) o).getCod() == Integer.parseInt(opt.getAttribute("value"))));
                }
            } else {
                checkOutrasCategorias.setChecked(false);
            }
        } else {
            checkQuatidade.setChecked(false);
            checkOutrasCategorias.setChecked(false);
        }
        if (p.isRootCategoria()) {
            if (p.getRestricaoVisibilidade() != null) {
                checkRegrasVisibilidade.setChecked(true);
                if (p.getRestricaoVisibilidade().isRestricaoDia()) {
                    checkVisibilidadeDia.setChecked(true);
                    for (int x = 0; x < 7; x++) {
                        diasDaSemanaEdit.getOptions().get(x).setSelected(p.getRestricaoVisibilidade().getDiasSemana()[x]);
                    }
                } else {
                    for (int x = 0; x < 7; x++) {
                        diasDaSemanaEdit.getOptions().get(x).setSelected(false);
                    }
                    checkVisibilidadeDia.setChecked(false);
                }
                if (p.getRestricaoVisibilidade().isRestricaoHorario()) {
                    checkVisibilidadeHorario.setChecked(true);
                    horarioAteEdit.setValue(p.getRestricaoVisibilidade().getHorarioAte().format(DateTimeFormatter.ofPattern("HH:mm")));
                    horarioDeEdit.setValue(p.getRestricaoVisibilidade().getHorarioDe().format(DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    horarioAteEdit.setValue("");
                    horarioDeEdit.setValue("");
                    checkVisibilidadeHorario.setChecked(false);
                }
            } else {
                checkRegrasVisibilidade.setChecked(false);
                checkVisibilidadeDia.setChecked(false);
                checkVisibilidadeHorario.setChecked(false);
            }
        } else {
            for (int x = 0; x < 7; x++) {
                diasDaSemanaEdit.getOptions().get(x).setSelected(false);
            }
            horarioAteEdit.setValue("");
            horarioDeEdit.setValue("");
            checkRegrasVisibilidade.setChecked(false);
            checkVisibilidadeDia.setChecked(false);
            checkVisibilidadeHorario.setChecked(false);
        }
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
        List<DOMElement> elements = browser.getDocument().findElements(By.cssSelector("div[data-only-root='true']"));
        if (!p.isRootCategoria()) {
            for (DOMElement e : elements) {
                e.setAttribute("class", "hide");
            }
        } else {
            for (DOMElement e : elements) {
                e.removeAttribute("class");
            }
        }
        browser.executeJavaScript("$(\"#nomeAlterarCategoria\").focus()");
        browser.executeJavaScript("$(\"#form-editar-cat\").find(\"input\").trigger('change');");
    }

    public void cancelarAlteracao() {
        this.categoriaAtual = null;
        browser.executeJavaScript("$(\"#modalEditar\").modal('hide')");
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Categoria c = new Categoria();
            if (categoriaAtual != null) {
                c = categoriaAtual;
            }
            c.setNomeCategoria(object.getProperty("nome").asString().getStringValue());
            c.setExemplosComentarioPedido(object.getProperty("exemplosComentario").asString().getStringValue());
            c.setFazEntrega(object.getProperty("fazEntrega").asBoolean().getBooleanValue());
            c.setQtdMinEntrega(object.getProperty("qtdMinEntrega").asNumber().getInteger());
            c.setOrdemExibicao(object.getProperty("ordemExibicao").asNumber().getInteger());
            if (object.getProperty("precisaPedirOutraCategoria").asBoolean().getBooleanValue()) {
                c.setPrecisaPedirOutraCategoria(true);
                ArrayList<Categoria> cats = new ArrayList<>();
                JSArray arrayOutrasCategorias = object.getProperty("outrasCategorias").asArray();
                for (int x = 0; x < arrayOutrasCategorias.length(); x++) {
                    JSValue value = arrayOutrasCategorias.get(x);
                    Categoria cat = ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(Integer.parseInt(value.asString().getStringValue()));
                    if (cat != null) {
                        cats.add(cat);
                    }
                }
                c.setCategoriasParaPoderPedir(cats);
            } else {
                c.setPrecisaPedirOutraCategoria(false);
                c.setCategoriasParaPoderPedir(new ArrayList<>());
            }
            if (object.getProperty("temRegrasVisibilidade").asBoolean().getBooleanValue()) {
                RestricaoVisibilidade res = new RestricaoVisibilidade();
                if (object.getProperty("temRegraVisibilidadeHorario").asBoolean().getBooleanValue()) {
                    res.setRestricaoHorario(true);
                    res.setHorarioDe(LocalTime.parse(object.getProperty("horariosRegraVisibilidade").asObject().getProperty("horarioDe").asString().getStringValue(), DateTimeFormatter.ofPattern("HH:mm")));
                    res.setHorarioAte(LocalTime.parse(object.getProperty("horariosRegraVisibilidade").asObject().getProperty("horarioAte").asString().getStringValue(), DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    res.setRestricaoHorario(false);
                }
                if (object.getProperty("temRegraVisibilidadeDia").asBoolean().getBooleanValue()) {
                    res.setRestricaoDia(true);
                    JSArray array = object.getProperty("diasRegraVisibilidade").asArray();
                    for (int x = 0; x < array.length(); x++) {
                        res.getDiasSemana()[Integer.parseInt(array.get(x).asString().getStringValue())] = true;
                    }
                } else {
                    res.setRestricaoDia(false);
                }
                c.setRestricaoVisibilidade(res);
            } else {
                c.setRestricaoVisibilidade(null);
            }
            if (ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(c)) {
                if (categoriaAtual != null) {
                    cancelarAlteracao();
                }
                recriarTable();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean realizarCadastroSub(JSObject object) {
        try {
            Categoria c = new Categoria();
            c.setNomeCategoria(object.getProperty("nome").asString().getStringValue());
            c.setExemplosComentarioPedido(object.getProperty("exemplosComentario").asString().getStringValue());
            c.setFazEntrega(object.getProperty("fazEntrega").asBoolean().getBooleanValue());
            c.setCategoriaPai(categoriaAtual);
            if (ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(c) && ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(categoriaAtual)) {
                addCategoria(browser.getDocument().findElement(By.cssSelector("ul[cod-categoria='" + categoriaAtual.getCod() + "']")).findElement(By.tagName("li")), c);
                cancelarAddSubCat();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
