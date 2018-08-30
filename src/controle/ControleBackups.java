/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package controle;

import com.br.joao.Db4oGenerico;
import com.db4o.io.FileStorage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**

 @author jvbor
 */
public class ControleBackups {

    private static ControleBackups instance;
    private Db4oGenerico db4oGenerico;

    private ControleBackups(Db4oGenerico db4o) {
        this.db4oGenerico = db4o;
    }

    public static ControleBackups getInstance(Db4oGenerico db4o) {
        if (instance == null) {
            instance = new ControleBackups(db4o);
        }
        return instance;
    }

    public boolean realizarBackup() {
        try {
            File directory = new File("backups/");
            if (!directory.exists()) {
                directory.mkdir();
            }
            for (File file : directory.listFiles()) {
                long diff = new Date().getTime() - file.lastModified();
                if (diff > 10 * 24 * 60 * 60 * 1000) {
                    file.delete();
                }
            }
            System.out.println(new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date()));
            db4oGenerico.getDb().ext().backup(new FileStorage(),"backups/backup-" + new SimpleDateFormat("dd-MM-yyyy HH-mm").format(new Date()));
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
