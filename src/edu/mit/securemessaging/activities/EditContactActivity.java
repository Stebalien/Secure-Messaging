package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Key;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import edu.mit.securemessaging.widgets.ContactAdapter;
import android.R.color;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class EditContactActivity extends Activity {
    /** Called when the activity is first created. */
    private Backend backend = Backend.getInstance();
    private TextView name;
    private TextView username;
    private CheckBox verified;
    private Person contact;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_contact_activity);
        
        name = ((TextView)findViewById(R.id.txtName));
        username = ((TextView)findViewById(R.id.txtUsername));
        verified = ((CheckBox)findViewById(R.id.chkVerified));
        
        String id = getIntent().getStringExtra("id");
        if (id == null) {
            ((TextView)findViewById(R.id.labelHeader)).setText(R.string.add_contact_title);
            contact = null;
        } else {
            ((TextView)findViewById(R.id.labelHeader)).setText(R.string.edit_contact_title);
            contact = backend.getContact(id);
            name.setText(contact.getName());
            username.setText(contact.getUsername());
            verified.setChecked(!contact.getTrustLevel().equals(TrustLevel.UNKNOWN));
        }
        
        ((Button)findViewById(R.id.btnSave)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (name.getText().toString().equals("")) {
                    name.setBackgroundColor(R.color.unverified_color);
                    return;
                } else {
                    name.setBackgroundColor(color.background_light);
                }
                if (username.getText().toString().equals("")) {
                    username.setBackgroundColor(R.color.unverified_color);
                    return;
                } else {
                    username.setBackgroundColor(color.background_light);
                }
                if (contact == null) {
                    contact = new Person(
                            name.getText().toString(),
                            username.getText().toString(),
                            new Key()
                            );
                    backend.addContact(contact);
                } else {
                    contact.setName(name.getText().toString());
                    contact.setUsername(username.getText().toString());
                }
                contact.setTrustLevel(verified.isChecked() ? TrustLevel.VERIFIED : TrustLevel.KNOWN);
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