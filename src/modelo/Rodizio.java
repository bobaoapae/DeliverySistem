/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import java.time.LocalTime;

/**

 @author SYSTEM
 */
public class Rodizio extends Produto {

    private LocalTime horaInicio;
    private boolean diasSemana[];

    public Rodizio() {
        this.diasSemana = new boolean[]{false, false, false, false, false, false, false};
    }

    public void setDomingo(boolean flag) {
        this.diasSemana[0] = flag;
    }

    public void setSegunda(boolean flag) {
        this.diasSemana[1] = flag;
    }

    public void setTerca(boolean flag) {
        this.diasSemana[2] = flag;
    }

    public void setQuarta(boolean flag) {
        this.diasSemana[3] = flag;
    }

    public void setQuinta(boolean flag) {
        this.diasSemana[4] = flag;
    }

    public void setSexta(boolean flag) {
        this.diasSemana[5] = flag;
    }

    public void setSabado(boolean flag) {
        this.diasSemana[6] = flag;
    }

    public boolean isDomingo() {
        return this.diasSemana[0];
    }

    public boolean isSegunda() {
        return this.diasSemana[1];
    }

    public boolean isTerca() {
        return this.diasSemana[2];
    }

    public boolean isQuarta() {
        return this.diasSemana[3];
    }

    public boolean isQuinta() {
        return this.diasSemana[4];
    }

    public boolean isSexta() {
        return this.diasSemana[5];
    }

    public boolean isSabado() {
        return this.diasSemana[6];
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public boolean[] getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(boolean[] diasSemana) {
        this.diasSemana = diasSemana;
    }

    @Override
    public String getNome() {
        if (!nome.contains("Rodizio")) {
            return "Rodizio - " + super.getNome(); //To change body of generated methods, choose Tools | Templates.
        } else {
            return super.getNome();
        }
    }

}
