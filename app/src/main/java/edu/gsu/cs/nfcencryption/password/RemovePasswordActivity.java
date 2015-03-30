package edu.gsu.cs.nfcencryption.password;

import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NfcEvent;
import android.os.Bundle;

import edu.gsu.cs.nfcencryption.R;
import edu.gsu.cs.nfcencryption.database.PasswordTable;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class RemovePasswordActivity extends PasswordActivity {

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (this.isFinished) {
            if (this.wasSuccessful) {
                

            } else {

            }
        }
    }

    /**
     *
     * @param event
     * @return
     */
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        return null;
    }

    /**
     *
     * @param event
     */
    @Override
    public void onNdefPushComplete(NfcEvent event) {

    }

    /**
     *
     * @throws Throwable
     */
    private void removePassword() throws Throwable {
        SQLiteDatabase writableDB = this.db.getWritableDatabase();

        writableDB.beginTransaction();
        try {
            // removing the password from the NFC device:
            //TODO: this needs to be implemented

            // deleting the existing password from the database (if there is one):
            writableDB.execSQL("TRUNCATE TABLE " + PasswordTable.NAME + ";");

            writableDB.setTransactionSuccessful();

        } catch (Throwable e) {
            throw e;

        } finally {
            writableDB.endTransaction();
        }
    }
}
