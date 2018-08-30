/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import com.br.joao.Db4ObjectSaveGeneric;
import java.util.Date;

/**

 @author SYSTEM
 */
public class RecargaCliente extends Db4ObjectSaveGeneric {

    private Date dataRecarga;
    private double valorRecarga;

    public RecargaCliente(double valorRecarga) {
        this.valorRecarga = valorRecarga;
        dataRecarga = new Date();
    }

    public Date getDataRecarga() {
        return dataRecarga;
    }

    public void setDataRecarga(Date dataRecarga) {
        this.dataRecarga = dataRecarga;
    }

    public double getValorRecarga() {
        return valorRecarga;
    }

    public void setValorRecarga(double valorRecarga) {
        this.valorRecarga = valorRecarga;
    }

}
