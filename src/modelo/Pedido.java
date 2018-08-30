/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import com.br.joao.Db4oGenerico;
import controle.ControlePromocoes;
import driver.WebWhatsDriver;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;

/**

 @author jvbor
 */
public class Pedido extends Db4ObjectSaveGeneric {

    private String endereco, nomeCliente;
    private boolean entrega, cartao, impresso, incluirNoRelatorio;
    private double troco, desconto, pgCreditos;
    private int numeroMesa;
    private String chatId, comentarioPedido;
    private Date dataPedido;
    private EstadoPedido estadoPedido;
    private String celular, fixo;
    private Endereco enderecoCompleto;
    private LocalTime horaAgendamento;
    private ArrayList<ItemPedido> produtos;
    private transient List<ItemPedido> produtosSync;
    private Cliente c;

    public Pedido() {
        produtos = new ArrayList();
        incluirNoRelatorio = true;
        dataPedido = new Date();
        estadoPedido = EstadoPedido.Novo;
        celular = "";
        fixo = "";
        comentarioPedido = "";
    }

    public Cliente getC() {
        return c;
    }

    public void setC(Cliente c) {
        this.c = c;
    }

    public LocalTime getHoraAgendamento() {
        return horaAgendamento;
    }

    public void setHoraAgendamento(LocalTime horaAgendamento) {
        this.horaAgendamento = horaAgendamento;
    }

    public double getDesconto() {
        return desconto;
    }

    public void setDesconto(double desconto) {
        this.desconto = desconto;
    }

    public Endereco getEnderecoCompleto() {
        return enderecoCompleto;
    }

    public void setEnderecoCompleto(Endereco enderecoCompleto) {
        this.enderecoCompleto = enderecoCompleto;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getFixo() {
        return fixo;
    }

    public void setFixo(String fixo) {
        this.fixo = fixo;
    }

    public EstadoPedido getEstadoPedido() {
        return estadoPedido;
    }

    public void setEstadoPedido(EstadoPedido estadoPedido) {
        this.estadoPedido = estadoPedido;
    }

    public Date getDataPedido() {
        return dataPedido;
    }

    public void setDataPedido(Date dataPedido) {
        this.dataPedido = dataPedido;
    }

    public String getComentarioPedido() {
        return comentarioPedido;
    }

    public void setComentarioPedido(String comentarioPedido) {
        this.comentarioPedido = comentarioPedido;
    }

    public String getChatId() {
        return chatId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public Chat getChat(WebWhatsDriver driver) {
        return driver.getFunctions().getChatById(chatId);
    }

    public void setChat(ChatBot chat) {
        this.chatId = chat.getChat().getId();
    }

    public void addItemPedido(ItemPedido item) {
        synchronized (getProdutos()) {
            Iterator<ItemPedido> iguais = getProdutos().stream().filter(o -> o.getP().equals(item.getP()) && item.getComentario().equals(((ItemPedido) o).getComentario()) && CollectionUtils.isEqualCollection(((ItemPedido) o).getAdicionais(), item.getAdicionais())).iterator();
            if (iguais.hasNext()) {
                ItemPedido itemBase = iguais.next();
                itemBase.setQtd(itemBase.getQtd() + item.getQtd());
            } else {
                getProdutos().add(item);
            }
        }
        //atualizarDesconto();
    }

    public void atualizarDesconto() {
        HashMap<Produto, Integer> hashMap = new HashMap<>();
        desconto = 0;
        synchronized (this.getProdutos()) {
            for (ItemPedido iitem : Collections.unmodifiableList(this.getProdutos())) {
                ArrayList<Promocao> promocoes = ControlePromocoes.getInstance(Db4oGenerico.getInstance("banco")).promocoesProduto(iitem.getP());
                for (Promocao promo : promocoes) {
                    for (CategoriaPromocao catPro : promo.getCategoriasPromocao()) {
                        if (catPro == CategoriaPromocao.POR_QUANTIDADE) {
                            if (!hashMap.containsKey(iitem.getP())) {
                                hashMap.put(iitem.getP(), iitem.getQtd());
                            } else {
                                hashMap.put(iitem.getP(), hashMap.get(iitem.getP()) + iitem.getQtd());
                            }
                            while (hashMap.get(iitem.getP()) >= promo.getQtd()) {
                                desconto += promo.getValor();
                                hashMap.put(iitem.getP(), hashMap.get(iitem.getP()) - promo.getQtd());
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean isIncluirNoRelatorio() {
        return incluirNoRelatorio;
    }

    public void setIncluirNoRelatorio(boolean incluirNoRelatorio) {
        this.incluirNoRelatorio = incluirNoRelatorio;
    }

    public int getNumeroMesa() {
        return numeroMesa;
    }

    public void setNumeroMesa(int numeroMesa) {
        this.numeroMesa = numeroMesa;
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
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public boolean isEntrega() {
        return entrega;
    }

    public void setEntrega(boolean entrega) {
        this.entrega = entrega;
    }

    public boolean isCartao() {
        return cartao;
    }

    public void setCartao(boolean cartao) {
        this.cartao = cartao;
    }

    public boolean isImpresso() {
        return impresso;
    }

    public void setImpresso(boolean impresso) {
        this.impresso = impresso;
    }

    public double getTroco() {
        return troco;
    }

    public void setTroco(double troco) {
        this.troco = troco;
    }

    public double getTotal() {
        double total = 0;
        synchronized (getProdutos()) {
            for (ItemPedido p : getProdutos()) {
                total += p.getSubTotal();
            }
        }
        return total - desconto - pgCreditos;
    }

    private List<ItemPedido> getProdutosSync() {
        if (produtosSync == null) {
            produtosSync = Collections.synchronizedList(produtos);
        }
        return produtosSync;
    }

    public double getPgCreditos() {
        return pgCreditos;
    }

    public void setPgCreditos(double pgCreditos) {
        this.pgCreditos = pgCreditos;
    }

    public List<ItemPedido> getProdutos() {
        synchronized (getProdutosSync()) {
            getProdutosSync().removeIf(o -> ((ItemPedido) o).getP() == null);
            Collections.sort(getProdutosSync());
            return getProdutosSync();
        }
    }

    public List<ItemPedido> getProdutos(Categoria c) {
        synchronized (getProdutosSync()) {
            List<ItemPedido> lista = new ArrayList<>();
            for (ItemPedido i : getProdutos()) {
                if (i.getP().getCategoria().equals(c) || i.getP().getCategoria().getRootCategoria().equals(c)) {
                    lista.add(i);
                }
            }
            return lista;
        }
    }

    public void setProdutos(ArrayList<ItemPedido> produtos) {
        this.produtos = produtos;
        this.produtosSync = Collections.synchronizedList(produtos);
    }

    public enum EstadoPedido {
        Concluido, Cancelado, SaiuEntrega, EmProducao, Novo, AguardandoRetirada
    }

}
