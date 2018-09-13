/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import modelo.Configuracao;

/**

 @author jvbor
 */
public class CheckForUpdate {

    public CheckForUpdate(String url) {
        checkNewVersion(url);
    }

    private void checkNewVersion(String url) {
        try {
            File ignoreUpdate = new File("ignore.txt");
            if (ignoreUpdate.exists()) {
                return;
            }
            File versionCheck = new File("update.txt");
            if (!versionCheck.exists()) {
                doUpdate(url);
                return;
            }
            String hashLocal = new String(Files.readAllBytes(Paths.get("update.txt")));
            String hashExterna = getText(url + "hash.php");
            if (hashExterna.isEmpty()) {
                return;
            }
            if (!hashLocal.equals(hashExterna)) {
                doUpdate(url);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Logger.getLogger(CheckForUpdate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getText(String url) throws Exception {
        URL website = new URL(url);
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

    private void doUpdate(String url) throws IOException, URISyntaxException {
        if (Configuracao.getInstance().isOpenPedidos()) {
            int result = JOptionPane.showConfirmDialog(null, "Uma atualização do sistema esta disponivel. Deseja atualizar agora?", "Atenção!!", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
                System.out.println("\"" + javaBin + "\"" + " -jar \"" + new File("").getAbsolutePath() + File.separator + "Atualizador.jar\" \"" + url + "\" \"" + new File("").getAbsolutePath() + File.separator + "SistemaDeliveryWhatsApp.jar\"");
                Runtime.getRuntime().exec("\"" + javaBin + "\"" + " -jar \"" + new File("").getAbsolutePath() + File.separator + "Atualizador.jar\" \"" + url + "\" \"" + new File("").getAbsolutePath() + File.separator + "SistemaDeliveryWhatsApp.jar\"");
                System.exit(0);
            }
        } else if (Configuracao.getInstance().getHoraUltimaMsg() == null || new Date().getTime() - Configuracao.getInstance().getHoraUltimaMsg().getTime() >= 1 * 60 * 60 * 1000) {
            final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
            System.out.println("\"" + javaBin + "\"" + " -jar \"" + new File("").getAbsolutePath() + File.separator + "Atualizador.jar\" \"" + url + "\" \"" + new File("").getAbsolutePath() + File.separator + "SistemaDeliveryWhatsApp.jar\"");
            Runtime.getRuntime().exec("\"" + javaBin + "\"" + " -jar \"" + new File("").getAbsolutePath() + File.separator + "Atualizador.jar\" \"" + url + "\" \"" + new File("").getAbsolutePath() + File.separator + "SistemaDeliveryWhatsApp.jar\"");
            System.exit(0);
        }
    }

}
