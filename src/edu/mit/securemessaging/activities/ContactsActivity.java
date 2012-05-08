package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.ContactsListener;
import edu.mit.securemessaging.DatabaseHelper;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import edu.mit.securemessaging.widgets.ContactAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsActivity extends OrmLiteBaseActivity<DatabaseHelper> {
    protected static enum ContactDialog {
        ADD_CONTACT, DELETE_CONTACT, DELETE_CONTACT_CONFIRM, DELETE_CONTACT_CONFIRM_WIPE;
        public static ContactDialog valueOf(int ordinal) {
            return values()[ordinal];
        }
    }
    /** Called when the activity is first created. */
    private ListView contactList;
    private Button btnAddContact;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        Backend backend = Backend.getInstance();
        contactList = (ListView)findViewById(R.id.contactList);
        try {
            contactList.setAdapter(
                    new ContactAdapter(this,
                            R.layout.contact,
                            getHelper().getPersonDao().queryBuilder().orderBy("name", false).where().notIn(Person.TRUST_FIELD, TrustLevel.UNKNOWN, TrustLevel.ME).prepare(),
                            getHelper())
                    );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        contactList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editContact((Person)parent.getItemAtPosition(position));
            }
        });
        
        // Show right button.
        btnAddContact = (Button)findViewById(R.id.btnHeaderRight);
        btnAddContact.setText(R.string.add_contact);
        btnAddContact.setVisibility(View.VISIBLE);
        btnAddContact.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(ContactDialog.ADD_CONTACT.ordinal());
            }
        });
        
        // Set title
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.contacts_title);
        
        backend.addContactsListener(new ContactsListener() {
            public void onContactsUpdated() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        BaseAdapter adapter = ((BaseAdapter)contactList.getAdapter());
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
    
    protected Dialog onCreateDialog(int id) {
        Dialog dialog;
        switch(ContactDialog.valueOf(id)) {
            case ADD_CONTACT:
                return createAddDialog();
            default:
                dialog = null;
        }
        return dialog;
    }
    
    protected AlertDialog createAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final CharSequence[] items = {
                "Add By Barcode",
                "Add Manually",
        };
        builder.setTitle(R.string.add_contact_title);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch(item) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "You have scanned someone's barcode.", Toast.LENGTH_LONG).show();
                        break;
                    case 1:
                        addContact();
                        break;
                }
            }
        });
        return builder.create();
    }
    
    public void editContact(Person contact) {
        Intent intent = new Intent(this, EditContactActivity.class);
        intent.putExtra("id", contact.getID());
        startActivityForResult(intent, 0); // Should be const later TODO
    }
    
    public void addContact() {
        Intent intent = new Intent(this, EditContactActivity.class);
        startActivityForResult(intent, 0); // Should be const later TODO
    }
    
    public void onActivityResult(int request, int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }
        if (request == 0) {
            ((BaseAdapter)contactList.getAdapter()).notifyDataSetChanged();
        }
        
    }
}