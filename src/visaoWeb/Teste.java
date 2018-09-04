/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import java.util.logging.Level;
import java.util.logging.Logger;

/**

 @author SYSTEM
 */
public class Teste {
    
    public synchronized void doNothing(){
        System.out.println("a");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Teste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void doNothing2(){
        System.out.println("a");
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Teste.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        Teste t = new Teste();
        new Thread(){
            public void run(){
                t.doNothing();
            }
        }.start();
        new Thread(){
            public void run(){
                t.doNothing2();
            }
        }.start();
    }
}
