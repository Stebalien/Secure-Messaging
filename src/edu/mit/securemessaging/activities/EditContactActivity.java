package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.DatabaseHelper;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class EditContactActivity extends OrmLiteBaseActivity<DatabaseHelper> {
    /** Called when the activity is first created. */
    private Backend backend = Backend.getInstance();
    private EditText name;
    private TextView nameError;
    private CheckBox verified;
    private Person contact;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_contact_activity);
        
        name = (EditText)findViewById(R.id.txtName);
        verified = ((CheckBox)findViewById(R.id.chkVerified));
        nameError = (TextView)findViewById(R.id.lableNameError);
        
        String id = getIntent().getStringExtra("id");
        if (id == null) {
            ((TextView)findViewById(R.id.labelHeader)).setText(R.string.add_contact_title);
            contact = null;
        } else {
            ((TextView)findViewById(R.id.labelHeader)).setText(R.string.edit_contact_title);
            try {
                contact = backend.getPerson(id);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            name.setText(contact.getName());
            verified.setChecked(!contact.getTrustLevel().equals(TrustLevel.UNKNOWN));
        }
        
        ((Button)findViewById(R.id.btnSave)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (name.getText().toString().equals("")) {
                    nameError.setText(R.string.invalid_name);
                    nameError.setVisibility(View.VISIBLE);
                    return;
                } else {
                    nameError.setVisibility(View.GONE);
                }
                
                TrustLevel t = verified.isChecked() ? TrustLevel.VERIFIED : TrustLevel.KNOWN;
                String n = name.getText().toString();
                try {
                    if (contact == null) {
                        contact = new Person(n, t);
                    } else {
                        contact.setName(n);
                        contact.setTrustLevel(t);
                    }
                    backend.addOrUpdateContact(contact);
                    nameError.setVisibility(View.GONE);
                } catch (SQLException e) {
                    nameError.setText(R.string.conflict_name);
                    nameError.setVisibility(View.VISIBLE);
                    return;
                }
                
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
}