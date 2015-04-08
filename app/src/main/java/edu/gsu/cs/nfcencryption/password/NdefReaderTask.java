package edu.gsu.cs.nfcencryption.password;

import android.app.Activity;
import android.app.ProgressDialog;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.WindowManager;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import edu.gsu.cs.nfcencryption.R;

/**
 * See <a href="http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278">
 * http://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278</a> for reference.
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
final class NdefReaderTask extends AsyncTask<Tag, Void, String> {

    /**
     * Used to display a spinning circle as the task executes.
     */
    private final ProgressDialog progressDialog;

    /**
     * This is used to listen for <code>NfcAsyncTask</code> to be done, and to then execute whatever
     * finishing logic.
     */
    private final PasswordActivity.NdefReaderListener delegate;

    /**
     * This is used to capture a possible exception within <code>NfcAsyncTask</code>, and to pass
     * such possible error to
     * <code>{@link edu.gsu.cs.nfcencryption.password.PasswordActivity.NdefReaderListener#onReadFail(Throwable)
     * +onReadFail(Throwable):void}</code>.
     */
    private Throwable throwable;

    /**
     *
     * @param delegate
     */
    NdefReaderTask(PasswordActivity.NdefReaderListener delegate) {
        this.delegate = delegate;
        this.progressDialog = new ProgressDialog((Activity)delegate);
    }

    /**
     * See this <a href="https://code.google.com/p/openmobster/source/browse/wiki/NFC.wiki?r=10240">
     * Google code example</a> for reference.
     *
     * Because the code in this method is very difficult and specific, it has been copied verbatim
     * from the above link (this is the only occurrence of such pure copy/pasting).
     *
     * @param record
     * @return
     * @throws UnsupportedEncodingException
     */
    private String getTextFrom(NdefRecord record) throws UnsupportedEncodingException {byte[] payload = record.getPayload();
        /*
        * payload[0] contains the "Status Byte Encodings" field, per the
        * NFC Forum "Text Record Type Definition" section 3.2.1.
        *
        * bit7 is the Text Encoding Field.
        *
        * if (Bit_7 == 0): The text is encoded in UTF-8 if (Bit_7 == 1):
        * The text is encoded in UTF16
        *
        * Bit_6 is reserved for future use and must be set to zero.
        *
        * Bits 5 to 0 are the length of the IANA language code.
        */

        //Get the Text Encoding
        String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";

        //Get the Language Code
        int languageCodeLength = payload[0] & 0077;
//        String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    /**
     * Starting a progressbar here, to make the calling <code>Activity</code> wait until
     * <code>doInBackground(Search...)</code> is done.
     */
    @Override
    protected void onPreExecute() {
        // in case this instance is re-used:
        this.throwable = null;

        // showing the spinning circle here:
        try {
            this.progressDialog.show();

            // show() must be called before setting the content view:
            this.progressDialog.setContentView(R.layout.progressdialog_spinning_circle);
            this.progressDialog.setCancelable(false);

        } catch (WindowManager.BadTokenException e) {
            this.throwable = e;
        }
    }

    /**
    *
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(Tag... params) {
        Ndef ndef = Ndef.get(params[0]);
        String nfcPassword = null;

        try {
            if (ndef == null) {
                throw new UnsupportedOperationException(
                        ((Activity)this.delegate).getResources().getString(R.string.ndef_not_supported)
                );
            }

            ndef.connect();
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            if (ndefMessage == null) {
                throw new NullPointerException(
                        ((Activity)this.delegate).getResources().getString(R.string.nfc_tag_empty)
                );
            }
            NdefRecord[] records = ndefMessage.getRecords();

            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN
                        && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    nfcPassword = getTextFrom(ndefRecord);
                }
            }
        } catch (Throwable e) {
            this.throwable = e;

        } finally {
            try {
                if (ndef != null && ndef.isConnected()) {
                    ndef.close();
                }
            } catch (Throwable e) {
                this.throwable = e;
            }
        }

        return nfcPassword;
    }

    /**
     *
     * @param result
     */
    @Override
    protected void onPostExecute(String result) {
        try {
            // if an error occurred in doInBackground:
            if (this.throwable != null) {
                throw this.throwable;

            } else if (result == null || TextUtils.isEmpty(result)) {
                throw new NullPointerException(
                        ((Activity)this.delegate).getResources().getString(R.string.nfc_tag_empty)
                );

            } else if (!this.isCancelled()) {
                char[] nfcPassword = result.toCharArray();

                if (Arrays.equals(nfcPassword, PasswordActivity.EMPTY_PASSWORD)) {
                    throw new NullPointerException(
                            ((Activity)this.delegate).getResources().getString(R.string.nfc_tag_empty)
                    );
                }

                // passing the search results to the Activity's implemented delegate here:
                this.delegate.onReadSuccess(nfcPassword);
            }
        } catch (Throwable e) {
            // ensuring that the progressbar stops before returning to the main UI:
            this.progressDialog.dismiss();

            this.delegate.onReadFail(e);
            this.cancel(true);
        } finally {
            if (this.progressDialog.isShowing()) {
                this.progressDialog.dismiss();
            }
        }
    }
}
