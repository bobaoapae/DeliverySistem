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
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControlePedidos;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.AdicionalProduto;
import modelo.Configuracao;
import modelo.ItemPedido;
import modelo.Pedido;
import utils.JXBrowserCrack;

/**

 @author SYSTEM
 */
public class VerPedido extends JDialog {

    private Browser browser;
    private BrowserView view;
    private SimpleDateFormat formatadorComAno = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatadorComAnoIngles = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatadorSemAno = new SimpleDateFormat("dd/MM");
    private Pedido p;

    public VerPedido(Pedido p) {
        init();
        this.p = p;
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
        this.setTitle("Historico de Pedidos");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/VerPedidoDelivery.html").toString());
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
        DOMElement codPedido = browser.getDocument().findElement(By.id("numeroPedido"));
        DOMInputElement nomeCliente = (DOMInputElement) browser.getDocument().findElement(By.id("nome"));
        DOMInputElement celular = (DOMInputElement) browser.getDocument().findElement(By.id("celular"));
        DOMInputElement fixo = (DOMInputElement) browser.getDocument().findElement(By.id("fixo"));
        DOMInputElement logradouro = (DOMInputElement) browser.getDocument().findElement(By.id("logradouro"));
        DOMInputElement numero = (DOMInputElement) browser.getDocument().findElement(By.id("numero"));
        DOMInputElement bairro = (DOMInputElement) browser.getDocument().findElement(By.id("bairro"));
        DOMInputElement pontoReferencia = (DOMInputElement) browser.getDocument().findElement(By.id("ref"));
        nomeCliente.setValue(p.getNomeCliente());
        celular.setValue(p.getCelular());
        fixo.setValue(p.getFixo());
        if (p.isEntrega()) {
            if (p.getEnderecoCompleto() != null) {
                logradouro.setValue(p.getEnderecoCompleto().getRua());
                numero.setValue(p.getEnderecoCompleto().getNumero() + "");
                bairro.setValue(p.getEnderecoCompleto().getBairro());
                pontoReferencia.setValue(p.getEnderecoCompleto().getReferencia());
            } else {
                logradouro.setValue(p.getEndereco());
            }
        } else {
            logradouro.setValue("RETIRADA");
            bairro.setValue("RETIRADA");
            pontoReferencia.setValue("RETIRADA");
        }
        codPedido.setInnerText(p.getCod() + "");
        this.setVisible(true);
    }

    private void recriarTable() {
        DOMElement table = browser.getDocument().findElement(By.id("listaPedidos"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        synchronized (p.getProdutos()) {
            for (ItemPedido item : p.getProdutos()) {
                addItemPedido(table, item);
            }
        }
        DOMElement valorTotal = browser.getDocument().findElement(By.id("valorTotal"));
        valorTotal.setInnerText(new DecimalFormat("###,###,###.00").format(p.getTotal()));
    }

    private void addItemPedido(DOMElement table, ItemPedido item) {
        DOMElement li = browser.getDocument().createElement("li");
        DOMElement div = browser.getDocument().createElement("div");
        DOMElement h4Qtd = browser.getDocument().createElement("h4");
        DOMElement h4Nome = browser.getDocument().createElement("h4");
        DOMElement imgRemover = browser.getDocument().createElement("img");
        DOMElement h4Valor = browser.getDocument().createElement("h4");
        DOMElement pAdicionais = browser.getDocument().createElement("p");
        DOMElement pComentario = browser.getDocument().createElement("p");
        li.setAttribute("class", "list-group-item");
        div.setAttribute("class", "clearfix");
        h4Qtd.setAttribute("class", "pull-left");
        h4Nome.setAttribute("class", "pull-left");
        imgRemover.setAttribute("class", "pull-right");
        imgRemover.setAttribute("src", "assets/img/Icon/x.svg");
        imgRemover.setAttribute("title", "Remover item");
        h4Valor.setAttribute("class", "pull-right");
        pAdicionais.setAttribute("class", "help-block");
        pComentario.setAttribute("class", "help-block");
        imgRemover.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
            @Override
            public void handleEvent(DOMEvent dome) {
                int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir o item?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                if (result == JOptionPane.YES_OPTION) {
                    synchronized (p.getProdutos()) {
                        p.getProdutos().remove(item);
                    }
                    try {
                        if (ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).alterar(p)) {
                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                            table.removeChild(li);
                            DOMElement valorTotal = browser.getDocument().findElement(By.id("valorTotal"));
                            valorTotal.setInnerText(new DecimalFormat("###,###,###.00").format(p.getTotal()));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(VerPedido.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, true);

        h4Qtd.setInnerText(item.getQtd() + "");
        h4Nome.setInnerText(item.getP().getNomeWithCategories());
        h4Valor.setInnerText(new DecimalFormat("###,###,###.00").format(item.getSubTotal()));
        String adicionais = "";
        for (int x = 0; x < item.getAdicionais().size(); x++) {
            AdicionalProduto adicional = item.getAdicionais().get(x);
            adicionais += adicional.getNome();
            if (x < item.getAdicionais().size() - 1) {
                adicionais += ",";
            }
        }
        if (adicionais.endsWith(",")) {
            adicionais = adicionais.substring(0, adicionais.lastIndexOf(","));
        }
        pAdicionais.setInnerText(adicionais);
        pComentario.setInnerText(item.getComentario());
        li.appendChild(div);
        div.appendChild(h4Qtd);
        div.appendChild(h4Nome);
        div.appendChild(imgRemover);
        div.appendChild(h4Valor);
        li.appendChild(pAdicionais);
        li.appendChild(pComentario);
        table.appendChild(li);
    }

}
