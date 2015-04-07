package edu.gsu.cs.nfcencryption.password;

import android.app.Activity;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;

import java.util.Arrays;

import edu.gsu.cs.nfcencryption.R;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
final class NdefWriterTask extends AsyncTask<Tag, Void, Void> {

    /**
     * This is used to listen for <code>NfcAsyncTask</code> to be done, and to then execute whatever
     * finishing logic.
     */
    private final PasswordActivity.NdefWriterListener delegate;

    /**
     * This is used to capture a possible exception within <code>NfcAsyncTask</code>, and to pass
     * such possible error to
     * <code>{@link edu.gsu.cs.nfcencryption.password.PasswordActivity.NdefReaderListener#onReadFail(Throwable)
     * +onReadFail(Throwable):void}</code>.
     */
    private Throwable throwable;

    /**
     *
     */
    private final char[] password;

    /**
     *
     * @param delegate
     * @param password
     */
    NdefWriterTask(PasswordActivity.NdefWriterListener delegate, char[] password) {
        this.delegate = delegate;
        this.password = password;
    }

    /**
     * See <a href="http://stackoverflow.com/a/6331373">http://stackoverflow.com/a/6331373</a> for reference.
     *
     * @return
     * @throws Throwable
     */
    private NdefRecord getNdefRecord() throws Throwable {
        String language = "en";
        byte[] passwordBytes = Arrays.toString(this.password).getBytes();
        byte[] languageBytes = language.getBytes("US-ASCII");
        int passwordLength = passwordBytes.length;
        int languageLength = languageBytes.length;
        byte[] payload = new byte[1 + passwordLength + languageLength];

        // setting a "status byte" here (see NDEF spec for actual bits)
        payload[0] = (byte) languageLength;

        // copying language and password bytes into payload here:
        System.arraycopy(languageBytes, 0, payload, 1, languageLength);
        System.arraycopy(passwordBytes, 0, payload, 1 + languageLength, passwordLength);

        return new NdefRecord(NdefRecord.TNF_WELL_KNOWN, NdefRecord.RTD_TEXT, new byte[0], payload);
    }

    /**
     *
     * @param params
     * @return
     */
    @Override
    protected Void doInBackground(Tag... params) {
        Ndef ndef = Ndef.get(params[0]);

        try {
            if (ndef == null) {
                throw new UnsupportedOperationException(
                        ((Activity)this.delegate).getResources().getString(R.string.ndef_not_supported)
                );

            } else if (!ndef.isWritable()) {
                throw new UnsupportedOperationException(
                        ((Activity)this.delegate).getResources().getString(R.string.nfc_tag_not_writable)
                );

            } else if (ndef.getMaxSize() < this.password.length) {
                throw new UnsupportedOperationException(
                        ((Activity)this.delegate).getResources()
                                .getString(R.string.nfc_tag_max_size_exceeded, ndef.getMaxSize())
                );
            }

            // writing the password to the NFC tag here:
            ndef.connect();
            NdefRecord[] ndefRecords = { getNdefRecord() };
            NdefMessage  ndefMessage = new NdefMessage(ndefRecords);
            ndef.writeNdefMessage(ndefMessage);

        } catch (Throwable e) {
            this.throwable = e;

        } finally {
            try {
                ndef.close();

            } catch (Throwable e) {
                this.throwable = e;
            }
        }

        return null;
    }

    /**
     *
     * @param nullParameter not used, but required by this implementation.
     */
    @Override
    protected void onPostExecute(Void nullParameter) {
        try {
            // if an error occurred in doInBackground:
            if (this.throwable != null) {
                throw this.throwable;

            } if (!this.isCancelled()) {

                // notifying the implementing activity here:
                this.delegate.onWriteSuccess();
            }
        } catch (Throwable e) {
            this.delegate.onWriteFail(e);
            this.cancel(true);
        }
    }
}
