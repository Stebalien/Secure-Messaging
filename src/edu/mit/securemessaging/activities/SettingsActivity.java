package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
    /** Called when the activity is first created. */
    private EditText nameField;
    private EditText passwordField;
    private EditText passwordConfirmField;
    private TextView passwordError;
    private TextView nameError;
    private static Backend BACKEND;
        
    @Override
    public void onCreate(Bundle savedInstanceState) {
        BACKEND = Backend.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.settings_title);

        nameField = (EditText)findViewById(R.id.txtSetupName);
        passwordField = (EditText)findViewById(R.id.txtSetupPassword);
        passwordConfirmField = (EditText)findViewById(R.id.txtSetupPasswordConfirm);
        passwordError = (TextView)findViewById(R.id.labelPasswordError);
        nameError = (TextView)findViewById(R.id.labelNameError);
        
        ((Button)findViewById(R.id.btnReset)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                reset();
                Toast.makeText(getBaseContext(), "Changes Discarded", Toast.LENGTH_SHORT).show();
            }
        });

        ((Button)findViewById(R.id.btnSubmit)).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                boolean valid = true;
                String password = passwordField.getText().toString();
                String passwordConfirm = passwordConfirmField.getText().toString();
                String name = nameField.getText().toString();

                passwordError.setVisibility(View.GONE);
                nameError.setVisibility(View.GONE);

                if (name.isEmpty()) {
                    nameError.setText(R.string.invalid_name);
                    nameError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                if (!password.equals(passwordConfirm)) {
                    passwordError.setText(R.string.password_mismatch);
                    passwordError.setVisibility(View.VISIBLE);
                    valid = false;
                }

                if (!valid) {
                    return;
                }
                
                Person me = BACKEND.getMe();
                
                me.setName(name);
                try {
                    BACKEND.getPersonDao().update(me);
                } catch (SQLException e) {
                    me.refresh();
                    return;
                }
                Toast.makeText(getBaseContext(), "Profile Saved", Toast.LENGTH_SHORT).show();
            }
        });
        reset();
    }
    
    public void reset() {
        nameField.setText(BACKEND.getMe().getName());
        passwordError.setVisibility(View.GONE);
        nameError.setVisibility(View.GONE);
        passwordField.setText("");
    }
}