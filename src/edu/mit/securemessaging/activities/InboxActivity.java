package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.InboxListener;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.R.layout;
import edu.mit.securemessaging.widgets.ConversationAdapter;
import android.app.Activity;
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

public class InboxActivity extends Activity {
    /** Called when the activity is first created. */
    private final Backend backend = Backend.getInstance();
    private ListView conversationList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inbox);
        conversationList = (ListView)findViewById(R.id.conversationList);
        conversationList.setAdapter(new ConversationAdapter(this, R.layout.conversation, backend.getConversations()));
        
        conversationList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra("id", ((Conversation)parent.getItemAtPosition(position)).getID()); // XXX: Temp var
                startActivity(intent);
            }
        });
        
        
        Button btnNewConversation = ((Button)findViewById(R.id.btnHeaderRight));
        btnNewConversation.setText(R.string.new_conversation);
        btnNewConversation.setVisibility(View.VISIBLE);
        btnNewConversation.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra("id", backend.newConversation().getID());
                startActivity(intent);
            }
        });
        
        
        backend.addInboxListener(new InboxListener() {
            public void InboxUpdated() {
                runOnUiThread(new Runnable() {
                   public void run() {
                        ((BaseAdapter)conversationList.getAdapter()).notifyDataSetChanged();
                   }
                });
            }
        });
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.conversations_title);
    }
}