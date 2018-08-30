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
import controle.ControleReservas;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Configuracao;
import modelo.Reserva;
import utils.DateUtils;

/**

 @author SYSTEM
 */
public class Reservas extends JDialog {

    private Browser browser;
    private BrowserView view;
    private Reserva reservaAlterando;
    private SimpleDateFormat formatadorComAnoIngles = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat formatadorComAnoHoraIngles = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private SimpleDateFormat formatadorHora = new SimpleDateFormat("HH:mm");

    public Reservas() {
        init();
        this.setModal(true);
        this.setLocationRelativeTo(null);
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        this.add(view);
        this.setLocationRelativeTo(null);
    }

    public Reservas(Reserva r) {
        this();
        this.reservaAlterando = r;
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
        this.setTitle("Cadastrar Reserva");
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.8)), ((int) (screenSize.getHeight() * 0.8))));
        pack();
    }

    public void abrir() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/NovaReserva.html").toString());
            }
        });
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        if (this.reservaAlterando != null) {
            DOMInputElement nomeContato = (DOMInputElement) browser.getDocument().findElement(By.id("nomeReserva"));
            DOMInputElement telefoneContato = (DOMInputElement) browser.getDocument().findElement(By.id("telefoneContato"));
            DOMInputElement qtdPessoasReserva = (DOMInputElement) browser.getDocument().findElement(By.id("qtdPessoasReserva"));
            DOMInputElement diaReserva = (DOMInputElement) browser.getDocument().findElement(By.id("diaReserva"));
            DOMInputElement horaReserva = (DOMInputElement) browser.getDocument().findElement(By.id("horaReserva"));
            DOMInputElement obs = (DOMInputElement) browser.getDocument().findElement(By.id("obs"));
            nomeContato.setValue(reservaAlterando.getNomeContato());
            telefoneContato.setValue(reservaAlterando.getTelefoneContato());
            qtdPessoasReserva.setValue(reservaAlterando.getQtdPessoas() + "");
            diaReserva.setValue(formatadorComAnoIngles.format(reservaAlterando.getDataReserva()));
            horaReserva.setValue(formatadorHora.format(reservaAlterando.getDataReserva()));
            obs.setValue(reservaAlterando.getComentario());
        }
        this.setVisible(true);

    }

    public boolean realizarCadastro(JSObject object) {
        try {
            Reserva r = new Reserva();
            if (reservaAlterando != null) {
                r = reservaAlterando;
            }
            r.setNomeContato(object.getProperty("nomeContato").asString().getStringValue());
            r.setTelefoneContato(object.getProperty("telefoneContato").asString().getStringValue());
            r.setQtdPessoas(object.getProperty("qtdPessoas").asNumber().getInteger());
            r.setComentario(object.getProperty("obsReserva").asString().getStringValue());
            r.setDataReserva(formatadorComAnoHoraIngles.parse(object.getProperty("dataReserva").asString().getStringValue() + " " + object.getProperty("horaReserva").asString().getStringValue()));
            Calendar horaAtual = Calendar.getInstance();
            Calendar dataInformada = Calendar.getInstance();
            dataInformada.setTimeInMillis(r.getDataReserva().getTime());
            if (((dataInformada.get(Calendar.HOUR_OF_DAY) == Configuracao.getInstance().getHoraInicioReservas().getHour() && dataInformada.get(Calendar.MINUTE) >= Configuracao.getInstance().getHoraInicioReservas().getMinute()) || dataInformada.get(Calendar.HOUR_OF_DAY) > Configuracao.getInstance().getHoraInicioReservas().getHour()) && (DateUtils.isSameDay(dataInformada, horaAtual) || DateUtils.isAfterDay(dataInformada, horaAtual)) && ((dataInformada.get(Calendar.HOUR_OF_DAY) == horaAtual.get(Calendar.HOUR_OF_DAY) && dataInformada.get(Calendar.MINUTE) > horaAtual.get(Calendar.MINUTE)) || dataInformada.get(Calendar.HOUR_OF_DAY) > horaAtual.get(Calendar.HOUR_OF_DAY) || DateUtils.isAfterDay(r.getDataReserva(), Calendar.getInstance().getTime()))) {
                if (ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).salvar(r)) {
                    if (reservaAlterando != null) {
                        JOptionPane.showMessageDialog(null, "Alterado com Sucesso!");
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(null, "Cadastrado com Sucesso!");
                        reservaAlterando = null;
                    }
                    return true;
                }
            } else {
                JOptionPane.showMessageDialog(null, "A data ou hora informada é invalida!", "Erro!", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
