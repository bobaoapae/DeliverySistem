/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

/**

 @author jvbor
 */
public class Pizza extends Produto {

    private int qtdSabores;
    private int qtdPedacos;
    private boolean canHaveBorda;
    private double valorEspecial;

    public double getValorEspecial() {
        return valorEspecial;
    }

    public void setValorEspecial(double valorEspecial) {
        this.valorEspecial = valorEspecial;
    }

    public int getQtdPedacos() {
        return qtdPedacos;
    }

    public void setQtdPedacos(int qtdPedacos) {
        this.qtdPedacos = qtdPedacos;
    }

    public boolean isCanHaveBorda() {
        return canHaveBorda;
    }

    public void setCanHaveBorda(boolean canHaveBorda) {
        this.canHaveBorda = canHaveBorda;
    }

    public int getQtdSabores() {
        return qtdSabores;
    }

    public void setQtdSabores(int qtdSabores) {
        this.qtdSabores = qtdSabores;
    }

    @Override
    public String getNome() {
        if (!nome.contains("Pizza")) {
            return "Pizza - " + super.getNome(); //To change body of generated methods, choose Tools | Templates.
        } else {
            return super.getNome();
        }
    }

    @Override
    public int sequenceNr() {
        return 2;
    }

}
