package edu.gsu.cs.nfcencryption.password;

import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NfcEvent;
import android.os.Bundle;

import java.util.Arrays;
import java.util.UUID;

import edu.gsu.cs.nfcencryption.R;
import edu.gsu.cs.nfcencryption.database.LocalDatabase;
import edu.gsu.cs.nfcencryption.database.PasswordTable;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class TestPasswordActivity extends PasswordActivity {

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
     * Using a <code>char[]</code> array for storing a password is a <strong>Java</strong> security
     * best-practice, see <a href="http://stackoverflow.com/a/8889285/1298685">this stackoverflow
     * answer</a> for reference. Also note that the <code>char[]</code> array should be cleared
     * after immediate use, to prevent it from being viewed by a "<em>heap analyzer</em>".
     *
     * @return
     */
    private char[] getPasswordFromDevice() {
        //TODO: this needs to be implemented
        UUID uniqueID = UUID.randomUUID();
        return uniqueID.toString().toCharArray();
    }

    /**
     *
     * @return
     * @throws Throwable
     */
    private boolean passwordMatches() throws Throwable {
        // getting the stored password from the NFC device:
        char[] password = getPasswordFromDevice();
        if (password == null) {
            throw new IllegalStateException(String.format("%s %s%n",
                    "The NFC device must have a stored password before calling passwordMatches()!",
                    "updatePassword() must be called first."
            ));
        }

        boolean result = false;

        /*
         * getting the corresponding password info from the database (see
         * http://stackoverflow.com/a/5408253/1298685 for reference):
         */
        Cursor c = LocalDatabase.getInstanceOf().getReadableDatabase().rawQuery(
                "SELECT " + PasswordTable.Columns.SALT + ", " +
                        PasswordTable.Columns.HASH + ", " +
                        PasswordTable.Columns.ALGORITHM_TYPE +
                        " FROM " + PasswordTable.NAME +
                        " ORDER BY ROWID ASC LIMIT 1;", null
        );
        if (c.moveToNext()) {
            c.moveToNext();
            byte[] salt = c.getBlob(1);
            byte[] hash = c.getBlob(2);
            String algorithmType = c.getString(3);
            c.close();

            // checking to see if the password matches the hash/salt:
            result = new EncryptedPassword(salt, hash, algorithmType).matches(password);

        } else {
            throw new IllegalStateException(String.format("%s %s%n",
                    "The NFC device has a stored password, but the local database is empty!",
                    "updatePassword() must be called before passwordMatches()."
            ));
        }

        // clearing the password char array (a Java security best-practice):
        Arrays.fill(password, Character.MIN_VALUE);

        return result;
    }
}
