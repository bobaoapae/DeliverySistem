/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import utils.DateUtils;

/**

 @author jvbor
 */
public class Cliente extends Db4ObjectSaveGeneric {

    private String nome, chatId, endereco, telefoneMovel, telefoneFixo;
    private Date dataAniversario;
    private Date dataCadastro;
    private Date dataUltimaCompra;
    private ArrayList<Pedido> pedidosCliente;
    private ArrayList<SeloFidelidade> selosFidelidade;
    private Endereco enderecoCompleto;
    private boolean cadastroRealizado;
    private ArrayList<RecargaCliente> regargas;
    private double creditosDisponiveis;

    public Cliente(String chatId) {
        this();
        this.chatId = chatId;
    }

    public Cliente() {
        this.dataCadastro = Calendar.getInstance().getTime();
        this.pedidosCliente = new ArrayList<>();
        this.selosFidelidade = new ArrayList<>();
        this.regargas = new ArrayList<>();
    }

    public void realizarRecarga(double valorRecarga) {
        this.creditosDisponiveis += valorRecarga;
        this.getRegargas().add(new RecargaCliente(valorRecarga));
    }

    public ArrayList<RecargaCliente> getRegargas() {
        if (this.regargas == null) {
            this.regargas = new ArrayList<>();
        }
        return regargas;
    }

    public void setRegargas(ArrayList<RecargaCliente> regargas) {
        this.regargas = regargas;
    }

    public double getCreditosDisponiveis() {
        return creditosDisponiveis;
    }

    public void setCreditosDisponiveis(double creditosDisponiveis) {
        this.creditosDisponiveis = creditosDisponiveis;
    }

    public String getTelefoneFixo() {
        if (telefoneFixo == null) {
            return "";
        }
        return telefoneFixo;
    }

    public void setTelefoneFixo(String telefoneFixo) {
        this.telefoneFixo = telefoneFixo;
    }

    public String getTelefoneMovel() {
        if (telefoneMovel == null) {
            return "";
        }
        return telefoneMovel;
    }

    public void setTelefoneMovel(String telefoneMovel) {
        this.telefoneMovel = telefoneMovel;
    }

    public boolean isCadastroRealizado() {
        return cadastroRealizado;
    }

    public void setCadastroRealizado(boolean cadastroRealizado) {
        this.cadastroRealizado = cadastroRealizado;
    }

    public Endereco getEnderecoCompleto() {
        return enderecoCompleto;
    }

    public void setEnderecoCompleto(Endereco enderecoCompleto) {
        this.enderecoCompleto = enderecoCompleto;
    }

    public ArrayList<SeloFidelidade> getSelosFidelidade() {
        if(selosFidelidade==null){
            selosFidelidade = new ArrayList<>();
        }
        if (Configuracao.getInstance().getValidadeSeloFidelidade() != -1) {
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DAY_OF_YEAR, -Configuracao.getInstance().getValidadeSeloFidelidade());
            selosFidelidade.removeIf(o -> !DateUtils.isSameDay(c.getTime(), o.getDataSelo()) && DateUtils.isBeforeDay(o.getDataSelo(), c.getTime()));
        }
        return selosFidelidade;
    }

    public void setSelosFidelidade(ArrayList<SeloFidelidade> selosFidelidade) {
        this.selosFidelidade = selosFidelidade;
    }

    public Date getDataAniversario() {
        return dataAniversario;
    }

    public void setDataAniversario(Date dataAniversario) {
        this.dataAniversario = dataAniversario;
    }

    public String getNome() {
        if (nome == null) {
            return "";
        }
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getEndereco() {
        if (enderecoCompleto != null) {
            String endereco = "";
            endereco += "Bairro: " + enderecoCompleto.getBairro() + "\r\n";
            endereco += "Logradouro: " + enderecoCompleto.getRua() + "\r\n";
            endereco += "Nr: " + enderecoCompleto.getNumero() + "\r\n";
            endereco += "Referencia: " + enderecoCompleto.getReferencia() + "\r\n";
            return endereco;
        }
        if (endereco == null) {
            return "";
        }
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Date getDataCadastro() {
        return dataCadastro;
    }

    public void setDataCadastro(Date dataCadastro) {
        this.dataCadastro = dataCadastro;
    }

    public Date getDataUltimaCompra() {
        return dataUltimaCompra;
    }

    public void setDataUltimaCompra(Date dataUltimaCompra) {
        this.dataUltimaCompra = dataUltimaCompra;
    }

    public ArrayList<Pedido> getPedidosCliente() {
        if (pedidosCliente == null) {
            pedidosCliente = new ArrayList<>();
        }
        return pedidosCliente;
    }

    public void realizaCompra(Pedido p) {
        this.dataUltimaCompra = Calendar.getInstance().getTime();
        p.setC(this);
        this.getPedidosCliente().add(p);
    }

    public void adicionarSelos(Pedido p) {
        double total = p.getTotal();
        for (int x = 0; x < Configuracao.getInstance().getMaximoSeloPorCompra(); x++) {
            if (total >= Configuracao.getInstance().getValorSelo()) {
                total -= Configuracao.getInstance().getValorSelo();
                this.getSelosFidelidade().add(new SeloFidelidade());
            }
        }
    }

}
