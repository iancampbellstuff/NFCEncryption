package edu.gsu.cs.nfcencryption.password;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.TextView;

import edu.gsu.cs.nfcencryption.R;
import edu.gsu.cs.nfcencryption.database.LocalDatabase;
import edu.gsu.cs.nfcencryption.util.ErrorHandler;

/**
 * See <a href="https://github.com/android/platform_development/blob/master/samples/AndroidBeamDemo/src/com/example/android/beam/Beam.java">
 * this Android example</a>, <a href="https://developer.android.com/guide/topics/connectivity/nfc/nfc.html">
 * https://developer.android.com/guide/topics/connectivity/nfc/nfc.html</a>,
 * <a href="https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html#foreground-dispatch">
 * https://developer.android.com/guide/topics/connectivity/nfc/advanced-nfc.html#foreground-dispatch</a>, and
 * <a href="http://www.developer.com/ws/android/nfc-programming-in-android.html">
 * http://www.developer.com/ws/android/nfc-programming-in-android.html</a> for reference.
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
abstract class PasswordActivity extends Activity {

    /**
     *
     */
    protected NfcAdapter nfcAdapter;

    /**
     *
     */
    protected LocalDatabase db;

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
    protected String resultsText;

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
     */
    private static final String RESULTS_TEXT_KEY = "results text key";

    /**
     *
     */
    protected static final String MIME_TYPE = "text/plain";

    /**
     *
     */
    protected static final char[] EMPTY_PASSWORD = "00000000-0000-0000-0000-000000000000".toCharArray();

    /**
     * Used to set the <code>Layout</code>, the displayed <code>TextView</code>, and the finished
     * state of corresponding instance variables. Note that this method must <strong>only</strong>
     * be called when the <code>activity_password_start</code> <code>Activity</code> is finished
     * interacting with the <em>NFC</em> device.
     *
     * @param wasSuccessful
     * @param resultsTextID
     */
    protected void setFinishedLayout(boolean wasSuccessful, int resultsTextID) {
        this.resultsText = this.getResources().getString(resultsTextID);
        this.setFinishedLayout(wasSuccessful, this.resultsText);
    }

    /**
     * Used to set the <code>Layout</code>, the displayed <code>TextView</code>, and the finished
     * state of corresponding instance variables. Note that this method must <strong>only</strong>
     * be called when the <code>activity_password_start</code> <code>Activity</code> is finished
     * interacting with the <em>NFC</em> device.
     *
     * @param wasSuccessful
     * @param resultText
     */
    protected void setFinishedLayout(boolean wasSuccessful, String resultText) {
        this.isFinished = true;
        this.wasSuccessful = wasSuccessful;
        this.resultsText = resultText;

        // setting the success/fail layout here:
        this.setContentView(wasSuccessful
                        ? R.layout.activity_password_success
                        : R.layout.activity_password_fail
        );

        ((TextView) this.findViewById(wasSuccessful
                ? R.id.tvSuccess // for the "activity_password_success" layout:
                : R.id.tvFail // for the "activity_password_fail" layout:
        )).setText(resultText);
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // getting saved resources here (originally saved in onSaveInstanceState(Bundle)):
        if (savedInstanceState != null) {
            this.isFinished = savedInstanceState.getBoolean(FINISHED_KEY);
            this.wasSuccessful = savedInstanceState.getBoolean(SUCCESSFUL_KEY);
            this.resultsText = savedInstanceState.getString(RESULTS_TEXT_KEY);
        }

        if (this.isFinished) {
            this.setFinishedLayout(this.wasSuccessful, this.resultsText);
            return;
        }

        // checking that the device supports NFC (should be true for API 10+):
        this.nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (this.nfcAdapter == null) {
            ErrorHandler.handle(this.getResources().getString(R.string.nfc_not_available));
            this.setFinishedLayout(false, R.string.nfc_not_available);
            return;

            // checking that NFC is enabled for the device:
        } else if (!this.nfcAdapter.isEnabled()) {
            ErrorHandler.handle(this.getResources().getString(R.string.nfc_not_enabled));
            this.setFinishedLayout(false, R.string.nfc_not_enabled);
            return;
        }

        // setting the initial layout here:
        this.setContentView(R.layout.activity_password_start);

        this.db = LocalDatabase.getInstanceOf(this);
    }

    /**
     * Because when the device may change orientation this <code>Activity</code> will be destroyed
     * and re-created, this method is used to preserve variable data that is critical to the current
     * state of the user's interaction.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        savedInstanceState.putBoolean(FINISHED_KEY, this.isFinished);
        savedInstanceState.putBoolean(SUCCESSFUL_KEY, this.wasSuccessful);
        savedInstanceState.putString(RESULTS_TEXT_KEY, this.resultsText);
    }

    /**
     * This method is called by the <strong>Android OS</strong> when an <strong>NFC</strong> device
     * is detected, and as such is a required implementation of all subclasses.
     *
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     *
     * @param intent
     */
    @Override
    protected abstract void onNewIntent(Intent intent);

    /**
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     */
    @Override
    protected void onPause() {
        if (!this.isFinished) {
            this.disableForegroundDispatch();
        }
        super.onPause();
    }

    /**
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!this.isFinished) {
            this.enableForegroundDispatch();
        }
    }

    /**
     * This routes the code to <code>{@link PasswordActivity#onNewIntent(android.content.Intent)
     * ~onNewIntent(Intent}:void</code> after detecting an NFC device.
     *
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     */
    private void enableForegroundDispatch() {
        // building a PendingIntent here:
        Intent intent = new Intent(this.getApplicationContext(), this.getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // preventing multiple instances of this activity from being created
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        // building an IntentFilter array here:
        IntentFilter[] intentFilters = new IntentFilter[2];
        intentFilters[0] = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            intentFilters[0].addDataType(MIME_TYPE);
            intentFilters[0].addCategory(Intent.CATEGORY_DEFAULT);

        } catch(IntentFilter.MalformedMimeTypeException e) {
            ErrorHandler.handle(e);
            this.setFinishedLayout(false, R.string.unsupported_mime_type);
            return;
        }
        intentFilters[1] = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFilters[1].addCategory(Intent.CATEGORY_DEFAULT);

        // building a tech-list array here:
        String[][] techLists = new String[][] {
                new String[] { MifareUltralight.class.getName(), Ndef.class.getName(), NfcA.class.getName() },
                new String[] { MifareClassic.class.getName(), Ndef.class.getName(), NfcA.class.getName() }
        };

        // enabling NFC "foreground dispatch" here:
        this.nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilters, techLists);
    }

    /**
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     */
    private void disableForegroundDispatch() {
        this.nfcAdapter.disableForegroundDispatch(this);
    }

    /**
     * This is used to implement the <em>Delegate</em> design-pattern.
     *
     * @author Ian A. Campbell
     * @author Andrew J. Rutherford
     */
    static interface NdefReaderListener {

        /**
         *
         * @param nfcPassword
         */
        public abstract void onReadSuccess(char[] nfcPassword);

        /**
         * This is used to allow for error-handling for <code>NdefReaderTask</code>
         * from the implementing <code>Activity</code>.
         *
         * @param e
         */
        public abstract void onReadFail(Throwable e);
    }

    /**
     * This is used to implement the <em>Delegate</em> design-pattern.
     *
     * @author Ian A. Campbell
     * @author Andrew J. Rutherford
     */
    interface NdefWriterListener {

        /**
         *
         */
        public abstract void onWriteSuccess();

        /**
         * This is used to allow for error-handling for <code>NdefWriterTask</code>
         * from the implementing <code>Activity</code>.
         *
         * @param e
         */
        public abstract void onWriteFail(Throwable e);
    }
}
