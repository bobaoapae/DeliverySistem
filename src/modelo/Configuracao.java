/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.time.LocalTime;
import java.util.Date;

/**

 @author SYSTEM
 */
public class Configuracao extends Db4ObjectSaveGeneric {

    private String nomeEstabelecimento, nomeBot, img, nomeImpressora, numeroAviso;
    private double valorSelo;
    private int maximoSeloPorCompra, validadeSeloFidelidade, tempoMedioRetirada, tempoMedioEntrega;
    private static Configuracao instance;
    private boolean openPedidos, openChatBot, impressaoHabilitada, reservas, reservasComPedidosFechados, abrirFecharPedidosAutomatico, agendamentoDePedidos;
    private Date horaAberturaPedidos;
    private LocalTime horaAutomaticaAbrirPedidos, horaAutomaticaFecharPedidos, horaInicioReservas;

    public int getTempoMedioRetirada() {
        return tempoMedioRetirada;
    }

    public void setTempoMedioRetirada(int tempoMedioRetirada) {
        this.tempoMedioRetirada = tempoMedioRetirada;
    }

    public int getTempoMedioEntrega() {
        return tempoMedioEntrega;
    }

    public void setTempoMedioEntrega(int tempoMedioEntrega) {
        this.tempoMedioEntrega = tempoMedioEntrega;
    }

    public boolean isTimeBeetwenHorarioFuncionamento(LocalTime horaInformada) {
        if (this.getHoraAutomaticaAbrirPedidos().isAfter(this.getHoraAutomaticaFecharPedidos())) {
            if (!(horaInformada.isBefore(this.getHoraAutomaticaAbrirPedidos()) && horaInformada.isAfter(this.getHoraAutomaticaFecharPedidos()))) {
                return true;
            }
        } else {
            if (horaInformada.isAfter(this.getHoraAutomaticaAbrirPedidos()) && horaInformada.isBefore(this.getHoraAutomaticaFecharPedidos())) {
                return true;
            }
        }
        return false;
    }

    public boolean isAgendamentoDePedidos() {
        return agendamentoDePedidos;
    }

    public void setAgendamentoDePedidos(boolean agendamentoDePedidos) {
        this.agendamentoDePedidos = agendamentoDePedidos;
    }

    public String getNumeroAviso() {
        return numeroAviso;
    }

    public void setNumeroAviso(String numeroAviso) {
        this.numeroAviso = numeroAviso;
    }

    public int getValidadeSeloFidelidade() {
        return validadeSeloFidelidade;
    }

    public void setValidadeSeloFidelidade(int validadeSeloFidelidade) {
        this.validadeSeloFidelidade = validadeSeloFidelidade;
    }

    public boolean isReservas() {
        return reservas;
    }

    public void setReservas(boolean reservas) {
        this.reservas = reservas;
    }

    public boolean isReservasComPedidosFechados() {
        return reservasComPedidosFechados;
    }

    public void setReservasComPedidosFechados(boolean reservasComPedidosFechados) {
        this.reservasComPedidosFechados = reservasComPedidosFechados;
    }

    public boolean isAbrirFecharPedidosAutomatico() {
        return abrirFecharPedidosAutomatico;
    }

    public void setAbrirFecharPedidosAutomatico(boolean abrirFecharPedidosAutomatico) {
        this.abrirFecharPedidosAutomatico = abrirFecharPedidosAutomatico;
    }

    public LocalTime getHoraAutomaticaAbrirPedidos() {
        return horaAutomaticaAbrirPedidos;
    }

    public void setHoraAutomaticaAbrirPedidos(LocalTime horaAutomaticaAbrirPedidos) {
        this.horaAutomaticaAbrirPedidos = horaAutomaticaAbrirPedidos;
    }

    public LocalTime getHoraAutomaticaFecharPedidos() {
        return horaAutomaticaFecharPedidos;
    }

    public void setHoraAutomaticaFecharPedidos(LocalTime horaAutomaticaFecharPedidos) {
        this.horaAutomaticaFecharPedidos = horaAutomaticaFecharPedidos;
    }

    public LocalTime getHoraInicioReservas() {
        return horaInicioReservas;
    }

    public void setHoraInicioReservas(LocalTime horaInicioReservas) {
        this.horaInicioReservas = horaInicioReservas;
    }

    public String getNomeImpressora() {
        return nomeImpressora;
    }

    public void setNomeImpressora(String nomeImpressora) {
        this.nomeImpressora = nomeImpressora;
    }

    public boolean isImpressaoHabilitada() {
        return impressaoHabilitada;
    }

    public void setImpressaoHabilitada(boolean impressaoHabilitada) {
        this.impressaoHabilitada = impressaoHabilitada;
    }

    public boolean isOpenChatBot() {
        return openChatBot;
    }

    public void setOpenChatBot(boolean openChatBot) {
        this.openChatBot = openChatBot;
    }

    public boolean isOpenPedidos() {
        return openPedidos;
    }

    public void setOpenPedidos(boolean openPedidos) {
        this.openPedidos = openPedidos;
    }

    public void abrirPedidos() {
        if (!this.openPedidos) {
            this.openPedidos = true;
            this.horaAberturaPedidos = new Date();
        }
    }

    public void fecharPedidos() {
        this.openPedidos = false;
    }

    public Date getHoraAberturaPedidos() {
        return horaAberturaPedidos;
    }

    public void setHoraAberturaPedidos(Date horaAberturaPedidos) {
        this.horaAberturaPedidos = horaAberturaPedidos;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getNomeBot() {
        return nomeBot;
    }

    public void setNomeBot(String nomeBot) {
        this.nomeBot = nomeBot;
    }

    public static Configuracao getInstance() {
        if (instance == null) {
            instance = new Configuracao();
        }
        return instance;
    }

    public static void setInstance(Configuracao instance) {
        Configuracao.instance = instance;
    }

    public String getNomeEstabelecimento() {
        return nomeEstabelecimento;
    }

    public void setNomeEstabelecimento(String nomeEstabelecimento) {
        this.nomeEstabelecimento = nomeEstabelecimento;
    }

    public double getValorSelo() {
        return valorSelo;
    }

    public void setValorSelo(double valorSelo) {
        this.valorSelo = valorSelo;
    }

    public int getMaximoSeloPorCompra() {
        return maximoSeloPorCompra;
    }

    public void setMaximoSeloPorCompra(int maximoSeloPorCompra) {
        this.maximoSeloPorCompra = maximoSeloPorCompra;
    }

}
