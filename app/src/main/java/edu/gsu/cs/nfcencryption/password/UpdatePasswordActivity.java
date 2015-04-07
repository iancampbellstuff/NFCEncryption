package edu.gsu.cs.nfcencryption.password;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;

import java.util.UUID;

import edu.gsu.cs.nfcencryption.R;
import edu.gsu.cs.nfcencryption.database.PasswordTable;
import edu.gsu.cs.nfcencryption.util.ErrorHandler;
import edu.gsu.cs.nfcencryption.util.SfxPlayer;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class UpdatePasswordActivity extends PasswordActivity
        implements PasswordActivity.NdefWriterListener {

    /**
     * Using the <code>char[]</code> value of a random <code>UUID</code> as a "<em>password</em>".
     * See https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html for further reference.
     *
     * Using a <code>char[]</code> array for storing a password is a <strong>Java</strong> security
     * best-practice, see <a href="http://stackoverflow.com/a/8889285/1298685">this stackoverflow
     * answer</a> for reference. Also note that the <code>char[]</code> array should be cleared
     * after immediate use, to prevent it from being viewed by a "<em>heap analyzer</em>".
     *
     * @return the char[] value of a random UUID as a "<em>password</em>".
     */
    private char[] getRandomPassword() {
        UUID uniqueID = UUID.randomUUID();
        return uniqueID.toString().toCharArray();
    }

    /**
     *
     * @param nfcTag
     * @throws Throwable
     */
    private void updatePassword(Tag nfcTag) throws Throwable {
        char[] password = getRandomPassword();
        SQLiteDatabase writableDB = this.db.getWritableDatabase();

        writableDB.beginTransaction();
        try {
            // first deleting the existing password (if there is one):
            writableDB.execSQL("DELETE FROM " + PasswordTable.NAME + ";");

            // saving the corresponding encryption to the database:
            EncryptedPassword encryptedPassword = new EncryptedPassword(password, EncryptionAlgorithm.PBKDF2WithHmacSHA1);
            writableDB.execSQL("INSERT INTO " + PasswordTable.NAME + " (" +
                            PasswordTable.Columns.SALT + ", " +
                            PasswordTable.Columns.HASH + ", " +
                            PasswordTable.Columns.ALGORITHM_TYPE +
                            ") VALUES (?, ?, ?);",
                    new Object[]{
                            encryptedPassword.getSalt(),
                            encryptedPassword.getHash(),
                            encryptedPassword.getAlgorithmType()
                    }
            );

            // writing to the NFC tag here:
            new NdefWriterTask(this, password).execute(nfcTag);

            writableDB.setTransactionSuccessful();

        } catch (Throwable e) {
            throw e;

        } finally {
            writableDB.endTransaction();
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
                        this.updatePassword(nfcTag);
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
        this.setFinishedLayout(true, R.string.password_update_successful);
    }

    /**
     *
     * @param e
     */
    @Override
    public void onWriteFail(Throwable e) {
        ErrorHandler.handle(e.getLocalizedMessage());
        SfxPlayer.getInstanceOf(this).playAlarmSound();
        this.setFinishedLayout(false, e.getLocalizedMessage());
    }
}
