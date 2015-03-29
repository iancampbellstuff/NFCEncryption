package edu.gsu.cs.nfcencryption;

import android.app.Application;
import android.content.Context;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class ApplicationContext extends Application {

    /**
     * <em>Singleton</em> instance.
     */
    private static ApplicationContext INSTANCE;

    /**
     * Empty implementation, required by the <code>Android OS</code>.
     */
    public ApplicationContext() {
    }

    /**
     *
     */
    @Override
    public void onCreate() {
        super.onCreate();

        if (INSTANCE == null) {
            INSTANCE = this;
        }
    }

    /**
     * Used to retrieve a <em>Singleton</em> instance.
     * @return
     */
    public static Context getInstanceOf() {
        return INSTANCE.getApplicationContext();
    }
}