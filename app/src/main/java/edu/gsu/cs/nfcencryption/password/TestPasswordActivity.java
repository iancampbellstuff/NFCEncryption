package edu.gsu.cs.nfcencryption.password;

import android.content.Intent;
import android.database.Cursor;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Arrays;

import edu.gsu.cs.nfcencryption.R;
import edu.gsu.cs.nfcencryption.database.PasswordTable;
import edu.gsu.cs.nfcencryption.util.ErrorHandler;
import edu.gsu.cs.nfcencryption.util.SfxPlayer;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class TestPasswordActivity extends PasswordActivity
        implements PasswordActivity.NdefReaderListener {

    /**
     *
     * @param nfcPassword
     * @return
     * @throws Throwable
     */
    private boolean passwordMatches(char[] nfcPassword) throws Throwable {
        if (nfcPassword == null) {
            throw new IllegalStateException(String.format("%s %s%n",
                    "The NFC device must have a stored password before calling passwordMatches()!",
                    "updatePassword() must be called first."
            ));
        }

        /*
         * getting the corresponding password info from the database (see
         * http://stackoverflow.com/a/5408253/1298685 for reference):
         */
        Cursor c = this.db.getReadableDatabase().rawQuery(
                "SELECT " + PasswordTable.Columns.SALT + ", " +
                        PasswordTable.Columns.HASH + ", " +
                        PasswordTable.Columns.ALGORITHM_TYPE +
                        " FROM " + PasswordTable.NAME +
                        " ORDER BY ROWID ASC LIMIT 1;", null
        );
        if (c.moveToFirst()) {
            byte[] salt = c.getBlob(0);
            byte[] hash = c.getBlob(1);
            String algorithmType = c.getString(2);
            c.close();

            // checking to see if the password matches the hash/salt:
            return new EncryptedPassword(salt, hash, algorithmType).matches(nfcPassword);

        } else {
            throw new IllegalStateException(String.format("%s %s%n",
                    "The NFC device has a stored password, but the local database is empty!",
                    "updatePassword() must be called before passwordMatches()."
            ));
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
            ((TextView)this.findViewById(R.id.tvStart)).setText(R.string.tap_nfc_device_to_test);
        }
    }

    /**
     * This method is called by the <strong>Android OS</strong> when an <strong>NFC</strong> device
     * is detected.
     *
     * Note that the detected <code>intent-filter</code> level will be passed as one level down in
     * priority to this method (for example, <code>NDEF_DISCOVERED</code> will be passed to this
     * method as <code>ACTION_TECH_DISCOVERED</code>).
     *
     * Also note that an empty <strong>NFC</strong> is detected, it will be detected as
     * <code>TECH_DISCOVERED</code> and will then be passed to this method as
     * <code>ACTION_TAG_DISCOVERED</code>.
     *
     * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
     * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
     *
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        String intentAction = intent.getAction();

        // tags with data will go here:
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intentAction)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(intentAction)) {
            String mimeType = intent.getType();

            if (MIME_TYPE.equals(mimeType)) {

                // reading from the NFC tag here:
                Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask(this).execute(nfcTag);

            } else {
                String errorMessage = this.getResources().getString(R.string.unsupported_mime_type, mimeType);
                ErrorHandler.handle(errorMessage);
                this.setFinishedLayout(false, errorMessage);
            }

            // tags without data will go here:
        } else if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intentAction)) {
            Tag nfcTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = nfcTag.getTechList();
            String ndefTech = Ndef.class.getName();

            for (String tech : techList) {
                if (ndefTech.equals(tech)) {

                    // reading from the NFC tag here:
                    new NdefReaderTask(this).execute(nfcTag);
                    break;
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
     * @param nfcPassword
     */
    @Override
    public void onReadSuccess(char[] nfcPassword) {
        SfxPlayer player = SfxPlayer.getInstanceOf(this);
        try {
            if (Arrays.equals(nfcPassword, PasswordActivity.EMPTY_PASSWORD)) {
                throw new NullPointerException(
                        this.getResources().getString(R.string.nfc_tag_empty)
                );
            } else if (this.passwordMatches(nfcPassword)) {
                player.playNotificationSound();
                this.setFinishedLayout(true, this.getResources().getString(R.string.password_matches, new String(nfcPassword)));

            } else {
                throw new IllegalStateException(
                        this.getResources().getString(R.string.password_does_not_match, new String(nfcPassword))
                );
            }
        } catch (Throwable e) {
            this.onReadFail(e);
        }
    }
}
