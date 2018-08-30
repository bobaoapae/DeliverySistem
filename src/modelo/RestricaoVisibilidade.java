/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package modelo;

import java.time.LocalTime;

/**

 @author jvbor
 */
public class RestricaoVisibilidade {
    
    public LocalTime horarioDe, horarioAte;
    public boolean restricaoHorario, restricaoDia;
    public boolean[] diasSemana;
    
    public RestricaoVisibilidade() {
        diasSemana = new boolean[]{false, false, false, false, false, false, false};
    }
    
    public boolean[] getDiasSemana() {
        return diasSemana;
    }
    
    public void setDiasSemana(boolean[] diasSemana) {
        this.diasSemana = diasSemana;
    }
    
    public LocalTime getHorarioDe() {
        return horarioDe;
    }
    
    public void setHorarioDe(LocalTime horarioDe) {
        this.horarioDe = horarioDe;
    }
    
    public LocalTime getHorarioAte() {
        return horarioAte;
    }
    
    public void setHorarioAte(LocalTime horarioAte) {
        this.horarioAte = horarioAte;
    }
    
    public boolean isRestricaoHorario() {
        return restricaoHorario;
    }
    
    public void setRestricaoHorario(boolean restricaoHorario) {
        this.restricaoHorario = restricaoHorario;
    }
    
    public boolean isRestricaoDia() {
        return restricaoDia;
    }
    
    public void setRestricaoDia(boolean restricaoDia) {
        this.restricaoDia = restricaoDia;
    }
    
}
