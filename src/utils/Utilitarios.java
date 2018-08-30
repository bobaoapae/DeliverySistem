/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package utils;

import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import javax.swing.text.JTextComponent;

/**

 @author BOV-INOS
 */
public class Utilitarios {

    public static Thread runInThread(Runnable r) {
        Thread t = new Thread(r);
        t.start();
        return t;
    }

    public static String fileToBase64(File file) throws IOException {
        String contentType = Files.probeContentType(file.toPath());

        // read data as byte[]
        byte[] data = Files.readAllBytes(file.toPath());

        // convert byte[] to base64(java7)
        //String base64str = DatatypeConverter.printBase64Binary(data);
        // convert byte[] to base64(java8)
        String base64str = Base64.getEncoder().encodeToString(data);

        // cretate "data URI"
        StringBuilder sb = new StringBuilder();
        sb.append("data:");
        sb.append(contentType);
        sb.append(";base64,");
        sb.append(base64str);
        return sb.toString();
    }

    public static String getText(URL website) throws Exception {
        URLConnection connection = website.openConnection();
        connection.addRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        in.close();

        return response.toString();
    }

    public static void replaceAllNoDigit(JTextComponent iin) {
        String in = iin.getText();
        if (in.isEmpty()) {
            return;
        }
        char[] chars = in.toCharArray();
        String s = "";
        for (char i : chars) {
            if (Character.isDigit(i)) {
                s += i;
            }
        }
        iin.setText(s);
    }

    public static String replaceAllNoDigit(String iin) {
        String in = iin;
        if (in.isEmpty()) {
            return "";
        }
        char[] chars = in.toCharArray();
        String s = "";
        for (char i : chars) {
            if (Character.isDigit(i)) {
                s += i;
            }
        }
        return s;
    }

    public static void formatMoneyValue(JTextComponent iin) {
        String in = iin.getText();
        in = in.replaceAll("\\.", "");
        in = in.replaceAll(",", ".");
        DecimalFormat formatter = new DecimalFormat("###,###,###.00");
        if (in.isEmpty()) {
            iin.setText("0,00");
            return;
        }
        iin.setText(formatter.format(Double.parseDouble(in)));
    }

    public static void validateNumberOnly(KeyEvent evt) {
        if (!Character.isDigit(evt.getKeyChar())) {
            evt.consume();
        }
    }

    public static void validateNumberFloatOnly(KeyEvent evt) {
        if (Character.isDigit(evt.getKeyChar())) {
            return;
        }
        if (evt.getKeyChar() != ',') {
            evt.consume();
            return;
        }
        if (evt.getComponent() instanceof JTextComponent) {
            JTextComponent comp = ((JTextComponent) evt.getComponent());
            if (evt.getKeyChar() == ',') {
                if (comp.getText().indexOf(",") != -1) {
                    evt.consume();
                    return;
                }
            }
        }
    }

    public static String plainText(String input) {
        return input.replaceAll("[^0-9]", "");
    }
    
    public static String plainTextAlfabeto(String input) {
        return input.replaceAll("[^a-zA-Z]", "");
    }

    public static String convertToString(Date d, String formato) {
        SimpleDateFormat formatador = new SimpleDateFormat(formato);
        return formatador.format(d);
    }

    public static String convertToString(Date d) {
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy");
        return formatador.format(d);
    }

    public static void validateMaxInput(KeyEvent iin, int max) {
        if (iin.getComponent() instanceof JTextComponent) {
            if (((JTextComponent) iin.getComponent()).getText().length() >= max) {
                iin.consume();
            }
        }
    }

    public static void validateMoneyValue(KeyEvent evt) {
        if (evt.getComponent() instanceof JTextComponent) {
            JTextComponent comp = ((JTextComponent) evt.getComponent());
            if (Character.isDigit(evt.getKeyChar())) {
                return;
            }
            if (evt.getKeyChar() != '.' && evt.getKeyChar() != ',') {
                evt.consume();
                return;
            }
            if (evt.getKeyChar() == ',') {
                if (comp.getText().indexOf(",") != -1) {
                    evt.consume();
                    return;
                }
                int posicaoCursor = comp.getCaretPosition();
                if (comp.getText().indexOf(".") >= posicaoCursor || comp.getText().indexOf(".") + 1 >= posicaoCursor) {
                    evt.consume();
                    return;
                }
            }
            if (evt.getKeyChar() == '.') {
                int posicaoCursor = comp.getCaretPosition();
                if (comp.getText().indexOf(",") != -1 && comp.getText().indexOf(",") <= posicaoCursor) {
                    evt.consume();
                    return;
                }
            }
        }
    }

    public static byte[] toMD5(String s) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytesOfMessage = s.getBytes("UTF-8");

        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] thedigest = md.digest(bytesOfMessage);
        return thedigest;
    }
}
