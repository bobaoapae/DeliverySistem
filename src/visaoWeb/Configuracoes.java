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
import com.teamdev.jxbrowser.chromium.dom.DOMInputElement;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleConfiguracao;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Configuracao;
import utils.JXBrowserCrack;
import utils.Utilitarios;

/**

 @author SYSTEM
 */
public class Configuracoes extends JDialog {

    private Browser browser;
    private BrowserView view;
    private SimpleDateFormat formatadorComAno = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatadorComAnoIngles = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatadorSemAno = new SimpleDateFormat("dd/MM");
    private Inicio inicio;
    
    public Configuracoes(Inicio inicio) {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
        this.inicio=inicio;
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
        this.setTitle("Relatorios");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/Configuracao.html").toString());
            }
        });
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        this.setVisible(true);
    }

    public boolean saveConfig(JSObject object) {
        try {
            Configuracao c = Configuracao.getInstance();
            c.setNomeEstabelecimento(object.getProperty("nomeEstabelecimento").asString().getStringValue());
            c.setNomeBot(object.getProperty("nomeBot").asString().getStringValue());
            c.setNumeroAviso(object.getProperty("numSuporte").asString().getStringValue());
            c.setImpressaoHabilitada(object.getProperty("impressao").asBoolean().getBooleanValue());
            c.setNomeImpressora(object.getProperty("nomeImpressora").asString().getStringValue());
            c.setValorSelo(object.getProperty("valorSelo").asNumber().getDouble());
            c.setMaximoSeloPorCompra(object.getProperty("selosCompra").asNumber().getInteger());
            c.setValidadeSeloFidelidade(object.getProperty("validadeSelo").asNumber().getInteger());
            c.setReservas(object.getProperty("aceitaReservas").asBoolean().getBooleanValue());
            c.setReservasComPedidosFechados(object.getProperty("aceitaReservasComPedidosFechados").asBoolean().getBooleanValue());
            c.setAbrirFecharPedidosAutomatico(object.getProperty("checkAbrirPedidos").asBoolean().getBooleanValue());
            c.setAgendamentoDePedidos(object.getProperty("checkAgendamento").asBoolean().getBooleanValue());
            if (c.isAbrirFecharPedidosAutomatico()) {
                c.setHoraAutomaticaAbrirPedidos(LocalTime.parse(object.getProperty("horarioInicioPedidos").asString().getStringValue(), DateTimeFormatter.ofPattern("HH:mm")));
                c.setHoraAutomaticaFecharPedidos(LocalTime.parse(object.getProperty("horarioFimPedidos").asString().getStringValue(), DateTimeFormatter.ofPattern("HH:mm")));
            }
            if (c.isReservas()) {
                c.setHoraInicioReservas(LocalTime.parse(object.getProperty("horarioInicioReservas").asString().getStringValue(), DateTimeFormatter.ofPattern("HH:mm")));
            }
            String src = ((DOMInputElement) browser.getDocument().findElement(By.id("inputFile"))).getFile();
            if (!src.isEmpty()) {
                File file = new File(src);
                try {
                    c.setImg(Utilitarios.fileToBase64(file));
                } catch (IOException ex) {
                    Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            }
            if (ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(c)) {
                JOptionPane.showMessageDialog(null, "Configuração salva com sucesso");
                this.dispose();
                inicio.loadConfig();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
