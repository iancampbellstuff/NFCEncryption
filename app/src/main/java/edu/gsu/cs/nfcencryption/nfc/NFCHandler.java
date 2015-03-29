package edu.gsu.cs.nfcencryption.nfc;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Arrays;
import java.util.UUID;

import edu.gsu.cs.nfcencryption.database.LocalDatabase;
import edu.gsu.cs.nfcencryption.database.PasswordTable;
import edu.gsu.cs.nfcencryption.encryption.EncryptedPassword;
import edu.gsu.cs.nfcencryption.encryption.EncryptionAlgorithm;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class NFCHandler {
    
    /**
     * <code>private</code>, to prevent outside instantiation.
     */
    private NFCHandler() {
    }

    /**
     * Note that this method must be executed within a <em>transaction</em>, using the the passed
     * <code>LocalDatabase</code> parameter.
     *
     * @param password retrieved from <code>{@link NFCHandler#getRandomPassword()
     * -getRandomPassword():char[]}</code>.
     */
    private static void updateDatabase(SQLiteDatabase writableDB, char[] password) {
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
     * @param password retrieved from <code>{@link NFCHandler#getRandomPassword()
     * -getRandomPassword():char[]}</code>.
     */
    private static void updateDevice(char[] password) {
        if (password == null) {
            throw new IllegalArgumentException(
                    "The password passed to updateDevice() must not be null!"
            );
        }

        //TODO: this needs to be implemented
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
    private static char[] getRandomPassword() {
        UUID uniqueID = UUID.randomUUID();
        return uniqueID.toString().toCharArray();
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
    private static char[] getPasswordFromDevice() {
        //TODO: this needs to be implemented
        UUID uniqueID = UUID.randomUUID();
        return uniqueID.toString().toCharArray();
    }

    /**
     *
     * @throws Throwable
     */
    public static void removePassword() throws Throwable {
        SQLiteDatabase writableDB = LocalDatabase.getInstanceOf().getWritableDatabase();

        writableDB.beginTransaction();
        try {
            // removing the password from the NFC device:
            //TODO: this needs to be implemented

            // deleting the existing password from the database (if there is one):
            LocalDatabase.getInstanceOf().executeInTransaction("TRUNCATE TABLE " + PasswordTable.NAME + ";");

            writableDB.setTransactionSuccessful();

        } catch (Throwable e) {
            throw e;

        } finally {
            writableDB.endTransaction();
        }
    }

    /**
     *
     * @throws Throwable
     */
    public static void updatePassword() throws Throwable {
        char[] password = getRandomPassword();
        SQLiteDatabase writableDB = LocalDatabase.getInstanceOf().getWritableDatabase();

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
     *
     * @return
     * @throws Throwable
     */
    public static boolean passwordMatches() throws Throwable {
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
