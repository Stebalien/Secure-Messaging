package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.ConversationListener;
import edu.mit.securemessaging.Common;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.Message;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import edu.mit.securemessaging.widgets.MessageAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ConversationActivity extends Activity {
    private static Backend BACKEND = null;
    private static final int ADD_CONTACT = 1;
    
    // Use ints for speed as recommended by android. (Really? speed+java?)
    protected static final int DIALOG_ADD = 1;
    protected static final int DIALOG_MODIFY = 2;
    
    // Modify dialog indices
    protected static final int DIALOG_MODIFY_ADD = 0;
    protected static final int DIALOG_MODIFY_REMOVE = 1;
   
    /** Called when the activity is first created. */
    private ListView listMessages;
    private Conversation conversation;
    private TextView title;
    private ImageView icon;
    private MessageAdapter adapter;
    
    private final ConversationListener cListener = new ConversationListener() {
        public void onConversationUpdated(String id) {
            if (!id.equals(conversation.getID())) {
                return;
            }
            runOnUiThread(new Runnable() {
                public void run() {
                    title.setText(Common.formatConversationTitle(conversation));
                    icon.setImageResource(Common.getConversationIcon(conversation));
                    adapter.update();
                    adapter.notifyDataSetChanged();
                    int position = adapter.getCount() - 1;
                    listMessages.smoothScrollToPosition(position > 0 ? position : 0);
                }
            });
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (BACKEND == null) BACKEND = Backend.getInstance();
        
        setContentView(R.layout.conversation_activity);
        conversation = BACKEND.getConversation(getIntent().getStringExtra("id"));
        listMessages = (ListView)findViewById(R.id.messageList);
        listMessages.setEmptyView(findViewById(R.id.conversationEmpty));
        try {
            adapter = new MessageAdapter(this,
                    R.layout.message,
                    BACKEND.getMessageDao().queryBuilder().orderBy(Message.TIMESTAMP_FIELD, true)
                    .where().eq(Message.CONVERSATION_FIELD, conversation).prepare(),
                    BACKEND.getHelper());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        listMessages.setAdapter(adapter);
        listMessages.setItemsCanFocus(false);
        int position = adapter.getCount() - 1;
        listMessages.setSelection(position > 0 ? position : 0);
        
      listMessages.setOnItemLongClickListener(new OnItemLongClickListener() {
      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
          final Bundle args = new Bundle();
          args.putString("id", ((Message)parent.getItemAtPosition(position)).getID());
          showDialog(DIALOG_MODIFY,  args);
          return true;
      }
      
  });
        
        
        ((TextView)findViewById(R.id.labelHeader)).setText("Placeholder conversation text");
        // Show right button.
        Button btnAddUser = (Button)findViewById(R.id.btnHeaderRight);
        btnAddUser.setText(R.string.add_contact);
        btnAddUser.setVisibility(View.VISIBLE);
        btnAddUser.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ContactPickerActivity.class);
                intent.putExtra("conversation_id", conversation.getID());
                startActivityForResult(intent, ADD_CONTACT);
            }
        });
        
        // Set title
        title = (TextView)findViewById(R.id.labelHeader);
        icon = (ImageView)findViewById(R.id.imgHeaderIcon);
        
        title.setText(Common.formatConversationTitle(conversation));
        icon.setVisibility(View.VISIBLE);
        icon.setImageResource(Common.getConversationIcon(conversation));
        
        final EditText txtMessage = (EditText) findViewById(R.id.txtMessage);
        
        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    conversation.sendMessage(txtMessage.getText().toString());
                } catch (SQLException e) {
                    new RuntimeException(e);
                }
                txtMessage.setText("");
            }
        });
        
        BACKEND.addConversationListener(cListener);
        
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        BACKEND.removeConversationListener(cListener);
    }
    
    public void onActivityResult(int request, int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }
        switch (request) {
            case ADD_CONTACT:
                try {
                    conversation.inviteMember(BACKEND.getPerson(data.getStringExtra("id")));
                } catch (SQLException e) {
                    new RuntimeException(e);
                }
                break;
        }
        
    }
    protected Dialog onCreateDialog(int id, final Bundle bundle) {
        final Dialog dialog;
        final String messageId;
        switch(id) {
            case DIALOG_MODIFY:
            	messageId = bundle.getString("id");
                    dialog = new AlertDialog.Builder(this)
                            .setTitle(R.string.dialog_edit_conversation)
                            .setItems( R.array.dialog_modify_conversation_menu, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int item) {
                                	Person person;
                                	try {
                                		 person = BACKEND.getMessageDao().queryForId(messageId).getSender();
                                	switch(item) {
                                    case DIALOG_MODIFY_ADD:
                                    	editContact(person.getID());
                                        break;
                                    case DIALOG_MODIFY_REMOVE:
                                    	conversation.deleteMessagesFrom(person);

                                        break;
                                }
									} catch (SQLException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
                                }
                            }).create();
                
                dialog.setCancelable(true);
                break;
            default:
                dialog = null;
        }
        return dialog;
    }
    @Override
    public void onBackPressed() {
        finish();
    }
    
    public void editContact(String contactID) {
        Intent intent = new Intent(this, EditContactActivity.class);
        intent.putExtra("id", contactID);
        startActivityForResult(intent, ADD_CONTACT); // Should be const later TODO
        
    }
    

}