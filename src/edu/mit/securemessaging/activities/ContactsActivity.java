package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.ContactsListener;
import edu.mit.securemessaging.Conversation;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactsActivity extends Activity {
    private static Backend BACKEND = null;
    
    // Use ints for speed as recommended by android. (Really? speed+java?)
    protected static final int DIALOG_ADD = 1;
    protected static final int DIALOG_MODIFY = 2;
    protected static final int DIALOG_DELETE = 3;
    protected static final int DIALOG_DELETE_CONFIRM = 4;
    protected static final int DIALOG_DELETE_CONFIRM_WIPE = 5;
    protected static final int DIALOG_ADD_REQUEST_ID = 6;
    
    // Add dialog indices
    protected static final int DIALOG_ADD_BARCODE = 0;
    protected static final int DIALOG_ADD_MANUAL = 1;
    
    // Modify dialog indices
    protected static final int DIALOG_MODIFY_EDIT = 0;
    protected static final int DIALOG_MODIFY_DELETE = 1;
    
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
                            BACKEND.getPersonDao().queryBuilder().orderBy("name", false).where().notIn(Person.TRUST_FIELD, TrustLevel.UNKNOWN, TrustLevel.ME).prepare(),
                            BACKEND.getHelper())
                    );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        contactList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: Does this make sense? We can also filter or find last updated.
                Person person = ((Person)parent.getItemAtPosition(position));
                Conversation conversation = person.getLastConversation();
                if (conversation == null) {
                    conversation = BACKEND.newConversation();
                    conversation.inviteMember(person);
                }
                
                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra("id", conversation.getID());
                startActivity(intent);
            }
        });
        
        contactList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final Bundle args = new Bundle();
                args.putString("id", ((Person)parent.getItemAtPosition(position)).getID());
                showDialog(DIALOG_MODIFY,  args);
                return true;
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
        switch(id) {
            case DIALOG_ADD:
                dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_add_contact_title)
                        .setItems(R.array.dialog_add_contact_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch(item) {
                                    case DIALOG_ADD_BARCODE:
                                        Toast.makeText(getApplicationContext(), "You have scanned someone's barcode.", Toast.LENGTH_LONG).show();
                                        break;
                                    case DIALOG_ADD_MANUAL:
                                        showDialog(DIALOG_ADD_REQUEST_ID);
                                        break;
                                }
                            }
                        }).create();
                dialog.setCancelable(true);
                break;
            case DIALOG_MODIFY:
                personId = bundle.getString("id");
                try {
                    dialog = new AlertDialog.Builder(this)
                            .setTitle(getResources().getString(
                                    R.string.dialog_modify_contact_title,
                                    BACKEND.getPersonDao().queryForId(personId).getName()))
                                    .setItems( R.array.dialog_modify_contact_menu, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                    switch(item) {
                                        case DIALOG_MODIFY_EDIT:
                                            editContact(personId);
                                            break;
                                        case DIALOG_MODIFY_DELETE:
                                            showDialog(DIALOG_DELETE, bundle);
                                            break;
                                    }
                                }
                            }).create();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
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
        startActivityForResult(intent, 0); // Should be const later TODO
    }
    
    public void editContact(Person contact) {
        editContact(contact.getID());
    }
    
    public void onActivityResult(int request, int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }
        if (request == 0) {
            BACKEND.fireContactsUpdated();
        }
        
    }
}