package edu.gsu.cs.nfcencryption.password;

import android.database.sqlite.SQLiteDatabase;
import android.nfc.NdefMessage;
import android.nfc.NfcEvent;
import android.os.Bundle;

import java.util.Arrays;
import java.util.UUID;

import edu.gsu.cs.nfcencryption.database.PasswordTable;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class UpdatePasswordActivity extends PasswordActivity {

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
     * @throws Throwable
     */
    private void updatePassword() throws Throwable {
        char[] password = getRandomPassword();
        SQLiteDatabase writableDB = this.db.getWritableDatabase();

        writableDB.beginTransaction();
        try {
            updateDatabase(writableDB, password);
            updateDevice(password);

            // clearing the password char array (a Java security best-practice):
            Arrays.fill(password, Character.MIN_VALUE);

            writableDB.setTransactionSuccessful();

        } catch (Throwable e) {
            throw e;

        } finally {
            writableDB.endTransaction();
        }
    }

    /**
     * Note that this method must be executed within a <em>transaction</em>, using the the passed
     * <code>LocalDatabase</code> parameter.
     *
     * @param password retrieved from
     * <code>{@link edu.gsu.cs.nfcencryption.password.UpdatePasswordActivity#getRandomPassword()
     * -getRandomPassword():char[]}</code>.
     */
    private void updateDatabase(SQLiteDatabase writableDB, char[] password) {
        if (password == null) {
            throw new IllegalArgumentException(
                    "The password passed to updateDatabase() must not be null!"
            );
        }

        // first deleting the existing password (if there is one):
        writableDB.execSQL("TRUNCATE TABLE " + PasswordTable.NAME + ";");

        // saving the corresponding encryption to the database:
        EncryptedPassword encryptedPassword = new EncryptedPassword(password, EncryptionAlgorithm.PBKDF2);
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
    }

    /**
     *
     * @param password retrieved from
     * <code>{@link edu.gsu.cs.nfcencryption.password.UpdatePasswordActivity#getRandomPassword()
     * -getRandomPassword():char[]}</code>.
     */
    private void updateDevice(char[] password) {
        if (password == null) {
            throw new IllegalArgumentException(
                    "The password passed to updateDevice() must not be null!"
            );
        }

        //TODO: this needs to be implemented
    }
}
