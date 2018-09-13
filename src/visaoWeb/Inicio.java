/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

//<editor-fold defaultstate="collapsed" desc="imports">
import com.br.joao.Db4ObjectSaveGeneric;
import com.br.joao.Db4oGenerico;
import com.db4o.Db4oEmbedded;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.TTransient;
import com.db4o.events.Event4;
import com.db4o.events.EventListener4;
import com.db4o.events.EventRegistry;
import com.db4o.events.EventRegistryFactory;
import com.db4o.events.ObjectInfoEventArgs;
import com.db4o.ta.TransparentActivationSupport;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSObject;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.ProtocolService;
import com.teamdev.jxbrowser.chromium.dom.By;
import com.teamdev.jxbrowser.chromium.dom.DOMElement;
import com.teamdev.jxbrowser.chromium.dom.DOMInputElement;
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
import modelo.ChatBotDelivery;
import modelo.Configuracao;
import modelo.EstadoDriver;
import modelo.Mesa;
import modelo.MessageBuilder;
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
    private ArrayList<Mesa> mesasEmAberto = new ArrayList<>();
    private Logger logger;
    private EventRegistry events;
    private SimpleDateFormat formatadorDia = new SimpleDateFormat("dd/MM");
    private SimpleDateFormat formatadorHora = new SimpleDateFormat("HH:mm");

    public Inicio() {
        init();
        new CheckForUpdate("http://ddtank.gamesnexus.com.br/delivery/");
        executores.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                new CheckForUpdate("http://ddtank.gamesnexus.com.br/delivery/");
            }
        }, 1, 1, TimeUnit.HOURS);
        this.setLocationRelativeTo(null);
        new JXBrowserCrack();
        browser = new Browser(ContextManager.getInstance().getContext());
        view = new BrowserView(browser);
        ProtocolService protocolService = browser.getContext().getProtocolService();
        protocolService.setProtocolHandler("jar", new ProtocoloHandlerJar());
        this.tabbedPane.add("Sistema", view);
        this.tabbedPane.add("WhatsApp", panelWhatsapp);
        this.setVisible(true);
        //<editor-fold defaultstate="collapsed" desc="log erros">
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
//</editor-fold>
        initWpp();
        criarConfiguracoesBanco();
        if(!ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).isEmpty() && ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).pesquisarPorCodigo(1) == null){
            for(Configuracao c :ControleConfiguracao.getInstance(Db4oGenerico.getInstance("banco")).carregarTodos()){
                try {
                    ControleConfiguracao.getInstance(Db4oGenerico.getInstance("banco")).excluir(c);
                } catch (Exception ex) {
                    Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).isEmpty()) {
            loadConfigPage();
        } else {
            loadConfig();
            loadIndex();
        }
    }

    //<editor-fold defaultstate="collapsed" desc="configurações banco">
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
        events = EventRegistryFactory.forObjectContainer(Db4oGenerico.getInstance("banco").getDb());
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
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="atualizar tempos">
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
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="iniciar chat bot">
    private void initWpp() {
        Runnable actionOnLogin = () -> {
            tabbedPane.setSelectedIndex(1);
            for (Chat chat : driver.getFunctions().getAllNewChats()) {
                ControleChatsAsync.getInstance().addChat(chat);
                System.out.println(chat);
            }
            driver.getFunctions().setListennerToNewChat(new NewChatObserver() {
                @Override
                public void onNewChat(Chat chat) {
                    ControleChatsAsync.getInstance().addChat(chat);
                }
            });
            executores.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    Chat c = driver.getFunctions().getChatByNumber("5544991050665");
                    if (c.getContact().isBlocked()) {
                        c.getContact().setBlocked(false);
                        while (c.getContact().isBlocked()) {

                        }
                        c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Tinha bloqueado seu número.");
                        JOptionPane.showMessageDialog(null, "O Contato do Administrador do sistema estava bloqueado, um aviso foi enviado.\n Caso isso se repita, o sistema ira ser suspenso.");
                    }
                }
            }, 0, 1, TimeUnit.MINUTES);
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
        window.asObject().setProperty("Configuracoes", new Configuracoes(this));
        window.asObject().setProperty("Relatorios", new Relatorios());
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
        browser.getDocument().findElement(By.id("reservas")).setInnerText("");
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
        for (Pedido p : ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosAtivos()) {
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
        events.created().addListener(new EventListener4<ObjectInfoEventArgs>() {
            @Override
            public void onEvent(Event4<ObjectInfoEventArgs> event4, ObjectInfoEventArgs t) {
                if (t.object() instanceof Pedido) {
                    Pedido p = (Pedido) t.object();
                    try {
                        addPedidosRetiradaEntrega(p);
                        atualizarValoresIndex();
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
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
            }
        });
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Atualizar Valores Mini - Relatório da Index">
    private synchronized void atualizarValoresIndex() {
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
    private synchronized void addPedidosRetiradaEntrega(Pedido p) {
        try {
            final DOMElement containnerPedidos;
            final DOMElement liPedido = browser.getDocument().createElement("li");
            if (null == p.getEstadoPedido()) {
                throw new Exception("Estado do Pedido invalido");
            } else {
                switch (p.getEstadoPedido()) {
                    case Novo:
                        containnerPedidos = browser.getDocument().findElement(By.id("pedidosAtivos"));
                        liPedido.setAttribute("class", "list-group-item card-pedido card-pedido-ativo");
                        break;
                    case SaiuEntrega:
                        containnerPedidos = browser.getDocument().findElement(By.id("pedidosSaiuEntrega"));
                        liPedido.setAttribute("class", "list-group-item card-pedido card-pedido-entrega");
                        break;
                    default:
                        throw new Exception("Estado do Pedido invalido");
                }
            }
            {//liPedido
                DOMElement img = browser.getDocument().createElement("img");
                img.setAttribute("class", "hidden-xs hidden-sm");
                img.setAttribute("width", "60");
                img.setAttribute("src", "assets/img/Icon/order.svg");
                DOMElement spanValor = browser.getDocument().createElement("span");
                spanValor.setAttribute("class", "valor");
                spanValor.setTextContent(new DecimalFormat("###,###,###.00").format(p.getTotal()));
                DOMElement divInfo = browser.getDocument().createElement("div");
                divInfo.setAttribute("class", "info");
                {//divInfo
                    {//cod
                        DOMElement h1 = browser.getDocument().createElement("h1");
                        DOMElement strong = browser.getDocument().createElement("strong");
                        strong.setTextContent("#" + p.getCod());
                        h1.appendChild(strong);
                        divInfo.appendChild(h1);
                    }
                    {//cliente
                        DOMElement h2 = browser.getDocument().createElement("h2");
                        h2.setTextContent(p.getNomeCliente());
                        divInfo.appendChild(h2);
                    }
                    {//valor
                        divInfo.appendChild(spanValor);
                    }
                    {//remover
                        DOMElement span = browser.getDocument().createElement("span");
                        span.setAttribute("class", "glyphicon glyphicon-remove");
                        span.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
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
                                            containnerPedidos.removeChild(liPedido);
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
                        divInfo.appendChild(span);
                    }
                    DOMElement divAcoes = browser.getDocument().createElement("div");
                    divAcoes.setAttribute("class", "acoes");
                    {
                        {//verPedido
                            DOMElement button = browser.getDocument().createElement("button");
                            button.setAttribute("class", "btn btn-info");
                            button.setAttribute("data-toggle", "tooltip");
                            button.setAttribute("data-placement", "top");
                            button.setAttribute("title", "Ver Pedido");
                            DOMElement span = browser.getDocument().createElement("span");
                            span.setAttribute("class", "glyphicon glyphicon-eye-open");
                            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                @Override
                                public void handleEvent(DOMEvent dome) {
                                    VerPedido verPedido = new VerPedido(p);
                                    verPedido.abrir();
                                    spanValor.setTextContent(new DecimalFormat("###,###,###.00").format(p.getTotal()));
                                }
                            }, true);
                            button.appendChild(span);
                            divAcoes.appendChild(button);
                        }
                        {//pedidoConcluido
                            DOMElement button = browser.getDocument().createElement("button");
                            DOMElement span = browser.getDocument().createElement("span");

                            if (p.getEstadoPedido() == Pedido.EstadoPedido.Novo) {
                                button.setAttribute("class", p.isEntrega() ? "btn btn-warning" : "btn btn-success");
                                button.setAttribute("data-toggle", "tooltip");
                                button.setAttribute("data-placement", "top");
                                button.setAttribute("title", p.isEntrega() ? "Saiu para Entrega" : "Pedido Entregue");
                                span.setAttribute("class", p.isEntrega() ? "glyphicon glyphicon-share-alt" : "glyphicon glyphicon-ok");
                                button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
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
                                                    containnerPedidos.removeChild(liPedido);
                                                    if (p.getEstadoPedido() != Pedido.EstadoPedido.Concluido) {
                                                        addPedidosRetiradaEntrega(p);
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
                            } else if (p.getEstadoPedido() == Pedido.EstadoPedido.SaiuEntrega) {
                                button.setAttribute("class", "btn btn-success");
                                button.setAttribute("data-toggle", "tooltip");
                                button.setAttribute("data-placement", "top");
                                button.setAttribute("title", "Pedido Entregue");
                                span.setAttribute("class", "glyphicon glyphicon-ok");
                                button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                    @Override
                                    public void handleEvent(DOMEvent dome) {
                                        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente marcar o pedido como entregue?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                                        if (result == JOptionPane.YES_OPTION) {
                                            Pedido.EstadoPedido estadoAnterior = p.getEstadoPedido();
                                            p.setEstadoPedido(Pedido.EstadoPedido.Concluido);
                                            try {
                                                if (ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).salvar(p)) {
                                                    containnerPedidos.removeChild(liPedido);
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
                            button.appendChild(span);
                            divAcoes.appendChild(button);
                        }
                        {//imprimirPedido
                            DOMElement button = browser.getDocument().createElement("button");
                            button.setAttribute("class", "btn btn-default");
                            button.setAttribute("data-toggle", "tooltip");
                            button.setAttribute("data-placement", "top");
                            button.setAttribute("title", "Imprimir Pedido");
                            DOMElement span = browser.getDocument().createElement("span");
                            span.setAttribute("class", "glyphicon glyphicon-print");
                            button.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                                @Override
                                public void handleEvent(DOMEvent dome) {
                                    int result = JOptionPane.showConfirmDialog(null, "Deseja realmente imprimir o pedido?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                                    if (result == JOptionPane.YES_OPTION) {
                                        imprimirPedido(p);
                                    }
                                }
                            }, true);
                            button.appendChild(span);
                            divAcoes.appendChild(button);
                        }
                        divInfo.appendChild(divAcoes);
                    }
                }
                liPedido.appendChild(img);
                liPedido.appendChild(divInfo);
                containnerPedidos.appendChild(liPedido);
            }
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
                            p.setImpresso(true);
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
        for (Reserva r : ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).getReservasAtivas()) {
            try {
                addReserva(r);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        events.created().addListener(new EventListener4<ObjectInfoEventArgs>() {
            @Override
            public void onEvent(Event4<ObjectInfoEventArgs> event4, ObjectInfoEventArgs t) {
                if (t.object() instanceof Reserva) {
                    Reserva r = (Reserva) t.object();
                    try {
                        addReserva(r);
                        imprimirReserva(r);
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                    try {
                        Chat c = driver.getFunctions().getChatByNumber("554491050665");
                        if (c != null) {
                            c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Nova Reserva #" + r.getCod());
                        }
                        c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
                        if (c != null) {
                            c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Novo Reserva #" + r.getCod());
                        }
                    } catch (Exception ex) {
                        logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
        });
    }

    private void addReserva(Reserva r) {
        try {
            DOMElement containner = browser.getDocument().findElement(By.id("reservas"));
            {
                DOMElement liReserva = browser.getDocument().createElement("li");
                liReserva.setAttribute("class", "list-group-item");
                {
                    DOMElement row = browser.getDocument().createElement("div");
                    row.setAttribute("class", "row");
                    {
                        DOMElement spanEditar = browser.getDocument().createElement("span");
                        spanEditar.setAttribute("class", "glyphicon glyphicon-pencil");
                        spanEditar.setAttribute("title", "Editar");
                        spanEditar.setAttribute("data-toggle", "tooltip");
                        spanEditar.setAttribute("data-placement", "top");
                        row.appendChild(spanEditar);
                        spanEditar.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                            @Override
                            public void handleEvent(DOMEvent dome) {
                                new Reservas(r).abrir();
                            }
                        }, true);

                        DOMElement spanRemover = browser.getDocument().createElement("span");
                        spanRemover.setAttribute("class", "glyphicon glyphicon-remove");
                        spanRemover.setAttribute("title", "Remover");
                        spanRemover.setAttribute("data-toggle", "tooltip");
                        spanRemover.setAttribute("data-placement", "top");
                        row.appendChild(spanRemover);
                        spanRemover.addEventListener(DOMEventType.OnClick, new DOMEventListener() {
                            @Override
                            public void handleEvent(DOMEvent dome) {
                                int result = JOptionPane.showConfirmDialog(null, "Deseja realmente excluir?", "Atenção!!", JOptionPane.YES_NO_OPTION);
                                if (result == JOptionPane.YES_OPTION) {
                                    try {
                                        if (ControleReservas.getInstance(Db4oGenerico.getInstance("banco")).excluir(r)) {
                                            JOptionPane.showMessageDialog(null, "Excluido com Sucesso!");
                                            containner.removeChild(liReserva);
                                        }
                                    } catch (Exception ex) {
                                        logger.log(Level.SEVERE, null, ex);
                                    }
                                }
                            }
                        }, true);
                        {//nome e codigo
                            DOMElement div = browser.getDocument().createElement("div");
                            div.setAttribute("class", "col-xs-12 col-md-4 col-lg-6");
                            {
                                DOMElement h1 = browser.getDocument().createElement("h1");
                                h1.setTextContent("#" + r.getCod());
                                DOMElement h2 = browser.getDocument().createElement("h2");
                                h2.setTextContent(r.getNomeContato());
                                div.appendChild(h1);
                                div.appendChild(h2);
                            }
                            row.appendChild(div);
                        }

                        {//telefone
                            DOMElement div = browser.getDocument().createElement("div");
                            div.setAttribute("class", "col-xs-12 col-sm-4 col-md-3 col-lg-2");
                            {
                                DOMElement h1 = browser.getDocument().createElement("h1");
                                h1.setTextContent("Fone");
                                DOMElement span = browser.getDocument().createElement("span");
                                span.setAttribute("class", "badge");
                                span.setTextContent(r.getTelefoneContato());
                                div.appendChild(h1);
                                div.appendChild(span);
                            }
                            row.appendChild(div);
                        }

                        {//data
                            DOMElement div = browser.getDocument().createElement("div");
                            div.setAttribute("class", "col-xs-6 col-sm-4 col-md-3 col-lg-2");
                            {
                                DOMElement h1 = browser.getDocument().createElement("h1");
                                h1.setTextContent("Data");
                                DOMElement span = browser.getDocument().createElement("span");
                                DOMElement span2 = browser.getDocument().createElement("span");
                                span.setAttribute("class", "badge");
                                span2.setAttribute("class", "badge");
                                span.setTextContent(formatadorDia.format(r.getDataReserva()));
                                span2.setTextContent(formatadorHora.format(r.getDataReserva()));
                                div.appendChild(h1);
                                div.appendChild(span);
                                div.appendChild(span2);
                            }
                            row.appendChild(div);
                        }

                        {//quantidade
                            DOMElement div = browser.getDocument().createElement("div");
                            div.setAttribute("class", "col-xs-6 col-sm-4 col-md-2");
                            {
                                DOMElement h1 = browser.getDocument().createElement("h1");
                                h1.setTextContent("Para");
                                DOMElement span = browser.getDocument().createElement("span");
                                span.setAttribute("class", "badge");
                                span.setTextContent(r.getQtdPessoas() + " pessoas");
                                div.appendChild(h1);
                                div.appendChild(span);
                            }
                            row.appendChild(div);
                        }
                    }
                    liReserva.appendChild(row);
                }
                if (!r.getComentario().isEmpty()) {
                    DOMElement row = browser.getDocument().createElement("div");
                    row.setAttribute("class", "row comentario");
                    {
                        DOMElement div = browser.getDocument().createElement("div");
                        div.setAttribute("class", "col-xs-12");
                        {
                            DOMElement p = browser.getDocument().createElement("p");
                            p.setTextContent(r.getComentario());
                            div.appendChild(p);
                        }
                        row.appendChild(div);
                    }
                    liReserva.appendChild(row);
                }
                containner.appendChild(liReserva);
            }
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
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
//</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Carregar Configuração">
    public void loadConfig() {
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
        List<Pedido> pedidosDoDia = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosDeliveryDoDia();
        long totalPedidoCancelados = pedidosDoDia.stream().filter(o -> ((Pedido) o).getEstadoPedido() == Pedido.EstadoPedido.Cancelado).count();
        long totalPedidos = pedidosDoDia.size();
        long totalPedidosDelivery = pedidosDoDia.stream().filter(o -> o.isEntrega()).count();
        long totalPedidosDeliveryEntregues = pedidosDoDia.stream().filter(o -> ((Pedido) o).getEstadoPedido() == Pedido.EstadoPedido.Concluido && o.isEntrega()).count();
        long totalPedidosDeliveryEmAberto = pedidosDoDia.stream().filter(o -> o.getEstadoPedido() != Pedido.EstadoPedido.Cancelado && ((Pedido) o).getEstadoPedido() != Pedido.EstadoPedido.Concluido && o.isEntrega()).count();
        long totalPedidosDeliveryCancelados = pedidosDoDia.stream().filter(o -> o.getEstadoPedido() == Pedido.EstadoPedido.Cancelado && o.isEntrega()).count();
        long totalPedidosRetirada = pedidosDoDia.stream().filter(o -> !o.isEntrega()).count();
        long totalPedidosRetiradaEntregues = pedidosDoDia.stream().filter(o -> ((Pedido) o).getEstadoPedido() == Pedido.EstadoPedido.Concluido && !o.isEntrega()).count();
        long totalPedidosRetiradaEmAberto = pedidosDoDia.stream().filter(o -> o.getEstadoPedido() != Pedido.EstadoPedido.Cancelado && ((Pedido) o).getEstadoPedido() != Pedido.EstadoPedido.Concluido && !o.isEntrega()).count();
        long totalPedidosRetiradaCancelados = pedidosDoDia.stream().filter(o -> o.getEstadoPedido() == Pedido.EstadoPedido.Cancelado && !o.isEntrega()).count();

        long valorPedidos = 0;
        for (Pedido p : pedidosDoDia) {
            if (p.getEstadoPedido() == Pedido.EstadoPedido.Cancelado) {
                continue;
            }
            valorPedidos += p.getTotal();
        }
        MessageBuilder builder = new MessageBuilder();
        builder.textBold(Configuracao.getInstance().getNomeEstabelecimento()).text(" - Resumo do Dia").newLine().newLine();
        builder.textBold("Total de Pedidos").text(": ").text(totalPedidos + "").newLine();
        builder.textBold("Total de Pedidos Cancelados").text(": ").text(totalPedidoCancelados + "").newLine().newLine();
        builder.textBold("Total de Pedidos Delivery").text(": ").text(totalPedidosDelivery + "").newLine();
        builder.textBold("Total de Pedidos Delivery Em Aberto").text(": ").text(totalPedidosDeliveryEmAberto + "").newLine();
        builder.textBold("Total de Pedidos Delivery Entregues").text(": ").text(totalPedidosDeliveryEntregues + "").newLine();
        builder.textBold("Total de Pedidos Delivery Cancelados").text(": ").text(totalPedidosDeliveryCancelados + "").newLine().newLine();
        builder.textBold("Total de Pedidos Retirada").text(": ").text(totalPedidosRetirada + "").newLine();
        builder.textBold("Total de Pedidos Retirada Em Aberto").text(": ").text(totalPedidosRetiradaEmAberto + "").newLine();
        builder.textBold("Total de Pedidos Retirada Concluidos").text(": ").text(totalPedidosRetiradaEntregues + "").newLine();
        builder.textBold("Total de Pedidos Retirada Cancelados").text(": ").text(totalPedidosRetiradaCancelados + "").newLine().newLine();
        if (valorPedidos > 0) {
            builder.textBold("Valor Total").text(": ").text(new DecimalFormat("###,###,###.00").format(valorPedidos) + "").newLine().newLine();
        }
        try {
            Chat c = driver.getFunctions().getChatByNumber("554491050665");
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Pedidos Fechado");
                c.sendMessage(builder.build());
            }
            c = driver.getFunctions().getChatByNumber("55" + Utilitarios.plainText(Configuracao.getInstance().getNumeroAviso()));
            if (c != null) {
                c.sendMessage("*" + Configuracao.getInstance().getNomeEstabelecimento() + ":* Pedidos Fechado");
                c.sendMessage(builder.build());
            }
        } catch (Exception ex) {

        }
        try {
            ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
            ControleBackups.getInstance(Db4oGenerico.getInstance("banco")).realizarBackup();
        } catch (Exception ex) {
            Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
        }
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

    //<editor-fold defaultstate="collapsed" desc="finalizar">
    public void finalizar() {
        int result = JOptionPane.showConfirmDialog(null, "Deseja realmente sair do sistema?\nObs: Lembre-se de fechar os pedidos para que não ocorram problemas no fluxo do caixa!", "Atenção!!", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            executores.shutdown();
            try {
                ControleConfiguracao.getInstance(Db4oGenerico.getInstance("config")).salvar(Configuracao.getInstance());
            } catch (Exception ex) {
                Logger.getLogger(Inicio.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
    }
//</editor-fold>

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
