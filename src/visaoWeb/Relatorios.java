/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Configuracao;
import utils.JXBrowserCrack;

/**

 @author SYSTEM
 */
public class Relatorios extends JDialog {

    private Browser browser;
    private BrowserView view;
    private SimpleDateFormat formatadorComAno = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat formatadorComAnoIngles = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatadorSemAno = new SimpleDateFormat("dd/MM");

    public Relatorios() {
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
                browser.loadURL(this.getClass().getClassLoader().getResource("html/Relatorio.html").toString());
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
        this.setVisible(true);
    }

    private void recriarTable() {

    }
    
}
