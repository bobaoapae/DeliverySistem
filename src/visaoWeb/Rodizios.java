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
import controle.ControleRodizios;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
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
import modelo.Configuracao;
import modelo.Rodizio;

/**

 @author SYSTEM
 */
public class Rodizios extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Rodizio rodizioAlterando;

    public Rodizios() {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
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
        this.setTitle("Gerenciar Clientes");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/GerenciamentoRodizio.html").toString());
            }
        });
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        try {
            recriarTable();
        } catch (Exception e) {
            e.printStackTrace();
        }
        rodizioAlterando = null;
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Rodizio r : ControleRodizios.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            addRodizio(table, r);
        }
    }

    private void alterarRodizio(Rodizio r) {
        this.rodizioAlterando = r;
        DOMInputElement nome = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement valor = (DOMInputElement) browser.getDocument().findElement(By.id("valor"));
        DOMInputElement horaInicio = (DOMInputElement) browser.getDocument().findElement(By.id("horaInicio"));
        DOMElement descricao =  browser.getDocument().findElement(By.id("desc"));
        nome.setValue(r.getNome());
        valor.setValue(new DecimalFormat("###,###,###.00").format(r.getValor()));
        horaInicio.setValue(r.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
        descricao.setInnerText(r.getDescricao());
        String diasSemana = "";
        if (r.isDomingo()) {
            diasSemana += "'Domingo',";
        }
        if (r.isSegunda()) {
            diasSemana += "'Segunda',";
        }
        if (r.isTerca()) {
            diasSemana += "'Terca',";
        }
        if (r.isQuarta()) {
            diasSemana += "'Quarta',";
        }
        if (r.isQuinta()) {
            diasSemana += "'Quinta',";
        }
        if (r.isSexta()) {
            diasSemana += "'Sexta',";
        }
        if (r.isSabado()) {
            diasSemana += "'Sabado',";
        }
        diasSemana = diasSemana.substring(0, diasSemana.lastIndexOf(","));
        browser.executeJavaScript("$(\"#dias-rodizio\").val([" + diasSemana + "])");
        browser.executeJavaScript("$(\"#dias-rodizio\").multiselect(\"refresh\");");
        browser.executeJavaScript("$(\"#nome\").focus()");
        browser.executeJavaScript("$(\"#cancelarEdicao\").fadeIn();");
    }

    public void cancelarAlteracao() {
        this.rodizioAlterando = null;
        browser.executeJavaScript("$(\"#cancelarEdicao\").effect( \"explode\", {}, 300, null );");
    }

    private void addRodizio(DOMElement table, Rodizio r) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-rodizio", r.getCod() + "");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdDias = browser.getDocument().createElement("td");
        DOMElement tdHorario = browser.getDocument().createElement("td");
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
                        if (ControleRodizios.getInstance(Db4oGenerico.getInstance("banco")).excluir(r)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(tr);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Rodizios.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);
        btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                alterarRodizio(r);
            }
        }, true);
        tdNome.setInnerText(r.getNome());
        if (r.getHoraInicio() != null) {
            tdHorario.setInnerText(r.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        String diasSemana = "";
        if (r.isDomingo()) {
            diasSemana += "Domingo,";
        }
        if (r.isSegunda()) {
            diasSemana += "Segunda,";
        }
        if (r.isTerca()) {
            diasSemana += "Terca,";
        }
        if (r.isQuarta()) {
            diasSemana += "Quarta,";
        }
        if (r.isQuinta()) {
            diasSemana += "Quinta,";
        }
        if (r.isSexta()) {
            diasSemana += "Sexta,";
        }
        if (r.isSabado()) {
            diasSemana += "Sabado,";
        }
        diasSemana = diasSemana.substring(0, diasSemana.lastIndexOf(","));
        tdDias.setInnerText(diasSemana);
        tdValor.setInnerText(new DecimalFormat("###,###,###.00").format(r.getValor()));
        tr.appendChild(tdNome);
        tr.appendChild(tdDias);
        tr.appendChild(tdHorario);
        tr.appendChild(tdValor);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }

    private void updateRodizio(Rodizio r) {
        DOMElement trHotDog = browser.getDocument().findElement(By.cssSelector("tr[cod-rodizio='" + r.getCod() + "']"));
        List<DOMElement> childs = trHotDog.findElements(By.tagName("td"));
        childs.get(0).setInnerText(r.getNome());
        String diasSemana = "";
        if (r.isDomingo()) {
            diasSemana += "Domingo,";
        }
        if (r.isSegunda()) {
            diasSemana += "Segunda,";
        }
        if (r.isTerca()) {
            diasSemana += "Terca,";
        }
        if (r.isQuarta()) {
            diasSemana += "Quarta,";
        }
        if (r.isQuinta()) {
            diasSemana += "Quinta,";
        }
        if (r.isSexta()) {
            diasSemana += "Sexta,";
        }
        if (r.isSabado()) {
            diasSemana += "Sabado,";
        }
        diasSemana = diasSemana.substring(0, diasSemana.lastIndexOf(","));
        childs.get(1).setInnerText(diasSemana);
        childs.get(2).setInnerText(r.getHoraInicio().format(DateTimeFormatter.ofPattern("HH:mm")));
        childs.get(3).setInnerText(new DecimalFormat("###,###,###.00").format(r.getValor()));
    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Rodizio r = new Rodizio();
            if (rodizioAlterando != null) {
                r = rodizioAlterando;
            }
            r.setNome(object.getProperty("nome").asString().getStringValue());
            r.setDescricao(object.getProperty("desc").asString().getStringValue());
            r.setValor(object.getProperty("valor").asNumber().getDouble());
            r.setDomingo(object.getProperty("domingo").asBoolean().getBooleanValue());
            r.setSegunda(object.getProperty("segunda").asBoolean().getBooleanValue());
            r.setTerca(object.getProperty("terca").asBoolean().getBooleanValue());
            r.setQuarta(object.getProperty("quarta").asBoolean().getBooleanValue());
            r.setQuinta(object.getProperty("quinta").asBoolean().getBooleanValue());
            r.setSexta(object.getProperty("sexta").asBoolean().getBooleanValue());
            r.setSabado(object.getProperty("sabado").asBoolean().getBooleanValue());
            r.setHoraInicio(LocalTime.parse(object.getProperty("horaInicio").asString().getStringValue()));
            if (ControleRodizios.getInstance(Db4oGenerico.getInstance("banco")).salvar(r)) {
                if (rodizioAlterando == null) {
                    addRodizio(browser.getDocument().findElement(By.id("myTable")), r);
                } else {
                    cancelarAlteracao();
                    updateRodizio(r);
                }
                rodizioAlterando = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
