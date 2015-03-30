package edu.gsu.cs.nfcencryption.util;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import edu.gsu.cs.nfcencryption.R;

/**
 * Contains convenience methods for error-handling, to be used throughout the application.
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class ErrorHandler {

    /**
     *
     */
    private static final String ERROR_TAG = "a handled error";

    /**
     * Empty implementation, <code>private</code> to prevent outside instantiation.
     */
    private ErrorHandler() {
    }

    /**
     *
     * @param errorMessage
     */
    public static void handle(String errorMessage) {
        Log.wtf(ERROR_TAG, errorMessage);
    }

    /**
     *
     * @param e
     */
    public static void handle(Throwable e) {
        Log.wtf(ERROR_TAG, e);
    }

    /**
     *
     * @param e
     * @param activity
     */
    public static void showErrorMessage(Throwable e, Activity activity) {
        handle(e);
        Toast.makeText(
                activity,
                R.string.error_occurred,
                Toast.LENGTH_SHORT
        ).show();
    }
}