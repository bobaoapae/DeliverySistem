/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import email.com.gmail.ttsai0509.escpos.EscPosBuilder;
import email.com.gmail.ttsai0509.escpos.command.Align;
import email.com.gmail.ttsai0509.escpos.command.Cut;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import modelo.AdicionalProduto;
import modelo.BordaPizza;
import modelo.Configuracao;
import modelo.ItemComboPedido;
import modelo.ItemPedido;
import modelo.Mesa;
import modelo.Pedido;
import modelo.Pizza;
import modelo.Reserva;
import modelo.SaborPizza;
import org.apache.commons.collections4.CollectionUtils;

/**

 @author SYSTEM
 */
public class ControleImpressao {

    private static ControleImpressao instance;

    public static ControleImpressao getInstance() {
        if (instance == null) {
            instance = new ControleImpressao();
        }
        return instance;
    }

    public boolean imprimir(Pedido p) {
        DecimalFormat moneyFormat = new DecimalFormat("###,###,###.00");
        EscPosBuilder builderImpressaoGeral = new EscPosBuilder().initialize();
        if (p.getNumeroMesa() <= 0) {
            builderImpressaoGeral.
                    font(email.com.gmail.ttsai0509.escpos.command.Font.DWDH_EMPHASIZED).align(Align.CENTER).text(Configuracao.getInstance().getNomeEstabelecimento() + "\r\n").font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR).feed(2);
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DWDH_EMPHASIZED).text("Ótima Noite\r\n" + p.getNomeCliente() + "\r\n").text("Bom Apetite!").feed(6).cut(Cut.PART);
            builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
        }
        SimpleDateFormat formatadorData = new SimpleDateFormat("HH:mm dd/MM/yyyy");
        builderImpressaoGeral.text(getStringWithSpacer("Data:", formatadorData.format(p.getDataPedido()), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Pedido ", "#" + p.getCod() + "", 42, "."));
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
        if (p.getNumeroMesa() > 0) {
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("Mesa: " + p.getNumeroMesa());
        }
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.align(Align.LEFT);
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DW);
        ArrayList<ItemPedido> aRemover = new ArrayList<>();
        synchronized (p.getProdutos()) {
            for (int x = 0; x < p.getProdutos().size(); x++) {
                ItemPedido item = p.getProdutos().get(x);
                if (!(item instanceof ItemComboPedido)) {
                    Iterator<ItemPedido> iguais = p.getProdutos().stream().filter(o -> o.getP().equals(item.getP()) && item.getComentario().equals(((ItemPedido) o).getComentario()) && CollectionUtils.isEqualCollection(((ItemPedido) o).getAdicionais(), item.getAdicionais())).iterator();
                    if (aRemover.contains(item)) {
                        continue;
                    }
                    if (iguais.hasNext()) {
                        ItemPedido itemBase = iguais.next();
                        while (iguais.hasNext()) {
                            ItemPedido atual = iguais.next();
                            itemBase.setQtd(itemBase.getQtd() + atual.getQtd());
                            aRemover.add(atual);
                        }
                    }
                } else {
                    Iterator<ItemPedido> iguais = p.getProdutos().stream().filter(o -> ItemComboPedido.class.isAssignableFrom(o.getClass()) && o.getP().equals(item.getP()) && item.getComentario().equals(((ItemPedido) o).getComentario()) && CollectionUtils.isEqualCollection(((ItemPedido) o).getAdicionais(), item.getAdicionais()) && CollectionUtils.isEqualCollection(((ItemComboPedido) o).getProdutosEscolhidosCombo(), ((ItemComboPedido) item).getProdutosEscolhidosCombo())).iterator();
                    if (aRemover.contains(item)) {
                        continue;
                    }
                    if (iguais.hasNext()) {
                        ItemPedido itemBase = iguais.next();
                        while (iguais.hasNext()) {
                            ItemPedido atual = iguais.next();
                            itemBase.setQtd(itemBase.getQtd() + atual.getQtd());
                            aRemover.add(atual);
                        }
                    }
                }
            }
            p.getProdutos().removeAll(aRemover);
            for (ItemPedido item : p.getProdutos()) {
                if (item instanceof ItemComboPedido) {
                    String produto = item.getQtd() + "x " + item.getP().getNome() + (item.getComentario().isEmpty() ? "" : " ( " + item.getComentario() + " ) ");
                    builderImpressaoGeral.text((getStringWithSpacer(produto, "R$" + moneyFormat.format(item.getSubTotal()), 10, ".")));
                    builderImpressaoGeral.text("\r\n");
                    if (((ItemComboPedido) item).getProdutosEscolhidosCombo().size() > 0) {
                        builderImpressaoGeral.text("Produtos Combo: " + "\r\n");
                        for (int x = 0; x < ((ItemComboPedido) item).getProdutosEscolhidosCombo().size(); x++) {
                            ItemPedido escolhido = ((ItemComboPedido) item).getProdutosEscolhidosCombo().get(x);
                            if (!(escolhido.getP() instanceof Pizza)) {
                                String produto2 = escolhido.getQtd() + "x " + escolhido.getP().getNome() + (escolhido.getComentario().isEmpty() ? "" : " ( " + escolhido.getComentario() + " ) ");
                                builderImpressaoGeral.text(produto2);
                                builderImpressaoGeral.text("\r\n");
                            } else {
                                String produto2 = escolhido.getQtd() + "x " + escolhido.getP().getNome();
                                builderImpressaoGeral.text(produto2);
                                builderImpressaoGeral.text("\r\n");
                                ArrayList<AdicionalProduto> sabores = escolhido.getAdicionais(SaborPizza.class);
                                ArrayList<AdicionalProduto> bordas = escolhido.getAdicionais(BordaPizza.class);
                                if (sabores.size() > 0) {
                                    builderImpressaoGeral.text("Sabores:" + "\r\n");
                                    for (AdicionalProduto ad : sabores) {
                                        builderImpressaoGeral.text(ad.getNome() + "\r\n");
                                    }
                                }
                                if (bordas.size() > 0) {
                                    builderImpressaoGeral.text((getStringWithSpacer("Borda: ", bordas.get(0).getNome(), 10, ".")));
                                }
                                if (item.getComentario() != null && !item.getComentario().isEmpty()) {
                                    builderImpressaoGeral.text("\r\n");
                                    builderImpressaoGeral.text((getStringWithSpacer("Obs: ", escolhido.getComentario(), 10, ".")));
                                }
                            }
                            builderImpressaoGeral.text("\r\n");
                            builderImpressaoGeral.text((getStringWithSpacer("", "", 21, "-")));
                            builderImpressaoGeral.text("\r\n");
                        }
                    }
                } else {
                    if (!(item.getP() instanceof Pizza)) {
                        String produto = item.getP().getNomeWithCategories() + (item.getComentario().isEmpty() ? "" : " ( " + item.getComentario() + " ) ");
                        builderImpressaoGeral.text((getStringWithSpacer(item.getQtd() + "", produto, 20, ".")));
                        builderImpressaoGeral.text("\r\n");
                        if (item.getAdicionais().size() > 0) {
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
                            builderImpressaoGeral.text((getStringWithSpacer("Adicionais:", adicionais, 20, ".")));
                            builderImpressaoGeral.text("\r\n");
                        }
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                        builderImpressaoGeral.text((getStringWithSpacer("Valor:", "R$ " + moneyFormat.format(item.getSubTotal()), 42, ".")));
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DW);
                    } else {
                        builderImpressaoGeral.text((getStringWithSpacer(item.getQtd() + "", item.getP().getNomeWithCategories(), 24, ".")));
                        builderImpressaoGeral.text("\r\n");
                        ArrayList<AdicionalProduto> sabores = item.getAdicionais(SaborPizza.class);
                        ArrayList<AdicionalProduto> bordas = item.getAdicionais(BordaPizza.class);
                        if (sabores.size() > 0) {
                            builderImpressaoGeral.text("Sabores:" + "\r\n");
                            for (AdicionalProduto ad : sabores) {
                                builderImpressaoGeral.text(ad.getNome() + "\r\n");
                            }
                        }
                        if (bordas.size() > 0) {
                            builderImpressaoGeral.text((getStringWithSpacer("Borda: ", bordas.get(0).getNome(), 20, ".")));
                        }
                        if (item.getComentario() != null && !item.getComentario().isEmpty()) {
                            builderImpressaoGeral.text("\r\n");
                            builderImpressaoGeral.text((getStringWithSpacer("Obs: ", item.getComentario(), 20, ".")));
                        }
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                        builderImpressaoGeral.text((getStringWithSpacer("Valor:", "R$ " + moneyFormat.format(item.getSubTotal()), 42, ".")));
                        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DW);
                    }
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 21, "-")));
                    builderImpressaoGeral.text("\r\n");
                }
            }
        }
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.align(Align.LEFT);
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
        builderImpressaoGeral.text(getStringWithSpacer("Nome: ", p.getNomeCliente(), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Telefone: ", p.getCelular(), 42, "."));
        builderImpressaoGeral.text("\r\n");
        if (p.getNumeroMesa() == 0) {
            if (p.isEntrega()) {
                builderImpressaoGeral.text("Endereço para Entrega");
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "-")));
                builderImpressaoGeral.text(p.getEndereco());
                builderImpressaoGeral.text("\r\n");
                builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "-")));
                builderImpressaoGeral.text("\r\n");
            } else {
                builderImpressaoGeral.text("Retirar no Local");
            }
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "-")));
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text("\r\n");
            builderImpressaoGeral.text(getStringWithSpacer("Total: ", moneyFormat.format(p.getTotal()), 42, "."));
            builderImpressaoGeral.text("\r\n");
            if (p.isEntrega()) {
                if (p.isCartao()) {
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.EMPHASIZED);
                    builderImpressaoGeral.align(Align.CENTER);
                    builderImpressaoGeral.text("***Levar Maquina de Cartao***\r\n");
                    if (p.getTroco() == -1) {
                        builderImpressaoGeral.text("Obs: " + p.getComentarioPedido() + "\r\n");
                    }
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                    builderImpressaoGeral.align(Align.LEFT);
                } else if (p.getTroco() != 0) {
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.EMPHASIZED);
                    builderImpressaoGeral.text((getStringWithSpacer("Valor do Troco: ", "R$" + moneyFormat.format(p.getTroco() - p.getTotal()), 42, ".")));
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
                    builderImpressaoGeral.align(Align.LEFT);
                }
                if (p.getHoraAgendamento() != null) {
                    builderImpressaoGeral.text((getStringWithSpacer("Horario para Entrega: ", p.getHoraAgendamento().format(DateTimeFormatter.ofPattern("HH:mm")), 42, ".")));
                }
            } else {
                if (p.getHoraAgendamento() != null) {
                    builderImpressaoGeral.text((getStringWithSpacer("Horario para Retirada: ", p.getHoraAgendamento().format(DateTimeFormatter.ofPattern("HH:mm")), 42, ".")));
                }
            }
            builderImpressaoGeral.text("\r\n");
        }
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "#")));
        builderImpressaoGeral.feed(8).cut(Cut.FULL);
        byte[] textoGeral = removerAcentos(builderImpressaoGeral.toString()).getBytes();
        try {
            if (Configuracao.getInstance().isImpressaoHabilitada()) {
                PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                if (impressoraGeral == null) {
                    throw new Exception("Impressora Geral não instalada");
                }
                imprimirBytes(textoGeral, impressoraGeral);
            } else {
                System.out.println(builderImpressaoGeral.toString());
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean imprimir(Mesa m) {
        DecimalFormat moneyFormat = new DecimalFormat("###,###,###.00");
        EscPosBuilder builderImpressaoGeral = new EscPosBuilder().initialize();
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
        builderImpressaoGeral.text("Mesa: " + m.getNumeroMesa());
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.align(Align.LEFT);
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DW);
        synchronized (m.getPedidos()) {
            ArrayList<ItemPedido> aRemover = new ArrayList<>();
            ArrayList<ItemPedido> pedidosMesa = new ArrayList<>();
            for (Pedido p : m.getPedidos()) {
                for (int x = 0; x < p.getProdutos().size(); x++) {
                    ItemPedido item = p.getProdutos().get(x);
                    pedidosMesa.add(item);
                }
            }
            for (int x = 0; x < pedidosMesa.size(); x++) {
                ItemPedido item = pedidosMesa.get(x);
                Iterator<ItemPedido> iguais = pedidosMesa.stream().filter(o -> o.getP().equals(item.getP()) && item.getComentario().equals(((ItemPedido) o).getComentario()) && (CollectionUtils.isEqualCollection(((ItemPedido) o).getAdicionais(), item.getAdicionais()))).iterator();
                if (aRemover.contains(item)) {
                    continue;
                }
                if (iguais.hasNext()) {
                    ItemPedido itemBase = iguais.next();
                    while (iguais.hasNext()) {
                        ItemPedido atual = iguais.next();
                        itemBase.setQtd(itemBase.getQtd() + atual.getQtd());
                        aRemover.add(atual);
                    }
                }
            }
            for (Pedido p : m.getPedidos()) {
                p.getProdutos().removeAll(aRemover);
            }
            for (Pedido p : m.getPedidos()) {
                for (int y = 0; y < p.getProdutos().size(); y++) {
                    ItemPedido item = p.getProdutos().get(y);
                    if (aRemover.contains(item)) {
                        continue;
                    }
                    String produto = item.getQtd() + "x " + item.getP().getNome() + (item.getComentario().isEmpty() ? "" : " ( " + item.getComentario() + " ) ");
                    builderImpressaoGeral.text((getStringWithSpacer(produto, "R$" + moneyFormat.format(item.getSubTotal()), 10, ".")));
                    builderImpressaoGeral.text("\r\n");
                    if (!(item.getP() instanceof Pizza)) {
                        if (item.getAdicionais().size() > 0) {
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
                            builderImpressaoGeral.text((getStringWithSpacer("Adicionais: ", adicionais, 10, ".")));
                        }
                    } else {
                        ArrayList<AdicionalProduto> sabores = item.getAdicionais(SaborPizza.class);
                        ArrayList<AdicionalProduto> bordas = item.getAdicionais(BordaPizza.class);
                        if (sabores.size() > 0) {
                            String adicionais = "";
                            for (int x = 0; x < sabores.size(); x++) {
                                AdicionalProduto adicional = sabores.get(x);
                                adicionais += adicional.getNome();
                                if (x < item.getAdicionais().size() - 1) {
                                    adicionais += ",";
                                }
                            }
                            if (adicionais.endsWith(",")) {
                                adicionais = adicionais.substring(0, adicionais.lastIndexOf(","));
                            }
                            builderImpressaoGeral.text((getStringWithSpacer("Sabores: ", adicionais, 10, "."))).feed(1);
                        }
                        if (bordas.size() > 0) {
                            builderImpressaoGeral.text((getStringWithSpacer("Borda: ", bordas.get(0).getNome(), 10, ".")));
                        }
                    }
                    builderImpressaoGeral.text("\r\n");
                    builderImpressaoGeral.text((getStringWithSpacer("", "", 21, "-")));
                    builderImpressaoGeral.text("\r\n");
                }
            }
        }
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.align(Align.LEFT);
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR);
        builderImpressaoGeral.text(getStringWithSpacer("Total: ", moneyFormat.format(m.getTotal()), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "#")));
        builderImpressaoGeral.feed(5).cut(Cut.FULL);
        byte[] textoGeral = removerAcentos(builderImpressaoGeral.toString()).getBytes();
        try {
            if (Configuracao.getInstance().isImpressaoHabilitada()) {
                PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                if (impressoraGeral == null) {
                    throw new Exception("Impressora Geral não instalada");
                }
                imprimirBytes(textoGeral, impressoraGeral);
            } else {
                System.out.println(builderImpressaoGeral.toString());
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean imprimir(Reserva r) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
        EscPosBuilder builderImpressaoGeral = new EscPosBuilder().initialize();
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.DH);
        builderImpressaoGeral.align(Align.CENTER);
        builderImpressaoGeral.text("SOLICITAÇÃO DE RESERVA");
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.align(Align.LEFT);
        builderImpressaoGeral.font(email.com.gmail.ttsai0509.escpos.command.Font.REGULAR).feed(2);
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Nome Contato: ", r.getNomeContato(), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Telefone Contato: ", r.getTelefoneContato(), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Número de Pessoas: ", r.getQtdPessoas() + "", 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Data Reserva: ", dateFormat.format(r.getDataReserva()), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text(getStringWithSpacer("Horario Reserva: ", timeFormat.format(r.getDataReserva()), 42, "."));
        builderImpressaoGeral.text("\r\n");
        builderImpressaoGeral.text((getStringWithSpacer("", "", 42, "#")));
        builderImpressaoGeral.feed(8).cut(Cut.FULL);
        byte[] textoGeral = removerAcentos(builderImpressaoGeral.toString()).getBytes();
        try {
            if (Configuracao.getInstance().isImpressaoHabilitada()) {
                PrintService impressoraGeral = selectImpress(Configuracao.getInstance().getNomeImpressora());
                if (impressoraGeral == null) {
                    throw new Exception("Impressora Geral não instalada");
                }
                imprimirBytes(textoGeral, impressoraGeral);
            } else {
                System.out.println(builderImpressaoGeral.toString());
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private PrintService selectImpress(String imp) {
        PrintService[] ps = PrintServiceLookup.lookupPrintServices(
                DocFlavor.INPUT_STREAM.AUTOSENSE, null);
        for (PrintService p : ps) {
            if (p.getName().equals(imp)) {
                return p;
            }
        }
        return null;
    }

    private String getStringWithSpacer(String string1, String string2, int width, String spacer) {
        String resultado = removerAcentos(string1);
        while ((resultado + string2).length() < width) {
            resultado += spacer;
        }
        return removerAcentos(resultado + string2);
        //return string1+"..."+string2;
    }

    private static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    private void imprimirBytes(byte[] texto, PrintService impressora) throws Exception {
        DocPrintJob dpj = impressora.createPrintJob();
        SimpleDoc dimpDoc = new SimpleDoc(texto, DocFlavor.BYTE_ARRAY.AUTOSENSE, null);
        dpj.print(dimpDoc, null);
    }
}
