/*
 To change this license header, choose License Headers in Project Properties.
 To change this template file, choose Tools | Templates
 and open the template in the editor.
 */
package visaoWeb;

import com.teamdev.jxbrowser.chromium.BrowserContext;
import com.teamdev.jxbrowser.chromium.BrowserContextParams;

/**

 @author jvbor
 */
public class ContextManager {

    private static ContextManager instance;
    private BrowserContext context;

    private ContextManager() {
        context = new BrowserContext(
                new BrowserContextParams("cache/" + this.getClass().getName()));
    }

    public static ContextManager getInstance() {
        if (instance == null) {
            instance = new ContextManager();
        }
        return instance;
    }

    public BrowserContext getContext() {
        return context;
    }

}
