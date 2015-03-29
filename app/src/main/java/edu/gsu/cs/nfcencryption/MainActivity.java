package edu.gsu.cs.nfcencryption;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import edu.gsu.cs.nfcencryption.password.RemovePasswordActivity;
import edu.gsu.cs.nfcencryption.password.TestPasswordActivity;
import edu.gsu.cs.nfcencryption.password.UpdatePasswordActivity;

/**
 *
 */
public final class MainActivity extends Activity {

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // remove password button here:
        Button btnRemovePassword = (Button)this.findViewById(R.id.btnRemovePassword);
        btnRemovePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // switching activites here:
                Intent intent = new Intent(MainActivity.this, RemovePasswordActivity.class);
                startActivity(intent);
            }
        });

        // update password button here:
        Button btnUpdatePassword = (Button)this.findViewById(R.id.btnUpdatePassword);
        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // switching activites here:
                Intent intent = new Intent(MainActivity.this, UpdatePasswordActivity.class);
                startActivity(intent);
            }
        });

        // test password button here:
        Button btnTestPassword = (Button)this.findViewById(R.id.btnTestPassword);
        btnTestPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // switching activites here:
                Intent intent = new Intent(MainActivity.this, TestPasswordActivity.class);
                startActivity(intent);
            }
        });
    }
}
