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
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.dom.internal.SelectElement;
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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
import modelo.RestricaoVisibilidade;
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
        browser.executeJavaScript("$('[data-toggle=\"tooltip\"]').tooltip();");
    }

    private void alterarProduto(Produto p) {
        this.produtoAlterando = p;
        DOMInputElement nomeProduto = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement descProduto = (DOMInputElement) browser.getDocument().findElement(By.id("desc"));
        DOMInputElement valorProduto = (DOMInputElement) browser.getDocument().findElement(By.id("valor"));
        DOMInputElement onyLocal = (DOMInputElement) browser.getDocument().findElement(By.id("checkConsumoNoLocal"));
        DOMInputElement checkRegrasVisibilidade = (DOMInputElement) browser.getDocument().findElement(By.id("checkRegrasVisibilidade"));
        DOMInputElement checkVisibilidadeHorario = (DOMInputElement) browser.getDocument().findElement(By.id("checkVisibilidadeHorario"));
        DOMInputElement checkVisibilidadeDia = (DOMInputElement) browser.getDocument().findElement(By.id("checkVisibilidadeDia"));
        DOMInputElement horarioDeEdit = (DOMInputElement) browser.getDocument().findElement(By.id("horarioDe"));
        DOMInputElement horarioAteEdit = (DOMInputElement) browser.getDocument().findElement(By.id("horarioAte"));
        SelectElement diasDaSemanaEdit = (SelectElement) browser.getDocument().findElement(By.id("diasDaSemana"));
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
        nomeProduto.setValue(p.getNome());
        descProduto.setValue(p.getDescricao());
        valorProduto.setValue(new DecimalFormat("###,###,###.00").format(p.getValor()));
        onyLocal.setChecked(p.isOnlyLocal());
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
        browser.executeJavaScript("$(\"#nome\").focus()");
        browser.executeJavaScript("$(\"#cadastroProduto\").find(\"input\").trigger('change');");
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class") + "hide");
    }

    public void cancelarAlteracao() {
        this.produtoAlterando = null;
        DOMElement botaoCancelar = browser.getDocument().findElement(By.id("cancelarEdicao"));
        botaoCancelar.setAttribute("class", botaoCancelar.getAttribute("class").replaceAll("hide", ""));
    }

    private void addProduto(DOMElement table, Produto p) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-produto", p.getCod() + "");
        DOMElement tdImage = browser.getDocument().createElement("td");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdValor = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        tdBotoes.setAttribute("class", "text-center");
        {//button visibilidade
            DOMElement button = browser.getDocument().createElement("button");
            button.setAttribute("class", "btn btn-success");
            button.setAttribute("data-toggle", "tooltip");
            button.setAttribute("data-placement", "top");
            button.setAttribute("type", "button");
            DOMElement span = browser.getDocument().createElement("span");
            if (!p.isVisivel()) {
                button.setAttribute("title", "Tornar Visivel");
                span.setAttribute("class", "glyphicon glyphicon-eye-close");
            } else {
                button.setAttribute("title", "Tornar Invisivel");
                span.setAttribute("class", "glyphicon glyphicon-eye-open");
            }
            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    p.setVisivel(!p.isVisivel());
                    try {
                        ControleProdutos.getInstance(Db4oGenerico.getInstance("banco")).salvar(p);
                        recriarTable();
                    } catch (Exception ex) {
                        Logger.getLogger("LogDelivery").log(Level.SEVERE, null, ex);
                    }
                }
            }, true);
            button.appendChild(span);
            tdBotoes.appendChild(button);
        }
        {//button descrição
            DOMElement div = browser.getDocument().createElement("div");
            div.setAttribute("style", "display: inline-block;");
            if (!p.getDescricao().isEmpty()) {
                div.setAttribute("data-toggle", "modal");
                div.setAttribute("data-target", "#modalDesc");
                div.setAttribute("data-descricao", p.getDescricao());
            }
            DOMElement button = browser.getDocument().createElement("button");
            button.setAttribute("class", "btn btn-info");
            button.setAttribute("data-toggle", "tooltip");
            button.setAttribute("data-placement", "top");
            button.setAttribute("title", "Ver Descrição");
            button.setAttribute("type", "button");
            if (p.getDescricao().isEmpty()) {
                button.setAttribute("disabled", "disabled");
            }
            DOMElement span = browser.getDocument().createElement("span");
            span.setAttribute("class", "glyphicon glyphicon-info-sign");
            button.appendChild(span);
            div.appendChild(button);
            tdBotoes.appendChild(div);
        }
        {//button adicionais
            DOMElement button = browser.getDocument().createElement("button");
            button.setAttribute("class", "btn btn-primary");
            button.setAttribute("data-toggle", "tooltip");
            button.setAttribute("data-placement", "top");
            button.setAttribute("title", "Gerenciar Adicionais");
            button.setAttribute("type", "button");
            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    new GerenciadorGrupoAdicionaisProduto(p).abrir();
                }
            }, true);
            DOMElement span = browser.getDocument().createElement("span");
            span.setAttribute("class", "glyphicon glyphicon-plus");
            button.appendChild(span);
            tdBotoes.appendChild(button);
        }
        {//button editar
            DOMElement button = browser.getDocument().createElement("button");
            button.setAttribute("class", "btn btn-warning");
            button.setAttribute("data-toggle", "tooltip");
            button.setAttribute("data-placement", "top");
            button.setAttribute("title", "Editar Produto");
            button.setAttribute("type", "button");
            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    alterarProduto(p);
                }
            }, true);
            DOMElement span = browser.getDocument().createElement("span");
            span.setAttribute("class", "glyphicon glyphicon-pencil");
            button.appendChild(span);
            tdBotoes.appendChild(button);
        }
        {//button excluir
            DOMElement button = browser.getDocument().createElement("button");
            button.setAttribute("class", "btn btn-danger");
            button.setAttribute("data-toggle", "tooltip");
            button.setAttribute("data-placement", "top");
            button.setAttribute("title", "Remover Produto");
            button.setAttribute("type", "button");
            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
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
                            Logger.getLogger("LogDelivery").log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, true);
            DOMElement span = browser.getDocument().createElement("span");
            span.setAttribute("class", "glyphicon glyphicon-trash");
            button.appendChild(span);
            tdBotoes.appendChild(button);
        }
        tdNome.setInnerText(p.getNome());
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(p.getValor()));
        if (p.getImage() != null && !p.getImage().isEmpty()) {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"" + p.getImage() + "\">");
        } else {
            tdImage.setInnerHTML("<img width=\"70\" class=\"img-responsive img-circle\" src=\"assets/img/fastFood.png\">");
        }
        tr.appendChild(tdImage);
        tr.appendChild(tdNome);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Produto l = new Produto();
            if (produtoAlterando != null) {
                l = produtoAlterando;
                if (l.getValor() != object.getProperty("valor").asNumber().getDouble()) {
                    categoriaAtual.getProdutosCategoria().remove(l);
                    l = new Produto();
                    l.setAdicionaisDisponiveis(produtoAlterando.getAdicionaisDisponiveis());
                }
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
                l.setRestricaoVisibilidade(res);
            } else {
                l.setRestricaoVisibilidade(null);
            }
            if (ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(categoriaAtual)) {
                cancelarAlteracao();
                recriarTable();
                produtoAlterando = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
