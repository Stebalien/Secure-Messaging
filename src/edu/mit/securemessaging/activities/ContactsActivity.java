package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.ContactsListener;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import edu.mit.securemessaging.widgets.ContactAdapter;
import edu.mit.securemessaging.widgets.SimpleQueryAdapter;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsActivity extends Activity {
    private static Backend BACKEND = null;
    
    private static final int REQUEST_MODIFY_CONTACT = 1;

    // Use ints for speed as recommended by android. (Really? speed+java?)
    protected static final int DIALOG_ADD = 1;
    protected static final int DIALOG_DELETE = 3;
    protected static final int DIALOG_DELETE_CONFIRM = 4;
    protected static final int DIALOG_DELETE_CONFIRM_WIPE = 5;
    protected static final int DIALOG_ADD_REQUEST_ID = 6;
    
    // Add dialog indices
    protected static final int DIALOG_ADD_BARCODE = 0;
    protected static final int DIALOG_ADD_MANUAL = 1;
    
    /** Called when the activity is first created. */
    private ListView contactList;
    private Button btnAddContact;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BACKEND == null) BACKEND = Backend.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts);
        Backend backend = Backend.getInstance();
        contactList = (ListView)findViewById(R.id.contactList);
        contactList.setEmptyView(findViewById(R.id.contactListEmpty));
        try {
            contactList.setAdapter(
                    new ContactAdapter(this,
                            R.layout.contact,
                            BACKEND.getPersonDao().queryBuilder().orderBy("name", true).where().notIn(Person.TRUST_FIELD, TrustLevel.UNKNOWN, TrustLevel.ME).prepare(),
                            BACKEND.getHelper())
                    );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        contactList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	  Person person = ((Person)parent.getItemAtPosition(position));
            	  editContact(person.getID());
            }
        });
        
        
        // Show right button.
        btnAddContact = (Button)findViewById(R.id.btnHeaderRight);
        btnAddContact.setText(R.string.add_contact);
        btnAddContact.setVisibility(View.VISIBLE);
        btnAddContact.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_ADD);
            }
        });
        
        // Set title
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.contacts_title);
        
        backend.addContactsListener(new ContactsListener() {
            public void onContactsUpdated() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        @SuppressWarnings("unchecked")
                        SimpleQueryAdapter<Person> adapter = ((SimpleQueryAdapter<Person>)contactList.getAdapter());
                        adapter.update();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
    }
    
    @Override
    protected Dialog onCreateDialog(int id, final Bundle bundle) {
        final Dialog dialog;
        final String personId;
        final Activity self = this;
        switch(id) {
            case DIALOG_ADD:
                dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_add_contact_title)
                        .setItems(R.array.dialog_add_contact_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch(item) {
                                    case DIALOG_ADD_BARCODE:
                                        IntentIntegrator intent = new IntentIntegrator(self);
                                        intent.initiateScan();
                                        break;
                                    case DIALOG_ADD_MANUAL:
                                        showDialog(DIALOG_ADD_REQUEST_ID);
                                        break;
                                }
                            }
                        }).create();
                dialog.setCancelable(true);
                break;
            case DIALOG_ADD_REQUEST_ID:
                final EditText idField = new EditText(this);
                idField.setHint(R.string.dialog_add_request_id_hint);
                dialog = new AlertDialog.Builder(this)
                        .setView(idField)
                        .setTitle(R.string.add_contact_title)
                        .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                editContact(idField.getText().toString());
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
                break;
            case DIALOG_DELETE:
                personId = bundle.getString("id");
                try {
                    dialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_delete_contact_title)
                            .setMessage(getResources().getString(
                                    R.string.dialog_delete_contact_message,
                                    BACKEND.getPersonDao().queryForId(personId).getName()))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    showDialog(DIALOG_DELETE_CONFIRM, bundle);
                            }})
                            .setNegativeButton(android.R.string.no, null).create();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                dialog.setCancelable(true);
                break;
            case DIALOG_DELETE_CONFIRM:
                    Toast.makeText(getApplicationContext(), "Deleted", Toast.LENGTH_SHORT).show();
            default:
                dialog = null;
        }
        return dialog;
    }
    
    public void editContact(String contactID) {
        Intent intent = new Intent(this, EditContactActivity.class);
        intent.putExtra("id", contactID);
        startActivityForResult(intent, REQUEST_MODIFY_CONTACT); // Should be const later TODO
    }
    
    public void editContact(Person contact) {
        editContact(contact.getID());
    }
    
    public void onActivityResult(int request, int result, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(request, result, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {
                int split = contents.indexOf(':');
                try {
                    BACKEND.addOrUpdateContact(new Person(contents.substring(0, split), contents.substring(split + 1, contents.length()), TrustLevel.VERIFIED));
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return;
            }
        }
        if (request == REQUEST_MODIFY_CONTACT && result == Activity.RESULT_OK) {
            BACKEND.fireContactsUpdated();
        }
    }
}