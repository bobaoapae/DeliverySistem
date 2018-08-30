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
import controle.ControleClientes;
import driver.WebWhatsDriver;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Chat;
import modelo.Cliente;
import modelo.Configuracao;
import modelo.Endereco;
import utils.JXBrowserCrack;
import utils.Utilitarios;

/**

 @author SYSTEM
 */
public class Clientes extends JDialog {
    
    private Browser browser;
    private BrowserView view;
    private Cliente clienteAlterando;
    private SimpleDateFormat formatadorComAno = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatadorComAnoIngles = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatadorSemAno = new SimpleDateFormat("dd/MM");
    private WebWhatsDriver driver;
    
    public Clientes(WebWhatsDriver driver) {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
        this.driver = driver;
    }
    
    private void init() {
        if (Configuracao.getInstance().getImg() != null && !Configuracao.getInstance().getImg().isEmpty()) {
            byte[] btDataFile = Base64.getDecoder().decode(Configuracao.getInstance().getImg().split(",")[1]);
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
                browser.loadURL(this.getClass().getClassLoader().getResource("html/Clientes.html").toString());
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
        clienteAlterando = null;
        this.setVisible(true);
    }
    
    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        for (Cliente c : ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()) {
            addCliente(table, c);
        }
    }
    
    private void alterarCliente(Cliente c) {
        this.clienteAlterando = c;
        DOMInputElement nome = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement celular = (DOMInputElement) browser.getDocument().findElement(By.id("celular"));
        DOMInputElement fixo = (DOMInputElement) browser.getDocument().findElement(By.id("fixo"));
        DOMInputElement rua = (DOMInputElement) browser.getDocument().findElement(By.id("rua"));
        DOMInputElement bairro = (DOMInputElement) browser.getDocument().findElement(By.id("bairro"));
        DOMInputElement numero = (DOMInputElement) browser.getDocument().findElement(By.id("numero"));
        DOMInputElement referencia = (DOMInputElement) browser.getDocument().findElement(By.id("ref"));
        DOMInputElement dataNascimento = (DOMInputElement) browser.getDocument().findElement(By.id("dataNascimento"));
        nome.setValue(c.getNome());
        celular.setValue(c.getTelefoneMovel());
        fixo.setValue(c.getTelefoneFixo());
        if (c.getEnderecoCompleto() != null) {
            rua.setValue(c.getEnderecoCompleto().getRua());
            bairro.setValue(c.getEnderecoCompleto().getBairro());
            numero.setValue(c.getEnderecoCompleto().getNumero() + "");
            referencia.setValue(c.getEnderecoCompleto().getReferencia());
        }
        if (c.getDataAniversario() != null) {
            dataNascimento.setValue(formatadorComAnoIngles.format(c.getDataAniversario()));
        }
        browser.executeJavaScript("$(\"#nome\").focus()");
        browser.executeJavaScript("$(\"#cancelarEdicao\").fadeIn();");
    }
    
    public void cancelarAlteracao() {
        this.clienteAlterando = null;
        browser.executeJavaScript("$(\"#cancelarEdicao\").effect( \"explode\", {}, 300, null );");
    }
    
    private void addCliente(DOMElement table, Cliente c) {
        DOMElement tr = browser.getDocument().createElement("tr");
        tr.setAttribute("cod-cliente", c.getCod() + "");
        DOMElement tdNome = browser.getDocument().createElement("td");
        DOMElement tdCelular = browser.getDocument().createElement("td");
        DOMElement tdFixo = browser.getDocument().createElement("td");
        DOMElement tdEndereco = browser.getDocument().createElement("td");
        DOMElement btEndereco = browser.getDocument().createElement("button");
        btEndereco.setAttribute("class", "btn btn-info");
        btEndereco.setInnerText("Endereço");
        DOMElement btCreditos = browser.getDocument().createElement("button");
        btCreditos.setAttribute("class", "btn btn-success");
        btCreditos.setInnerText("Créditos");
        DOMElement btPedidos = browser.getDocument().createElement("button");
        btPedidos.setAttribute("class", "btn btn-info");
        btPedidos.setInnerText("Pedidos");
        DOMElement tdAniversario = browser.getDocument().createElement("td");
        DOMElement tdBotoes = browser.getDocument().createElement("td");
        DOMElement btAlterar = browser.getDocument().createElement("button");
        btAlterar.setAttribute("class", "btn btn-warning");
        btAlterar.setInnerText("Alterar");
        DOMElement btExcluir = browser.getDocument().createElement("button");
        btExcluir.setAttribute("class", "btn btn-danger");
        btExcluir.setInnerText("Excluir");
        tdBotoes.appendChild(btCreditos);
        tdBotoes.appendChild(btPedidos);
        tdBotoes.appendChild(btAlterar);
        tdBotoes.appendChild(btExcluir);
        btExcluir.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    try {
                        if (ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).excluir(c)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(tr);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(Clientes.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);
        btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                alterarCliente(c);
            }
        }, true);
        btPedidos.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                new HistoricoPedidos(c).abrir();
            }
        }, true);
        btCreditos.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                new Creditos(c, driver).abrir();
            }
        }, true);
        tdNome.setInnerText(c.getNome());
        if (c.getChatId().isEmpty()) {
            tdCelular.setInnerText(c.getTelefoneMovel());
        } else {
            DOMElement imgWpp = browser.getDocument().createElement("img");
            DOMElement numberWpp = browser.getDocument().createElement("span");
            numberWpp.setInnerText(c.getTelefoneMovel());
            imgWpp.setAttribute("width", "15");
            imgWpp.setAttribute("src", "assets/img/Icon/wpp.png");
            imgWpp.setAttribute("style", "margin-right: 5px");
            imgWpp.setAttribute("title", "Clique para enviar uma mensagem via WhatsApp");
            imgWpp.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    System.out.println("Chat to >" + c);
                }
            }, true);
            tdCelular.appendChild(imgWpp);
            tdCelular.appendChild(numberWpp);
        }
        tdFixo.setInnerText(c.getTelefoneFixo());
        btEndereco.setAttribute("data-toggle", "modal");
        btEndereco.setAttribute("data-target", "#exampleModalCenter");
        btEndereco.setAttribute("data-endereco", c.getEndereco().isEmpty() ? " " : c.getEndereco());
        tdEndereco.appendChild(btEndereco);
        if (c.getDataAniversario() != null) {
            tdAniversario.setInnerText(formatadorSemAno.format(c.getDataAniversario()));
        }
        tr.appendChild(tdNome);
        tr.appendChild(tdCelular);
        tr.appendChild(tdFixo);
        tr.appendChild(tdEndereco);
        tr.appendChild(tdAniversario);
        tr.appendChild(tdBotoes);
        table.appendChild(tr);
    }
    
    private void updateCliente(Cliente c) {
        DOMElement trHotDog = browser.getDocument().findElement(By.cssSelector("tr[cod-cliente='" + c.getCod() + "']"));
        List<DOMElement> childs = trHotDog.findElements(By.tagName("td"));
        childs.get(0).setInnerText(c.getNome());
        if (c.getChatId().isEmpty()) {
            childs.get(1).setInnerText(c.getTelefoneMovel());
        } else {
            if (childs.get(1).getChildren().size() == 2) {
                childs.get(1).getChildren().get(1).setTextContent(c.getTelefoneMovel());
            } else {
                DOMElement imgWpp = browser.getDocument().createElement("img");
                DOMElement numberWpp = browser.getDocument().createElement("span");
                numberWpp.setInnerText(c.getTelefoneMovel());
                imgWpp.setAttribute("width", "15");
                imgWpp.setAttribute("src", "assets/img/Icon/wpp.png");
                imgWpp.setAttribute("style", "margin-right: 5px");
                imgWpp.setAttribute("title", "Clique para enviar uma mensagem via WhatsApp");
                imgWpp.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                    @Override
                    public void handleEvent(DOMEvent dome) {
                        System.out.println("Chat to >" + c);
                    }
                }, true);
                childs.get(1).setInnerText("");
                childs.get(1).appendChild(imgWpp);
                childs.get(1).appendChild(numberWpp);
            }
        }
        childs.get(2).setInnerText(c.getTelefoneFixo());
        childs.get(3).setInnerText(c.getEndereco());
        childs.get(4).setInnerText(formatadorSemAno.format(c.getDataAniversario()));
    }
    
    public boolean realizarCadastro(JSObject object) {
        try {
            Cliente c = new Cliente();
            if (clienteAlterando != null) {
                c = clienteAlterando;
            }
            c.setNome(object.getProperty("nome").asString().getStringValue());
            Endereco e = new Endereco();
            e.setRua(object.getProperty("rua").asString().getStringValue());
            e.setBairro(object.getProperty("bairro").asString().getStringValue());
            e.setReferencia(object.getProperty("ref").asString().getStringValue());
            e.setNumero(object.getProperty("numero").asNumber().getInteger());
            c.setEnderecoCompleto(e);
            c.setTelefoneMovel(object.getProperty("celular").asString().getStringValue());
            c.setTelefoneFixo(object.getProperty("fixo").asString().getStringValue());
            c.setCadastroRealizado(true);
            c.setDataAniversario(formatadorComAnoIngles.parse(object.getProperty("dataNascimento").asString().getStringValue()));
            Chat chat = Inicio.driver.getFunctions().getChatByNumber("55" + Utilitarios.replaceAllNoDigit(c.getTelefoneMovel()));
            if (chat != null) {
                c.setChatId(chat.getId());
            } else {
                c.setChatId("");
            }
            if (ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).salvar(c)) {
                if (clienteAlterando == null) {
                    addCliente(browser.getDocument().findElement(By.id("myTable")), c);
                } else {
                    cancelarAlteracao();
                    updateCliente(c);
                }
                clienteAlterando = null;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
