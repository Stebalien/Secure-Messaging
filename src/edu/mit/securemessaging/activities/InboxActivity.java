package edu.mit.securemessaging.activities;

import java.sql.SQLException;


import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.InboxListener;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.widgets.ConversationAdapter;
import edu.mit.securemessaging.widgets.SimpleQueryAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class InboxActivity extends Activity {
    /** Called when the activity is first created. */
    private static Backend BACKEND = null;
    private ListView conversationList;
    private SimpleQueryAdapter<Conversation> adapter;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BACKEND == null) BACKEND = Backend.getInstance();
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.inbox);
        conversationList = (ListView)findViewById(R.id.conversationList);
        conversationList.setEmptyView(findViewById(R.id.inboxEmpty));
        try {
            adapter = new ConversationAdapter(this,
                    R.layout.conversation,
                    BACKEND.getConversationDao().queryBuilder().orderBy("timestamp", false).prepare(),
                    BACKEND.getHelper());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        conversationList.setAdapter(adapter);

        conversationList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra("id", ((Conversation)parent.getItemAtPosition(position)).getID());
                startActivity(intent);
            }
        });
        
        
        Button btnNewConversation = ((Button)findViewById(R.id.btnHeaderRight));
        btnNewConversation.setText(R.string.new_conversation);
        btnNewConversation.setVisibility(View.VISIBLE);
        btnNewConversation.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ConversationActivity.class);
                intent.putExtra("id", BACKEND.newConversation().getID());
                startActivity(intent);
            }
        });
        
        
        BACKEND.addInboxListener(new InboxListener() {
            public void InboxUpdated() {
                runOnUiThread(new Runnable() {
                   public void run() {
                       adapter.update();
                       adapter.notifyDataSetChanged();
                   }
                });
            }
        });
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.conversations_title);
    }
}