/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.br.joao.Db4ObjectSaveGeneric;
import com.br.joao.Db4oGenerico;
import com.db4o.Db4oEmbedded;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.TTransient;
import com.db4o.ta.TransparentActivationSupport;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.ProtocolService;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMInputElement;
import com.teamdev.jxbrowser.chromium.dom.DOMNode;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEvent;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventListener;
import com.teamdev.jxbrowser.chromium.dom.events.DOMEventType;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControleBackups;
import controle.ControleCategorias;
import controle.ControleChatsAsync;
import controle.ControleClientes;
import controle.ControleConfiguracao;
import controle.ControleImpressao;
import controle.ControlePedidos;
import controle.ControleReservas;
import driver.WebWhatsDriver;
import handlersBot.HandlerBoasVindas;
import handlersBot.HandlerBotDelivery;
import handlersBot.HandlerPedidoConcluido;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import modelo.ActionOnErrorInDriver;
import modelo.ActionOnLowBaterry;
import modelo.Categoria;
import modelo.Chat;
import modelo.ChatBot;
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.EstadoDriver;
import modelo.Mesa;
import modelo.NewChatObserver;
import modelo.Pedido;
import modelo.Reserva;
import org.apache.commons.lang3.exception.ExceptionUtils;
import utils.JXBrowserCrack;
import utils.ProtocoloHandlerJar;
import utils.Utilitarios;

/**

 @author jvbor
 */
public class Inicio extends JFrame {

    private Browser browser;
    private BrowserView view;
    private JTabbedPane tabbedPane;
    private JPanel panelWhatsapp;
    public static WebWhatsDriver driver;
    private ScheduledExecutorService executores = Executors.newSingleThreadScheduledExecutor();
    private ArrayList<Reserva> reservasJaAdicionadas = new ArrayList();
    private ArrayList<Pedido> pedidosJaAdicionados = new ArrayList();
    private ArrayList<Mesa> mesasEmAberto = new ArrayList<>();
    private Logger logger;

    public Inicio() {
        init();
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        ProtocolService protocolService = browser.getContext().getProtocolService();
        protocolService.setProtocolHandler("jar", new ProtocoloHandlerJar());
        this.tabbedPane.add("Sistema", view);
        this.tabbedPane.add("WhatsApp", panelWhatsapp);
        this.setVisible(true);
        logger = Logger.getLogger("DeliveryError");
        FileHandler fh;
        try {
            fh = new FileHandler("LogDelivery.txt", true);
            logger.addHandler(fh);
            logger.addHandler(new Handler() {
                @Override
                public void publish(LogRecord lr) {
                    try {
                        if (driver != null && driver.getEstadoDriver() != null && driver.getEstadoDriver() == EstadoDriver.LOGGED) {
                            Chat c = driver.getFunctions().getChatByNumber("554491050665");
                            if (c != null) {
                                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Erro-> " + ExceptionUtils.getStackTrace(lr.getThrown()));
                            }
                        }
                    } catch (Exception ex) {

                    }
                }

                @Override
                public void flush() {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void close() throws SecurityException {
                    //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
            logger.addHandler(new StreamHandler(System.out, new SimpleFormatter()));
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                logger.log(Level.SEVERE, null, e);
            }
        });
        initWpp();
        criarConfiguracoesBanco();
        if (ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).isEmpty()) {
            loadConfigPage();
        } else {
            loadConfig();
            loadIndex();
        }
    }

    private void criarConfiguracoesBanco() {
        EmbeddedConfiguration configClient = Db4oEmbedded.newConfiguration();
        configClient.common().detectSchemaChanges(true);
        configClient.common().exceptionsOnNotStorable(true);
        configClient.common().internStrings(true);
        configClient.common().allowVersionUpdates(true);
        configClient.common().automaticShutDown(true);
        configClient.common().activationDepth(99999999);
        configClient.common().updateDepth(99999999);
        configClient.common().add(new TransparentActivationSupport());
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).cascadeOnUpdate(true);
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).cascadeOnActivate(true);
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).indexed(true);
        configClient.common().objectClass(Db4ObjectSaveGeneric.class).objectField("cod").indexed(true);
        configClient.common().objectClass(Clientes.class).objectField("chatId").indexed(true);
        configClient.common().objectClass(Pedido.class).objectField("estadoPedido").indexed(true);
        configClient.common().objectClass(Pedido.class).objectField("numeroMesa").indexed(true);
        configClient.common().objectClass(Collections.synchronizedList(new ArrayList<>()).getClass()).translate(new TTransient());
        configClient.common().objectClass(FXCollections.synchronizedObservableList(FXCollections.observableList(new ArrayList())).getClass()).translate(new TTransient());
        Db4oGenerico.getInstance("banco", configClient);

        Categoria catPizza = ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(-2);
        if (catPizza == null) {
            catPizza = new Categoria();
            catPizza.setNomeCategoria("Pizzas 🍕");
            catPizza.setCod(-2);
            catPizza.setOrdemExibicao(1);
            try {
                ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(catPizza);
            } catch (Exception ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Categoria catCombos = ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).pesquisarPorCodigo(-3);
        if (catCombos == null) {
            catPizza = new Categoria();
            catPizza.setNomeCategoria("Combos 🍔🍟");
            catPizza.setCod(-3);
            catPizza.setOrdemExibicao(0);
            try {
                ControleCategorias.getInstance(Db4oGenerico.getInstance("banco")).salvar(catPizza);
            } catch (Exception ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        ControleBackups.getInstance(Db4oGenerico.getInstance("banco")).realizarBackup();

    }

    public void alterarTempos(JSObject object) {
        Configuracao.getInstance().setTempoMedioEntrega(object.getProperty("tempoMedioEntrega").asNumber().getInteger());
        Configuracao.getInstance().setTempoMedioRetirada(object.getProperty("tempoMedioRetirada").asNumber().getInteger());
        try {
            ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
        } catch (Exception ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
        browser.getDocument().findElement(By.id("tempoMedioRetirada")).setInnerText(Configuracao.getInstance().getTempoMedioRetirada() + "");
        browser.getDocument().findElement(By.id("tempoMedioEntrega")).setInnerText(Configuracao.getInstance().getTempoMedioEntrega() + "");
        ((DOMInputElement) browser.getDocument().findElement(By.id("tempRet"))).setValue(Configuracao.getInstance().getTempoMedioRetirada() + "");
        ((DOMInputElement) browser.getDocument().findElement(By.id("tempEntre"))).setValue(Configuracao.getInstance().getTempoMedioEntrega() + "");
    }

    //<editor-fold defaultstate="collapsed" desc="iniciar chat bot">
    private void initWpp() {
        Runnable actionOnLogin = () -> {
            tabbedPane.setSelectedIndex(1);
            for (Chat chat : driver.getFunctions().getAllNewChats()) {
                ControleChatsAsync.getInstance().addChat(chat);
            }
            driver.getFunctions().addListennerToNewChat(new NewChatObserver() {
                @Override
                public void onNewChat(Chat chat) {
                    ControleChatsAsync.getInstance().addChat(chat);
                }
            });
            JOptionPane.showMessageDialog(null, "Bot Logado Com Sucesso");
        };
        Logger logger = Logger.getLogger("DriverError");
        FileHandler fh;
        try {
            fh = new FileHandler("LogDriver.txt", true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ActionOnErrorInDriver actionOnErrorInDriver = new ActionOnErrorInDriver() {
            @Override
            public void run(Exception excptn) {
                logger.log(Level.SEVERE, excptn.getMessage(), excptn);
            }
        };
        /*
         onReceiveMessage = new ActionOnReceiveMessage() { @Override public void
         run(MessageGroup mg) { controle.processNewMsgs(mg); } };
         */
        ActionOnLowBaterry onLowBaterry = new ActionOnLowBaterry() {
            @Override
            public void run(long i) {
                JOptionPane.showMessageDialog(rootPane, "O celular ao qual o bot está conectado está com a bateria baixa, por favor conecte ao carregador", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        };
        Runnable onDisconnect = () -> {
            JOptionPane.showMessageDialog(rootPane, "O celular ao qual o bot está conectado está sem acesso a internet ou desligado!", "Aviso", JOptionPane.WARNING_MESSAGE);

        };
        try {
            driver = new WebWhatsDriver(panelWhatsapp, "Bot", false, actionOnLogin, null, actionOnErrorInDriver, onLowBaterry, onDisconnect);
            revalidate();
            //driver.setOnReceiveMsg(onReceiveMessage);
        } catch (MalformedURLException ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Iniciar Tela">
    private void init() {
        this.setTitle("Delivery System");
        this.setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setMinimumSize(new Dimension(((int) (screenSize.getWidth() * 0.9)), ((int) (screenSize.getHeight() * 0.9))));
        this.tabbedPane = new JTabbedPane();
        this.add(tabbedPane);
        this.panelWhatsapp = new JPanel(new BorderLayout());
        pack();
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                finalizar();
            }

        });
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Abrir Pagina Inicial">
    private void loadIndex() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/_index.html").toString());
            }
        });
        view.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent ke) {
                if (ke.isControlDown() && ke.getKeyCode() == KeyEvent.VK_F1) {
                    novoPedido();
                }
            }

        });

        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
        window.asObject().setProperty("Combos", new GerenciadorCombos());
        window.asObject().setProperty("Rodizios", new Rodizios());
        window.asObject().setProperty("Reservas", new Reservas());
        window.asObject().setProperty("HistoricoPedidos", new HistoricoPedidos());
        window.asObject().setProperty("Clientes", new Clientes(driver));
        window.asObject().setProperty("PizzasTamanho", new PizzasTamanho());
        window.asObject().setProperty("PizzasSabor", new PizzasSabor());
        window.asObject().setProperty("PizzasBorda", new PizzasBorda());
        window.asObject().setProperty("Categorias", new GerenciadorCategorias());
        browser.getDocument().findElement(By.id("nomeEmpresa")).setInnerText(Configuracao.getInstance().getNomeEstabelecimento());
        if (Configuracao.getInstance().getImg() != null && !Configuracao.getInstance().getImg().isEmpty()) {
            browser.getDocument().findElement(By.id("logoEmpresa")).setAttribute("src", Configuracao.getInstance().getImg());
        }
        browser.getDocument().findElement(By.id("mesasAbertas")).setInnerText("");
        browser.getDocument().findElement(By.id("pedidosAtivos")).setInnerText("");
        browser.getDocument().findElement(By.id("pedidosSaiuEntrega")).setInnerText("");
        browser.getDocument().findElement(By.id("tempoMedioRetirada")).setInnerText(Configuracao.getInstance().getTempoMedioRetirada() + "");
        browser.getDocument().findElement(By.id("tempoMedioEntrega")).setInnerText(Configuracao.getInstance().getTempoMedioEntrega() + "");
        ((DOMInputElement) browser.getDocument().findElement(By.id("tempRet"))).setValue(Configuracao.getInstance().getTempoMedioRetirada() + "");
        ((DOMInputElement) browser.getDocument().findElement(By.id("tempEntre"))).setValue(Configuracao.getInstance().getTempoMedioEntrega() + "");
        gerenciarPedidos();
        gerenciarReservas();
        atualizarValoresIndex();
        executores.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                if (Configuracao.getInstance().isAbrirFecharPedidosAutomatico()) {
                    LocalTime horaAtual = LocalTime.now();
                    if (!Configuracao.getInstance().isOpenPedidos()) {
                        if (Configuracao.getInstance().getHoraAutomaticaAbrirPedidos().isAfter(Configuracao.getInstance().getHoraAutomaticaFecharPedidos())) {
                            if (!(horaAtual.isBefore(Configuracao.getInstance().getHoraAutomaticaAbrirPedidos()) && horaAtual.isAfter(Configuracao.getInstance().getHoraAutomaticaFecharPedidos()))) {
                                new Thread() {
                                    public void run() {
                                        abrirPedidos();
                                    }
                                }.start();
                            }
                        } else {
                            if (horaAtual.isAfter(Configuracao.getInstance().getHoraAutomaticaAbrirPedidos()) && horaAtual.isBefore(Configuracao.getInstance().getHoraAutomaticaFecharPedidos())) {
                                new Thread() {
                                    public void run() {
                                        abrirPedidos();
                                    }
                                }.start();
                            }
                        }
                    } else {
                        if (Configuracao.getInstance().getHoraAutomaticaFecharPedidos().isBefore(Configuracao.getInstance().getHoraAutomaticaAbrirPedidos())) {
                            if ((horaAtual.isBefore(Configuracao.getInstance().getHoraAutomaticaAbrirPedidos()) && horaAtual.isAfter(Configuracao.getInstance().getHoraAutomaticaFecharPedidos()))) {
                                new Thread() {
                                    public void run() {
                                        internalFecharPedidos();
                                    }
                                }.start();
                            }
                        } else {
                            if (horaAtual.isAfter(Configuracao.getInstance().getHoraAutomaticaFecharPedidos()) || horaAtual.isBefore(Configuracao.getInstance().getHoraAutomaticaAbrirPedidos())) {
                                new Thread() {
                                    public void run() {
                                        internalFecharPedidos();
                                    }
                                }.start();
                            }
                        }
                    }
                }
            }
        }, 0, 10, TimeUnit.SECONDS);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Abrir Pagina de Configuração">
    private void loadConfigPage() {
        Browser.invokeAndWaitFinishLoadingMainFrame(browser, new Callback<Browser>() {
            @Override
            public void invoke(Browser arg0) {
                browser.loadURL(this.getClass().getClassLoader().getResource("html/Configuracao.html").toString());
            }
        });
        browser.executeJavaScript("window.java = {};");
        JSValue window = browser.executeJavaScriptAndReturnValue("window.java");
        window.asObject().setProperty("atual", this);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Realizar Novo Pedido">
    public void novoPedido() {
        NovoPedido p = new NovoPedido();
        p.abrir();
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gerenciar Pedidos Ativos">
    private void gerenciarPedidos() {
        executores.scheduleWithFixedDelay(() -> {
            boolean flag = false;
            try {
                for (Pedido p : ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosAtivos()) {
                    if (!pedidosJaAdicionados.contains(p)) {
                        pedidosJaAdicionados.add(p);
                        flag = true;
                        if (p.getNumeroMesa() == 0) {
                            try {
                                addPedidosRetiradaEntrega(p);
                            } catch (Exception ex) {
                                logger.log(Level.SEVERE, null, ex);
                            }
                        } else {
                            Mesa m = new Mesa(p.getNumeroMesa());
                            if (mesasEmAberto.contains(m)) {
                                m = mesasEmAberto.get(mesasEmAberto.indexOf(m));
                                synchronized (m.getPedidos()) {
                                    m.getPedidos().add(p);
                                }
                            } else {
                                mesasEmAberto.add(m);
                                synchronized (m.getPedidos()) {
                                    m.getPedidos().add(p);
                                }
                                addMesa(m);
                            }
                        }
                    }
                }
                if (flag) {
                    atualizarValoresIndex();
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Atualizar Valores Mini - Relatório da Index">
    private void atualizarValoresIndex() {
        try {
            LinkedHashMap<String, Integer> ranking = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getTopVendidosMes(5);
            String series = "";
            for (Map.Entry<String, Integer> entry : ranking.entrySet()) {
                series += "{"
                        + "name: '" + entry.getKey() + "',"
                        + "data: [" + entry.getValue() + "]"
                        + "},";
            }
            if (series.endsWith(",")) {
                series = series.substring(0, series.lastIndexOf(","));
            }
            String script = "Highcharts.chart('topVendidos', {\n"
                    + "                                chart: {\n"
                    + "                                    type: 'column'\n"
                    + "                                },\n"
                    + "                                title: {\n"
                    + "                                    text: 'Top 5 vendidos do mês'\n"
                    + "                                },\n"
                    + "                                xAxis: {\n"
                    + "                                    categories: [\n"
                    + "                                        'Agosto'\n"
                    + "                                    ],\n"
                    + "                                    minPadding: 0,\n"
                    + "                                    maxPadding: 0,\n"
                    + "                                    crosshair: true\n"
                    + "                                },\n"
                    + "                                yAxis: {\n"
                    + "                                    min: 0,\n"
                    + "                                    title: {\n"
                    + "                                        text: 'Quantidade'\n"
                    + "                                    }\n"
                    + "                                },\n"
                    + "                                tooltip: {\n"
                    + "                                    headerFormat: '<span style=\"font-size:10px\">{point.key}</span><table>',\n"
                    + "                                    pointFormat: '<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>' +\n"
                    + "                                            '<td style=\"padding:0\"><b>{point.y}</b></td></tr>',\n"
                    + "                                    footerFormat: '</table>',\n"
                    + "                                    shared: true,\n"
                    + "                                    useHTML: true\n"
                    + "                                },\n"
                    + "                                plotOptions: {\n"
                    + "                                    column: {\n"
                    + "                                        pointPadding: 0.2,\n"
                    + "                                        borderWidth: 0\n"
                    + "                                    },\n"
                    + "                                    series: {\n"
                    + "                                        animation: true,\n"
                    + "                                        dataLabels: {\n"
                    + "                                            enabled: true\n"
                    + "                                        }\n"
                    + "                                    }\n"
                    + "                                },\n"
                    + "                                series: [" + series + "]\n"
                    + "                            });";
            System.out.println(script);
            browser.executeJavaScript(script);
            List<Pedido> pedidosDoDia = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosDeliveryDoDia();
            int ativos = 0;
            int cancelados = 0;
            int entreges = 0;
            for (Pedido p : pedidosDoDia) {
                if (p.getEstadoPedido() == Pedido.EstadoPedido.Cancelado) {
                    cancelados++;
                } else if (p.getEstadoPedido() == Pedido.EstadoPedido.Concluido) {
                    entreges++;
                } else if (p.getEstadoPedido() == Pedido.EstadoPedido.Novo || p.getEstadoPedido() == Pedido.EstadoPedido.AguardandoRetirada) {
                    ativos++;
                }
            }
            DOMElement pAtivos = browser.getDocument().findElement(By.id("qtdPedidosAtivos"));
            DOMElement pEntregues = browser.getDocument().findElement(By.id("qtdPedidosEntregues"));
            DOMElement pCancelados = browser.getDocument().findElement(By.id("qtdPedidosCancelados"));
            pAtivos.setInnerText(ativos + "");
            pEntregues.setInnerText(entreges + "");
            pCancelados.setInnerText(cancelados + "");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Adicionar Div dos Pedidos Retirada/Entrega">
    private void addPedidosRetiradaEntrega(Pedido p) {
        try {
            if (!p.isImpresso()) {
                imprimirPedido(p);
            }
            final DOMElement containnerPedidos;
            if (null == p.getEstadoPedido()) {
                throw new Exception("Estado do Pedido invalido");
            } else {
                switch (p.getEstadoPedido()) {
                    case Novo:
                        containnerPedidos = browser.getDocument().findElement(By.id("pedidosAtivos"));
                        break;
                    case SaiuEntrega:
                        containnerPedidos = browser.getDocument().findElement(By.id("pedidosSaiuEntrega"));
                        break;
                    default:
                        throw new Exception("Estado do Pedido invalido");
                }
            }
            DOMElement divPedido = browser.getDocument().createElement("div");
            DOMElement divPanel = browser.getDocument().createElement("div");
            DOMElement divPanelHeading = browser.getDocument().createElement("div");
            DOMElement divPanelBody = browser.getDocument().createElement("div");
            DOMElement divClearFix = browser.getDocument().createElement("div");
            DOMElement h3NrPedido = browser.getDocument().createElement("h3");
            DOMElement spanCancelarPedido = browser.getDocument().createElement("span");
            DOMElement h2TotalPedido = browser.getDocument().createElement("h2");
            DOMElement imgPedido = browser.getDocument().createElement("img");
            DOMElement pNomeRetirada = browser.getDocument().createElement("p");
            DOMElement buttonVerPedido = browser.getDocument().createElement("button");
            DOMElement buttonConcluido = browser.getDocument().createElement("button");
            DOMElement buttonImprimir = browser.getDocument().createElement("button");

            divClearFix.setAttribute("class", "clearfix");
            divPedido.setAttribute("class", "card-pedido col-xs-12 col-md-6 col-lg-4");
            divPanel.setAttribute("class", "panel panel-default");
            divPanelHeading.setAttribute("class", "panel-heading");
            divPanelBody.setAttribute("class", "panel-body text-center");
            h3NrPedido.setAttribute("class", "panel-title pull-left");
            spanCancelarPedido.setAttribute("class", "panel-title pull-right glyphicon glyphicon-remove");
            spanCancelarPedido.setAttribute("title", "Cancelar Pedido");
            h2TotalPedido.setAttribute("class", "text-success");
            imgPedido.setAttribute("class", "img-responsive center-block");
            imgPedido.setAttribute("width", "75");
            imgPedido.setAttribute("src", "assets/img/Icon/order.svg");
            buttonVerPedido.setAttribute("class", "btn btn-info btn-block");
            buttonConcluido.setAttribute("class", "btn btn-warning btn-block");
            buttonImprimir.setAttribute("class", "btn btn-default btn-block");
            buttonVerPedido.setTextContent("Ver Pedido");
            if (p.getEstadoPedido() == Pedido.EstadoPedido.Novo) {
                buttonConcluido.setTextContent(p.isEntrega() ? "Saiu p/ entrega" : "Foi Retirado");
                buttonConcluido.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                    @Override
                    public void handleEvent(DOMEvent dome) {
                        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente marcar o pedido como " + (p.isEntrega() ? "saiu p/ entrega" : "retirado") + "?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            Pedido.EstadoPedido estadoAnterior = p.getEstadoPedido();
                            result = JOptionPane.showConfirmDialog(null, "Deseja enviar a mensagem de aviso para o cliente?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                            p.setEstadoPedido(p.isEntrega() ? Pedido.EstadoPedido.SaiuEntrega : Pedido.EstadoPedido.Concluido);
                            if (result == JOptionPane.YES_OPTION) {
                                if (p.getChatId() != null && !p.getChatId().isEmpty()) {
                                    if (!p.isEntrega()) {
                                        p.getChat(driver).sendMessage("Ótimas notícias " + p.getNomeCliente() + ", seu pedido já esta pronto e aguardando a retirada!!");
                                    } else {
                                        p.getChat(driver).sendMessage("Ótimas notícias " + p.getNomeCliente() + ", seu pedido já esta pronto e esta saindo para a entrega!!");
                                    }
                                }
                            }
                            try {
                                if (ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).salvar(p)) {
                                    p.getC().adicionarSelos(p);
                                    ControleClientes.getInstance(Db4oGenerico.getInstance("banco")).salvar(p.getC());
                                    containnerPedidos.removeChild(divPedido);
                                    if (p.getEstadoPedido() != Pedido.EstadoPedido.Concluido) {
                                        addPedidosRetiradaEntrega(p);
                                        atualizarValoresIndex();
                                    }
                                }
                            } catch (Exception ex) {
                                p.setEstadoPedido(estadoAnterior);
                                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }, true);
            } else if (p.getEstadoPedido() == Pedido.EstadoPedido.SaiuEntrega) {
                buttonConcluido.setTextContent("Entregue");
                buttonConcluido.setAttribute("class", "btn btn-success btn-block");
                buttonConcluido.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                    @Override
                    public void handleEvent(DOMEvent dome) {
                        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente marcar o pedido como entregue?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                        if (result == JOptionPane.YES_OPTION) {
                            Pedido.EstadoPedido estadoAnterior = p.getEstadoPedido();
                            p.setEstadoPedido(Pedido.EstadoPedido.Concluido);
                            try {
                                if (ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).salvar(p)) {
                                    containnerPedidos.removeChild(divPedido);
                                    atualizarValoresIndex();
                                }
                            } catch (Exception ex) {
                                p.setEstadoPedido(estadoAnterior);
                                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }, true);
            }
            buttonImprimir.setTextContent("Imprimir Pedido");
            spanCancelarPedido.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    int result = JOptionPane.showConfirmDialog(null, "Deseja realmente cancelar o pedido?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        Pedido.EstadoPedido estadoAnterior = p.getEstadoPedido();
                        p.setEstadoPedido(Pedido.EstadoPedido.Cancelado);
                        if (p.getChatId() != null && !p.getChatId().isEmpty()) {
                            String motivo = JOptionPane.showInputDialog(null, "Qual o motivo do cancelamento? Obs: o cliente sera avisado");
                            if (motivo != null && !motivo.isEmpty()) {
                                p.getChat(driver).sendMessage("Tenho más notícias 😔, seu pedido foi cancelado pelo seguinte motivo: " + motivo);
                            }
                        }
                        try {
                            if (ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).salvar(p)) {
                                JOptionPane.showMessageDialog(null, "Pedido Cancelado");
                                containnerPedidos.removeChild(divPedido);
                                if (p.getC() != null) {
                                    p.getC().realizarRecarga(p.getPgCreditos());
                                }
                                atualizarValoresIndex();
                            }
                        } catch (Exception ex) {
                            p.setEstadoPedido(estadoAnterior);
                            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, true);
            buttonImprimir.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    int result = JOptionPane.showConfirmDialog(null, "Deseja realmente imprimir o pedido?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        imprimirPedido(p);
                    }
                }
            }, true);
            buttonVerPedido.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    VerPedido verPedido = new VerPedido(p);
                    verPedido.abrir();
                    h2TotalPedido.setTextContent(new DecimalFormat("###,###,###.00").format(p.getTotal()));
                }
            }, true);
            h3NrPedido.setTextContent("Pedido #" + p.getCod());
            h2TotalPedido.setTextContent(new DecimalFormat("###,###,###.00").format(p.getTotal()));
            pNomeRetirada.setTextContent(p.getNomeCliente());
            divPedido.appendChild(divPanel);
            divPanel.appendChild(divPanelHeading);
            divPanelHeading.appendChild(h3NrPedido);
            divPanelHeading.appendChild(spanCancelarPedido);
            divPanelHeading.appendChild(divClearFix);
            divPanel.appendChild(divPanelBody);
            divPanelBody.appendChild(imgPedido);
            divPanelBody.appendChild(pNomeRetirada);
            divPanelBody.appendChild(h2TotalPedido);
            divPanelBody.appendChild(buttonVerPedido);
            divPanelBody.appendChild(buttonConcluido);
            divPanelBody.appendChild(buttonImprimir);
            containnerPedidos.appendChild(divPedido);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Adicionar Div das Mesas em Aberto">
    private void addMesa(Mesa m) {
        imprimirPedidosMesa(m);
        DOMElement containnerMesas = browser.getDocument().findElement(By.id("mesasAbertas"));
        DOMElement divMesa = browser.getDocument().createElement("div");
        DOMElement divPanel = browser.getDocument().createElement("div");
        DOMElement divPanelHeading = browser.getDocument().createElement("div");
        DOMElement divPanelBody = browser.getDocument().createElement("div");
        DOMElement h3NrMesa = browser.getDocument().createElement("h3");
        DOMElement h2TotalMesa = browser.getDocument().createElement("h2");
        DOMElement imgMesa = browser.getDocument().createElement("img");
        DOMElement buttonPedidos = browser.getDocument().createElement("button");
        DOMElement buttonFechar = browser.getDocument().createElement("button");

        divMesa.setAttribute("class", "col-xs-3 col-lg-2 card-mesa");
        divPanel.setAttribute("class", "panel panel-default");
        divPanelHeading.setAttribute("class", "panel-heading");
        divPanelBody.setAttribute("class", "panel-body text-center");
        h3NrMesa.setAttribute("class", "panel-title");
        h2TotalMesa.setAttribute("class", "text-success");
        imgMesa.setAttribute("class", "img-responsive center-block");
        imgMesa.setAttribute("width", "75");
        imgMesa.setAttribute("src", "assets/img/Icon/table.svg");
        buttonPedidos.setAttribute("class", "btn btn-info btn-block");
        buttonFechar.setAttribute("class", "btn btn-danger btn-block");
        h3NrMesa.setTextContent("Mesa " + m.getNumeroMesa() + "");
        h2TotalMesa.setTextContent(new DecimalFormat("###,###,###.00").format(m.getTotal()));

        divMesa.appendChild(divPanel);
        divPanel.appendChild(divPanelHeading);
        divPanelHeading.appendChild(h3NrMesa);
        divPanel.appendChild(divPanelBody);
        divPanelBody.appendChild(imgMesa);
        divPanelBody.appendChild(h2TotalMesa);
        divPanelBody.appendChild(buttonPedidos);
        divPanelBody.appendChild(buttonFechar);
        containnerMesas.appendChild(divMesa);

        m.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object o1) {
                h3NrMesa.setTextContent("Mesa " + m.getNumeroMesa() + "");
            }
        });

        m.getPedidos().addListener(new ListChangeListener<Pedido>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends Pedido> c) {
                h2TotalMesa.setTextContent(new DecimalFormat("###,###,###.00").format(m.getTotal()));
                imprimirPedidosMesa(m);
            }
        });
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Imprimir Pedido">
    private void imprimirPedido(Pedido p) {
        new Thread() {
            public void run() {
                if (!ControleImpressao.getInstance().imprimir(p)) {
                    try {
                        Chat c = driver.getFunctions().getChatByNumber("554491050665");
                        if (c != null) {
                            c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Falha ao imprimir o Pedido #" + p.getCod());
                        }
                        c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
                        if (c != null) {
                            c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Falha ao imprimir o Pedido #" + p.getCod());
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    JOptionPane.showMessageDialog(null, "Falha ao imprimir o Pedido #" + p.getCod());
                } else {
                    try {
                        if (!p.isImpresso()) {
                            Thread.sleep(5000);
                            p.setImpresso(true);
                            if (p.getChatId() != null && !p.getChatId().isEmpty() && driver != null && driver.getEstadoDriver() != null && driver.getEstadoDriver() == EstadoDriver.LOGGED) {
                                p.getChat(driver).sendMessage("Pronto, " + p.getNomeCliente() + ". Seu pedido de numero #" + p.getCod() + " foi registrado e já está em produção\nCaso deseje realizar um novo pedido, basta me enviar uma mensagem");
                                if (p.getHoraAgendamento() == null) {
                                    if (!p.isEntrega()) {
                                        p.getChat(driver).sendMessage("Em cerca de 10 à 15 minutos você já pode vir busca-lo");
                                    } else {
                                        p.getChat(driver).sendMessage("Em cerca de 30 à 45 minutos ele sera entrege no endereço informado");
                                    }
                                } else {
                                    if (!p.isEntrega()) {
                                        p.getChat(driver).sendMessage("Às " + p.getHoraAgendamento().format(DateTimeFormatter.ofPattern("HH:mm")) + " você já pode vir buscar");
                                    } else {
                                        p.getChat(driver).sendMessage("Às " + p.getHoraAgendamento().format(DateTimeFormatter.ofPattern("HH:mm")) + " ele sera entregue no endereço informado");
                                    }
                                }
                                ChatBot chat = ControleChatsAsync.getInstance().getChatAsyncByChat(p.getChatId());
                                if (chat != null) {
                                    chat.setHandler(new HandlerPedidoConcluido(chat), true);
                                } else {
                                    JOptionPane.showMessageDialog(null, "Falha ao notificar cliente sobre a conclusão do pedido");
                                    throw new Exception("Falha ao encontrar o ChatAsync");
                                }
                                try {
                                    Chat c = driver.getFunctions().getChatByNumber("554491050665");
                                    if (c != null) {
                                        c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Novo Pedido #" + p.getCod());
                                    }
                                    c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
                                    if (c != null) {
                                        c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Novo Pedido #" + p.getCod());
                                    }
                                } catch (Exception ex) {
                                    logger.log(Level.SEVERE, null, ex);
                                }
                            }
                            ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).alterar(p);
                        }
                    } catch (Exception ex) {
                        p.setImpresso(false);
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Imprimir Pedidos da Mesa">
    private void imprimirPedidosMesa(Mesa m) {
        new Thread() {
            public void run() {
                synchronized (m.getPedidos()) {
                    for (Pedido p : m.getPedidos()) {
                        if (!p.isImpresso()) {
                            if (!ControleImpressao.getInstance().imprimir(p)) {
                                JOptionPane.showMessageDialog(null, "Falha ao imprimir o Pedido #" + p.getCod());
                            } else {
                                p.setImpresso(true);
                                try {
                                    ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).alterar(p);
                                } catch (Exception ex) {
                                    p.setImpresso(false);
                                    Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }
                }
            }
        }.start();
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Gerenciar Reservas Ativas">
    private void gerenciarReservas() {
        DOMElement table = browser.getDocument().findElement(By.id("myTable"));
        for (DOMNode node : table.getChildren()) {
            table.removeChild(node);
        }
        executores.scheduleWithFixedDelay(() -> {
            try {
                for (Reserva r : ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).getReservasAtivas()) {
                    if (!reservasJaAdicionadas.contains(r)) {
                        reservasJaAdicionadas.add(r);
                        addReserva(r);
                    }
                }
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void addReserva(Reserva r) {
        try {
            if (!r.isImpresso()) {
                imprimirReserva(r);
            }
            DOMElement table = browser.getDocument().findElement(By.id("myTable"));
            DOMElement tr = browser.getDocument().createElement("tr");
            tr.setAttribute("cod-reserva", r.getCod() + "");
            DOMElement tdCod = browser.getDocument().createElement("td");
            DOMElement tdNome = browser.getDocument().createElement("td");
            DOMElement tdTelefoneContato = browser.getDocument().createElement("td");
            DOMElement tdDia = browser.getDocument().createElement("td");
            DOMElement tdHora = browser.getDocument().createElement("td");
            DOMElement tdQtPessoas = browser.getDocument().createElement("td");
            DOMElement tdObs = browser.getDocument().createElement("td");
            DOMElement tdBotoes = browser.getDocument().createElement("td");
            DOMElement btAlterar = browser.getDocument().createElement("button");
            btAlterar.setAttribute("class", "btn btn-warning");
            btAlterar.setInnerText("Alterar");
            DOMElement btExcluir = browser.getDocument().createElement("button");
            btExcluir.setAttribute("class", "btn btn-danger");
            btExcluir.setInnerText("Remover");
            tdBotoes.appendChild(btAlterar);
            tdBotoes.appendChild(btExcluir);
            btExcluir.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            if (ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).excluir(r)) {
                                JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                                table.removeChild(tr);
                            }
                        } catch (Exception ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }, true);
            btAlterar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                @Override
                public void handleEvent(DOMEvent dome) {
                    new Reservas(r).abrir();
                }
            }, true);
            tdCod.setInnerText("#" + r.getCod());
            tdNome.setInnerText(r.getNomeContato());
            tdDia.setInnerText(new SimpleDateFormat("dd/MM").format(r.getDataReserva()));
            tdHora.setInnerText(new SimpleDateFormat("HH:mm").format(r.getDataReserva()));
            tdQtPessoas.setInnerText(r.getQtdPessoas() + "");
            tdTelefoneContato.setInnerText(r.getTelefoneContato());
            tdObs.setInnerText(r.getComentario());
            tr.appendChild(tdCod);
            tr.appendChild(tdNome);
            tr.appendChild(tdTelefoneContato);
            tr.appendChild(tdDia);
            tr.appendChild(tdHora);
            tr.appendChild(tdQtPessoas);
            tr.appendChild(tdObs);
            tr.appendChild(tdBotoes);
            table.appendChild(tr);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void imprimirReserva(Reserva r) {
        new Thread() {
            public void run() {
                if (!ControleImpressao.getInstance().imprimir(r)) {
                    JOptionPane.showMessageDialog(null, "Falha ao imprimir a Reserva #" + r.getCod());
                } else {
                    try {
                        if (!r.isImpresso()) {
                            r.setImpresso(true);
                            ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).alterar(r);
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }.start();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Salvar Configuração">
    public boolean saveConfig(JSObject object) {
        try {
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
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
                browser.dispose();
                driver.getBrowser().dispose();
                this.dispose();
                Inicio.main(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Carregar Configuração">
    private void loadConfig() {
        Configuracao.setInstance(ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).pesquisarPorCodigo(1));
        this.setTitle(Configuracao.getInstance().getNomeEstabelecimento());
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
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Abrir Pedidos">
    public void abrirPedidos() {
        Configuracao.getInstance().abrirPedidos();
        try {
            ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
            ControleBackups.getInstance(Db4oGenerico.getInstance("banco")).realizarBackup();
        } catch (Exception ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
        atualizarValoresIndex();
        try {
            Chat c = driver.getFunctions().getChatByNumber("554491050665");
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Pedidos Aberto");
            }
            c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Pedidos Aberto");
            }
        } catch (Exception ex) {

        }
        JOptionPane.showMessageDialog(null, "Pedidos Abertos");
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Fechar Pedidos">
    public void fecharPedidos() {
        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente fechar os pedidos?", "Atenção!!", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            internalFecharPedidos();
        }
    }

    private void internalFecharPedidos() {
        Configuracao.getInstance().fecharPedidos();
        try {
            Chat c = driver.getFunctions().getChatByNumber("554491050665");
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Pedidos Fechado");
            }
            c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Pedidos Fechado");
            }
        } catch (Exception ex) {

        }
        try {
            ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
            ControleBackups.getInstance(Db4oGenerico.getInstance("banco")).realizarBackup();
        } catch (Exception ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
        new Thread() {
            public void run() {
                for (ChatBotDelivery chat : ControleChatsAsync.getInstance().getChats()) {
                    if (!((HandlerBotDelivery) chat.getHandler()).notificaPedidosFechados()) {
                        continue;
                    }
                    chat.sendEncerramos();
                    chat.setHandler(new HandlerBoasVindas(chat), false);
                }
            }
        }.start();
        JOptionPane.showMessageDialog(null, "Pedidos Fechados");
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Abrir Chat Bot">
    public void abrirChatBot() {
        JOptionPane.showMessageDialog(null, "ChatBot Aberto");
        driver.getFunctions().setStatus("Pedidos Pelo WhatsApp Aberto");
        try {
            Chat c = driver.getFunctions().getChatByNumber("554491050665");
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* ChatBot Aberto");
            }
            c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* ChatBot Aberto");
            }
        } catch (Exception ex) {

        }
        Configuracao.getInstance().setOpenChatBot(true);
        try {
            ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
        } catch (Exception ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Fechar Chat Bot">
    public void fecharChatBot() {
        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente fechar o ChatBot?", "Atenção!!", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            Configuracao.getInstance().setOpenChatBot(false);
            try {
                ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
            } catch (Exception ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
            JOptionPane.showMessageDialog(null, "ChatBot Fechado");
            driver.getFunctions().setStatus("Pedidos Pelo WhatsApp Fechado");
            try {
                Chat c = driver.getFunctions().getChatByNumber("554491050665");
                if (c != null) {
                    c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* ChatBot Fechado");
                }
                c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
                if (c != null) {
                    c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* ChatBot Fechado");
                }
            } catch (Exception ex) {

            }
        }
    }
//</editor-fold>

    public void finalizar() {
        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente sair do sistema?\nObs: Lembre-se de fechar os pedidos para que não ocorram problemas no fluxo do caixa!", "Atenção!!", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            executores.shutdown();
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        /*
         Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         If Nimbus (introduced in Java SE 6) is not available, stay with the
         default look and feel. For details see
         http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Inicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Inicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Inicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Inicio.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Inicio();
            }
        });
    }

}
