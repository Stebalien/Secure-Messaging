package edu.mit.securemessaging.activities;

import java.io.IOException;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class StartActivity extends Activity {
    /** Called when the activity is first created. */
    private TextView title;
    private Button button;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Backend BACKEND = Backend.getInstance();
        Person me = BACKEND.getMe();
        
        if (me == null) {
            // Set title
            setContentView(R.layout.register);
            title = ((TextView)findViewById(R.id.labelHeader));
            button = ((Button)findViewById(R.id.btnSubmit));
            
            title.setText(R.string.setup_title);
            
            final EditText nameField = (EditText)findViewById(R.id.txtSetupName);
            final EditText passwordField = (EditText)findViewById(R.id.txtSetupPassword);
            final EditText passwordConfirmField = (EditText)findViewById(R.id.txtSetupPasswordConfirm);
            final TextView passwordError = (TextView)findViewById(R.id.labelPasswordError);
            final TextView nameError = (TextView)findViewById(R.id.labelNameError);
            
            
            button.setOnClickListener(new OnClickListener() {
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
                    
                    if (password.isEmpty()) {
                        passwordError.setText(R.string.password_required_length);
                        passwordError.setVisibility(View.VISIBLE);
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
                    
                    try {
                        if (!BACKEND.register(name, password)) {
                            // TODO: Should set error text
                            return;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    finish();
                }
            });
        } else {
            setContentView(R.layout.login);
            title = ((TextView)findViewById(R.id.labelHeader));
            button = ((Button)findViewById(R.id.btnSubmit));
            title.setText(R.string.login_title);
            
            final EditText passwordField = (EditText)findViewById(R.id.txtLoginPassword);
            
            button.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    String password = passwordField.getText().toString();
                    
                    if (!BACKEND.checkPassword(password)) {
                        Toast.makeText(getBaseContext(), R.string.incorrect_password, Toast.LENGTH_SHORT).show();
                        passwordField.setText("");
                        passwordField.startAnimation(AnimationUtils.loadAnimation(getBaseContext(), R.anim.shake));
                        return;
                    }
                    
                    startActivity(new Intent(getBaseContext(), MainActivity.class));
                    finish();
                }
            });
        }
    }
}