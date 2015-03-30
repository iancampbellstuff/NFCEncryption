package edu.gsu.cs.nfcencryption.password;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Bundle;

import edu.gsu.cs.nfcencryption.R;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
abstract class PasswordActivity extends Activity
        implements NfcAdapter.CreateNdefMessageCallback, NfcAdapter.OnNdefPushCompleteCallback {

    /**
     *
     */
    protected boolean isFinished;

    /**
     *
     */
    protected boolean wasSuccessful;

    /**
     *
     */
    private static final String FINISHED_KEY = "finished key";

    /**
     *
     */
    private static final String SUCCESSFUL_KEY = "successful key";

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            this.isFinished = savedInstanceState.getBoolean(FINISHED_KEY);
            this.wasSuccessful = savedInstanceState.getBoolean(SUCCESSFUL_KEY);

            if (this.isFinished) {
                setContentView(this.wasSuccessful
                                ? R.layout.activity_password_success
                                : R.layout.activity_password_fail
                );
            } else {
                setContentView(R.layout.activity_password_start);
            }
        } else {
            setContentView(R.layout.activity_password_start);
        }
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(FINISHED_KEY, this.isFinished);
        savedInstanceState.putBoolean(SUCCESSFUL_KEY, this.wasSuccessful);
    }

    /**
     *
     * @param event
     * @return
     */
    @Override
    public abstract NdefMessage createNdefMessage(NfcEvent event);

    /**
     *
     * @param event
     */
    @Override
    public abstract void onNdefPushComplete(NfcEvent event);
}
