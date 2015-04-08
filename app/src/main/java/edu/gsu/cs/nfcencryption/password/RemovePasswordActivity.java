package edu.gsu.cs.nfcencryption.password;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.TextView;

import edu.gsu.cs.nfcencryption.R;
import edu.gsu.cs.nfcencryption.database.PasswordTable;
import edu.gsu.cs.nfcencryption.util.ErrorHandler;
import edu.gsu.cs.nfcencryption.util.SfxPlayer;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class RemovePasswordActivity extends PasswordActivity
        implements PasswordActivity.NdefWriterListener {

    /**
     *
     * @param nfcTag
     * @throws Throwable
     */
    private void removePassword(Tag nfcTag) throws Throwable {
        SQLiteDatabase writableDB = this.db.getWritableDatabase();

        writableDB.beginTransaction();
        try {
            // deleting the existing password from the database (if there is one):
            writableDB.execSQL("DELETE FROM " + PasswordTable.NAME + ";");

            // writing an empty UUID to the NFC tag here, to "remove" the password:
            new NdefWriterTask(this, EMPTY_PASSWORD).execute(nfcTag);

            writableDB.setTransactionSuccessful();

        } catch (Throwable e) {
            throw e;

        } finally {
            writableDB.endTransaction();
        }
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!this.isFinished) {
            // setting the "tap nfc device" text here:
            ((TextView)this.findViewById(R.id.tvStart)).setText(R.string.tap_nfc_device_to_remove);
        }
    }

    /**
     * This method is called by the <strong>Android OS</strong> when an <strong>NFC</strong> device
     * is detected.
     *
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        String intentAction = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentAction)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentAction)) {

            Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = nfcTag.getTechList();
            String ndefTech = Ndef.class.getName();

            for (String tech : techList) {
                if (ndefTech.equals(tech)) {

                    try {
                        this.removePassword(nfcTag);
                        break;

                    } catch (Throwable e) {
                        ErrorHandler.handle(e.getLocalizedMessage());
                        this.setFinishedLayout(false, e.getLocalizedMessage());
                        break;
                    }
                }
            }
        } else {
            String errorMessage = this.getResources().getString(R.string.unsupported_intent_action, intentAction);
            ErrorHandler.handle(errorMessage);
            this.setFinishedLayout(false, errorMessage);
        }
    }

    /**
     *
     */
    @Override
    public void onWriteSuccess() {
        SfxPlayer.getInstanceOf(this).playNotificationSound();
        this.setFinishedLayout(true, R.string.password_removal_successful);
    }
}
