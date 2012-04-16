package edu.mit.securemessaging.activities;

import java.util.List;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Common;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.Conversation.ConversationListener;
import edu.mit.securemessaging.Message;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.widgets.ContactAdapter;
import edu.mit.securemessaging.widgets.MessageAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationActivity extends Activity {
    private final Backend backend = Backend.getInstance();
    private static enum Request {
        ADD_CONTACT;
        
        public static Request valueOf(int ordinal) {
            return values()[ordinal];
        }
    }
    
    /** Called when the activity is first created. */
    private ListView listMessages;
    private Conversation conversation;
    private TextView title;
    private ImageView icon;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);
        conversation = backend.getConversation(getIntent().getStringExtra("id"));
        listMessages = (ListView)findViewById(R.id.messageList);
        listMessages.setAdapter(new MessageAdapter(this, R.layout.message, conversation.getMessages()));
        listMessages.setItemsCanFocus(false);
        
        ((TextView)findViewById(R.id.labelHeader)).setText("Placeholder conversation text");
        // Show right button.
        Button btnAddUser = (Button)findViewById(R.id.btnHeaderRight);
        btnAddUser.setText(R.string.add_contact);
        btnAddUser.setVisibility(View.VISIBLE);
        btnAddUser.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ContactPickerActivity.class);
                startActivityForResult(intent, Request.ADD_CONTACT.ordinal());
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
                conversation.sendMessage(txtMessage.getText().toString());
                txtMessage.setText("");
            }
        });
        
        conversation.addConversationListener(new ConversationListener() {
            public void onConversationUpdated() {
                runOnUiThread(new Runnable() {
                   public void run() {
                        title.setText(Common.formatConversationTitle(conversation));
                        icon.setImageResource(Common.getConversationIcon(conversation));
                        BaseAdapter adapter = ((BaseAdapter)listMessages.getAdapter());
                        adapter.notifyDataSetChanged();
                        int position = adapter.getCount() - 1;
                        listMessages.smoothScrollToPosition(position > 0 ? position : 0);
                   }
                });
            }
        });
        
    }
    
    public void onActivityResult(int request, int result, Intent data) {
        if (result != Activity.RESULT_OK) {
            return;
        }
        switch (Request.valueOf(request)) {
            case ADD_CONTACT:
                conversation.addMember(backend.getContact(data.getStringExtra("id")));
                break;
        }
        
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
}