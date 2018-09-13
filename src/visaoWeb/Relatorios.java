/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.br.joao.Db4oGenerico;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.Callback;
import com.teamdev.jxbrowser.chromium.JSValue;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;
import controle.ControlePedidos;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
import modelo.Configuracao;
import modelo.Pedido;
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
        new Thread() {
            public void run() {
                while (!Relatorios.this.isVisible()) {
                }
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
            }
        }.start();
        this.setVisible(true);
    }

    private void recriarTable() {
        LocalDateTime primeiroMes = LocalDate.now().atStartOfDay().withDayOfMonth(1).minusMonths(2);
        LocalDateTime ultimoMes = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(LocalTime.MAX);
        vendasProdutosUltimosTresMeses(primeiroMes, ultimoMes);
        receitaUltimosTresMeses(primeiroMes, ultimoMes);
        entregasDiaSemanaUltimosTresMeses(primeiroMes, ultimoMes);
        entregasHorarios(LocalDate.now());
    }

    private void entregasHorarios(LocalDate date) {
        List<Pedido> pedidos = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosBetweenDate(Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), Date.from(date.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant()));
        HashMap<Integer, List<Pedido>> group = new HashMap<>();
        for (int x = 0; x < 23; x++) {
            group.put(x, new ArrayList<>());
        }
        for (Pedido p : pedidos) {
            if (!p.isEntrega()) {
                continue;
            }
            Calendar c = Calendar.getInstance();
            c.setTime(p.getDataPedido());
            int horario = c.get(Calendar.HOUR_OF_DAY);
            group.get(horario).add(p);
        }
        Gson builder = new Gson();
        JsonArray series = new JsonArray();
        for (int x = 0; x < 23; x++) {
            series.add(group.get(x).size());
        }
        browser.executeJavaScript("Highcharts.chart('graf-04', {\n"
                + "                        chart: {\n"
                + "                            type: 'areaspline'\n"
                + "                        },\n"
                + "                        title: {\n"
                + "                            text: 'Entregas por Hora do Dia'\n"
                + "                        },\n"
                + "                        subtitle: {\n"
                + "                            text: '" + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "' //data do periodo\n"
                + "                        },\n"
                + "                        legend: {\n"
                + "                            layout: 'vertical',\n"
                + "                            align: 'left',\n"
                + "                            verticalAlign: 'top',\n"
                + "                            x: 150,\n"
                + "                            y: 100,\n"
                + "                            floating: true,\n"
                + "                            borderWidth: 1,\n"
                + "                            backgroundColor: (Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'\n"
                + "                        },\n"
                + "                        xAxis: {\n"
                + "                            categories: [\n"
                + "                                '00:00',\n"
                + "                                '01:00',\n"
                + "                                '02:00',\n"
                + "                                '03:00',\n"
                + "                                '04:00',\n"
                + "                                '05:00',\n"
                + "                                '06:00',\n"
                + "                                '07:00',\n"
                + "                                '08:00',\n"
                + "                                '09:00',\n"
                + "                                '10:00',\n"
                + "                                '11:00',\n"
                + "                                '12:00',\n"
                + "                                '13:00',\n"
                + "                                '14:00',\n"
                + "                                '15:00',\n"
                + "                                '16:00',\n"
                + "                                '17:00',\n"
                + "                                '18:00',\n"
                + "                                '19:00',\n"
                + "                                '20:00',\n"
                + "                                '21:00',\n"
                + "                                '22:00',\n"
                + "                                '23:00'\n"
                + "                            ]\n"
                + "                        },\n"
                + "                        yAxis: {\n"
                + "                            title: {\n"
                + "                                text: 'Quantidade'\n"
                + "                            }\n"
                + "                        },\n"
                + "                        tooltip: {\n"
                + "                            shared: true,\n"
                + "                            valueSuffix: ''\n"
                + "                        },\n"
                + "                        credits: {\n"
                + "                            enabled: false\n"
                + "                        },\n"
                + "                        plotOptions: {\n"
                + "                            areaspline: {\n"
                + "                                fillOpacity: 0.5\n"
                + "                            },\n"
                + "                            series: {\n"
                + "                                animation: true,\n"
                + "                                dataLabels: {\n"
                + "                                    enabled: true\n"
                + "                                }\n"
                + "                            }\n"
                + "                        },\n"
                + "                        series: [{\n"
                + "                            name: 'Entregas',\n"
                + "                            data: " + builder.toJson(series) + " //Quant Entregas\n"
                + "                        }]\n"
                + "                    });");
    }

    private void entregasDiaSemanaUltimosTresMeses(LocalDateTime primeiroMes, LocalDateTime ultimoMes) {
        List<Pedido> pedidos = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosBetweenDate(Date.from(primeiroMes.atZone(ZoneId.systemDefault()).toInstant()), Date.from(ultimoMes.atZone(ZoneId.systemDefault()).toInstant()));;
        HashMap<Integer, List<Pedido>> group = new HashMap<>();
        for (int x = 0; x < 7; x++) {
            group.put(x, new ArrayList<>());
        }
        for (Pedido p : pedidos) {
            if (!p.isEntrega()) {
                continue;
            }
            Calendar c = Calendar.getInstance();
            c.setTime(p.getDataPedido());
            int diaSemana = c.get(Calendar.DAY_OF_WEEK) - 1;
            group.get(diaSemana).add(p);
        }
        browser.executeJavaScript("Highcharts.chart('graf-03', {\n"
                + "                        chart: {\n"
                + "                            type: 'column'\n"
                + "                        },\n"
                + "                        title: {\n"
                + "                            text: 'Entregas por Semana'\n"
                + "                        },\n"
                + "                        subtitle: {\n"
                + "                            text: '" + primeiroMes.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + ultimoMes.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "' //Periodo\n"
                + "                        },\n"
                + "                        xAxis: {\n"
                + "                            categories: [\n"
                + "                                ''\n"
                + "                            ],\n"
                + "                            crosshair: true\n"
                + "                        },\n"
                + "                        yAxis: {\n"
                + "                            min: 0,\n"
                + "                            title: {\n"
                + "                                text: 'Quantidade'\n"
                + "                            }\n"
                + "                        },\n"
                + "                        tooltip: {\n"
                + "                            headerFormat: '<span style=\"font-size:10px\">{point.key}</span><table>',\n"
                + "                            pointFormat: '<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>' +\n"
                + "                                '<td style=\"padding:0\"><b>{point.y}</b></td></tr>',\n"
                + "                            footerFormat: '</table>',\n"
                + "                            shared: true,\n"
                + "                            useHTML: true\n"
                + "                        },\n"
                + "                        plotOptions: {\n"
                + "                            column: {\n"
                + "                                pointPadding: 0.2,\n"
                + "                                borderWidth: 0\n"
                + "                            },\n"
                + "                            series: {\n"
                + "                                animation: true,\n"
                + "                                dataLabels: {\n"
                + "                                    enabled: true\n"
                + "                                }\n"
                + "                            }\n"
                + "\n"
                + "                        },\n"
                + "                        series: [{\n"
                + "                            name: 'Domingo', //Dia\n"
                + "                            data: [" + group.get(0).size() + "] // Quantidade de entregas\n"
                + "\n"
                + "                        }, {\n"
                + "                            name: 'Segunda',\n"
                + "                            data: [" + group.get(1).size() + "]\n"
                + "\n"
                + "                        }, {\n"
                + "                            name: 'Terça',\n"
                + "                            data: [" + group.get(2).size() + "]\n"
                + "\n"
                + "                        }, {\n"
                + "                            name: 'Quarta',\n"
                + "                            data: [" + group.get(3).size() + "]\n"
                + "\n"
                + "                        }, {\n"
                + "                            name: 'Quinta',\n"
                + "                            data: [" + group.get(4).size() + "]\n"
                + "\n"
                + "                        }, {\n"
                + "                            name: 'Sexta',\n"
                + "                            data: [" + group.get(5).size() + "]\n"
                + "\n"
                + "                        }, {\n"
                + "                            name: 'Sabado',\n"
                + "                            data: [" + group.get(6).size() + "]\n"
                + "\n"
                + "                        }]\n"
                + "                    });");
    }

    private void receitaUltimosTresMeses(LocalDateTime primeiroMes, LocalDateTime ultimoMes) {
        LocalDateTime primeiroMesCopia = LocalDate.now().atStartOfDay().withDayOfMonth(1).minusMonths(2);
        List<Pedido> pedidos = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getPedidosBetweenDate(Date.from(primeiroMes.atZone(ZoneId.systemDefault()).toInstant()), Date.from(ultimoMes.atZone(ZoneId.systemDefault()).toInstant()));;
        HashMap<String, List<Pedido>> pedidosPorMes = new HashMap<>();
        JsonArray meses = new JsonArray();
        while (primeiroMesCopia.isBefore(ultimoMes)) {
            meses.add(primeiroMesCopia.format(DateTimeFormatter.ofPattern("MMMM")));
            pedidosPorMes.put(primeiroMesCopia.format(DateTimeFormatter.ofPattern("MMMM")), new ArrayList<>());
            primeiroMesCopia = primeiroMesCopia.plus(Period.ofMonths(1));
        }
        for (Pedido p : pedidos) {
            String mesAtual = new SimpleDateFormat("MMMM").format(p.getDataPedido());
            if (pedidosPorMes.containsKey(mesAtual)) {
                pedidosPorMes.get(mesAtual).add(p);
            } else {
                pedidosPorMes.put(mesAtual, new ArrayList<>());
                pedidosPorMes.get(mesAtual).add(p);
            }
        }
        JsonArray series = new JsonArray();
        {//delivery
            JsonObject delivery = new JsonObject();
            delivery.addProperty("name", "Delivery");
            JsonArray valores = new JsonArray();
            delivery.add("data", valores);
            for (int x = 0; x < meses.size(); x++) {
                double valor = 0;
                for (Pedido p : pedidosPorMes.get(meses.get(x).getAsString())) {
                    if (p.isEntrega()) {
                        valor += p.getTotal()+p.getPgCreditos();
                    }
                }
                valores.add(new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).floatValue());
            }
            series.add(delivery);
        }
        {//retirada
            JsonObject retirada = new JsonObject();
            retirada.addProperty("name", "Retirada");
            JsonArray valores = new JsonArray();
            retirada.add("data", valores);
            for (int x = 0; x < meses.size(); x++) {
                double valor = 0;
                for (Pedido p : pedidosPorMes.get(meses.get(x).getAsString())) {
                    if (!p.isEntrega()) {
                        valor += p.getTotal()+p.getPgCreditos();
                    }
                }
                valores.add(new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).floatValue());
            }
            series.add(retirada);
        }
        {//total
            JsonObject delivery = new JsonObject();
            delivery.addProperty("name", "Total");
            JsonArray valores = new JsonArray();
            delivery.add("data", valores);
            for (int x = 0; x < meses.size(); x++) {
                double valor = 0;
                for (Pedido p : pedidosPorMes.get(meses.get(x).getAsString())) {
                        valor += p.getTotal()+p.getPgCreditos();
                }
                valores.add(new BigDecimal(valor).setScale(2, RoundingMode.HALF_UP).floatValue());
            }
            series.add(delivery);
        }

        Gson builder = new Gson();
        String seriesString = builder.toJson(series);
        String mesesString = builder.toJson(meses);
        browser.executeJavaScript("Highcharts.chart('graf-02', {\n"
                + "                        chart: {\n"
                + "                            type: 'line'\n"
                + "                        },\n"
                + "                        title: {\n"
                + "                            text: 'Receita por Período de Venda'\n"
                + "                        },\n"
                + "                        subtitle: {\n"
                + "                            text: '" + primeiroMes.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + ultimoMes.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "' //Periodo\n"
                + "                        },\n"
                + "                        xAxis: {\n"
                + "                            categories: " + mesesString + " //Meses do Periodo\n"
                + "                        },\n"
                + "                        yAxis: {\n"
                + "                            title: {\n"
                + "                                text: 'Receita (R$)'\n"
                + "                            }\n"
                + "                        },\n"
                + "                        plotOptions: {\n"
                + "                            line: {\n"
                + "                                dataLabels: {\n"
                + "                                    enabled: true\n"
                + "                                },\n"
                + "                                enableMouseTracking: true\n"
                + "                            }\n"
                + "                        },\n"
                + "                        series: " + seriesString + "\n"
                + "                    });");

    }

    private void vendasProdutosUltimosTresMeses(LocalDateTime primeiroMes, LocalDateTime ultimoMes) {
        LinkedHashMap<String, Integer> ranking = ControlePedidos.getInstance(Db4oGenerico.getInstance("banco")).getVendasBetweenDate(Date.from(primeiroMes.atZone(ZoneId.systemDefault()).toInstant()), Date.from(ultimoMes.atZone(ZoneId.systemDefault()).toInstant()));
        JsonArray array = new JsonArray();
        for (Entry<String, Integer> entry : ranking.entrySet()) {
            JsonObject ob = new JsonObject();
            JsonArray array2 = new JsonArray();
            array2.add(entry.getValue());
            ob.addProperty("name", entry.getKey());
            ob.add("data", array2);
            array.add(ob);
        }
        Gson builder = new Gson();
        String series = builder.toJson(array);
        browser.executeJavaScript("Highcharts.chart('graf-01', {\n"
                + "                        chart: {\n"
                + "                            type: 'column'\n"
                + "                        },\n"
                + "                        title: {\n"
                + "                            text: 'Vendas por Produto'\n"
                + "                        },\n"
                + "                        subtitle: {\n"
                + "                            text: '" + primeiroMes.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - " + ultimoMes.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "' //data do periodo\n"
                + "                        },\n"
                + "                        xAxis: {\n"
                + "                            categories: [\n"
                + "                                ''\n"
                + "                            ],\n"
                + "                            crosshair: true\n"
                + "                        },\n"
                + "                        yAxis: {\n"
                + "                            min: 0,\n"
                + "                            title: {\n"
                + "                                text: 'Quantidade'\n"
                + "                            }\n"
                + "                        },\n"
                + "                        tooltip: {\n"
                + "                            headerFormat: '<span style=\"font-size:10px\">{point.key}</span><table>',\n"
                + "                            pointFormat: '<tr><td style=\"color:{series.color};padding:0\">{series.name}: </td>' +\n"
                + "                                '<td style=\"padding:0\"><b>{point.y}</b></td></tr>',\n"
                + "                            footerFormat: '</table>',\n"
                + "                            shared: true,\n"
                + "                            useHTML: true\n"
                + "                        },\n"
                + "                        plotOptions: {\n"
                + "                            column: {\n"
                + "                                pointPadding: 0.2,\n"
                + "                                borderWidth: 0\n"
                + "                            },\n"
                + "                            series: {\n"
                + "                                animation: true,\n"
                + "                                dataLabels: {\n"
                + "                                    enabled: true\n"
                + "                                }\n"
                + "                            }\n"
                + "\n"
                + "                        },\n"
                + "                        series: " + series + "\n"
                + "\n"
                + "                    });");
    }

}
