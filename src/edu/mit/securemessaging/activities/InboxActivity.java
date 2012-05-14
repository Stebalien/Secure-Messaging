package edu.mit.securemessaging.activities;

import java.sql.SQLException;


import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Backend.InboxListener;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.widgets.ConversationAdapter;
import edu.mit.securemessaging.widgets.SimpleQueryAdapter;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class InboxActivity extends Activity {
    /** Called when the activity is first created. */
    private static Backend BACKEND = null;
    private ListView conversationList;
    private SimpleQueryAdapter<Conversation> adapter;
    
    private static final int DIALOG_MODIFY = 1;
    private static final int DIALOG_DELETE = 2;
    
    protected static final int DIALOG_MODIFY_DELETE = 0;
    
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
        conversationList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle bundle = new Bundle();
                bundle.putString("id", ((Conversation)parent.getItemAtPosition(position)).getID());
                showDialog(DIALOG_MODIFY, bundle);
                return true;
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
    
    
    @Override
    public Dialog onCreateDialog(int id, final Bundle bundle) {
        Dialog dialog;
        final String conversationId;
        switch(id) {
            case DIALOG_MODIFY:
                conversationId = bundle.getString("id");
                dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_modify_conversation_title)
                        .setItems( R.array.dialog_modify_inbox_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch(item) {
                                    case DIALOG_MODIFY_DELETE:
                                        showDialog(DIALOG_DELETE, bundle);
                                        break;
                                }
                            }
                        }).create();
                dialog.setCancelable(true);
                break;
            case DIALOG_DELETE:
                conversationId = bundle.getString("id");
                dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_delete_conversation_title)
                        .setMessage(R.string.dialog_delete_conversation_message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                try {
                                    BACKEND.getConversationDao().deleteById(conversationId);
                                    BACKEND.fireInboxUpdated();
                                    Toast.makeText(getApplicationContext(), "Conversation Deleted", Toast.LENGTH_SHORT).show();
                                } catch (SQLException e) {
                                    Log.e("INBOX", "Failed to delete conversation", e);
                                }
                        }})
                        .setNegativeButton(android.R.string.no, null).create();
                dialog.setCancelable(true);
                break;
            default:
                dialog = null;
        }
        return dialog;
        
    }
}