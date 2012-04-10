package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.widgets.ContactAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContactsActivity extends Activity {
    /** Called when the activity is first created. */
    private ListView contactList;
    private Button btnAddContact;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        Backend backend = Backend.getInstance();
        contactList = (ListView)findViewById(R.id.contactList);
        contactList.setAdapter(new ContactAdapter(this, R.layout.contact, backend.getContacts()));
        
        // Show right button.
        btnAddContact = (Button)findViewById(R.id.btnHeaderRight);
        btnAddContact.setText(R.string.add_contact);
        btnAddContact.setVisibility(View.VISIBLE);
        btnAddContact.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
               //pass 
            }
        });
        
        // Set title
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.contacts_title);
        
    }
}