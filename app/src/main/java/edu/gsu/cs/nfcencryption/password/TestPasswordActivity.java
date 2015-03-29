package edu.gsu.cs.nfcencryption.password;

import android.app.Activity;
import android.os.Bundle;

import edu.gsu.cs.nfcencryption.R;

/**
 *
 * @author Ian A. Campbell
 * @author Andrew J. Rutherford
 */
public final class TestPasswordActivity extends Activity {

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_start);
    }
}
