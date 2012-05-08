package edu.mit.securemessaging.widgets;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.stmt.PreparedQuery;

import edu.mit.securemessaging.Message;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageAdapter extends SimpleQueryAdapter<Message> {

    final int viewResourceId;
    final Resources res;

    public MessageAdapter(Activity context, int viewResourceId, PreparedQuery<Message> q, OrmLiteSqliteOpenHelper dbh) throws SQLException {
        super(context, viewResourceId, Message.class, q, dbh, MessageMapping.class);
        this.viewResourceId = viewResourceId;
        res = context.getResources();
    }
    
    private class MessageMapping extends Mapper {
        TextView name;
        TextView verified_text;
        TextView message_text;
        ImageView lock_icon;
        View row;
        
        public void update(Message message) {
            Person sender = message.getSender();
            this.name.setText(sender.getName());
            this.message_text.setText(message.getContents());
            
            switch (sender.getTrustLevel()) {
                case ME:
                case VERIFIED:
                    this.verified_text.setText(R.string.verified);
                    this.lock_icon.setImageResource(R.drawable.ic_lock_verified);
                    this.verified_text.setTextColor(res.getColor(R.color.verified_color));
                    this.row.setBackgroundResource(R.drawable.bg_msg_verified);
                    break;
                case KNOWN:
                    this.verified_text.setText(R.string.unverified);
                    this.lock_icon.setImageResource(R.drawable.ic_lock_unverified);
                    this.verified_text.setTextColor(res.getColor(R.color.unverified_color));
                    this.row.setBackgroundResource(R.drawable.bg_msg_known);
                    break;
                case UNKNOWN:
                    this.verified_text.setText(R.string.unknown);
                    this.lock_icon.setImageResource(R.drawable.ic_lock_unknown);
                    this.verified_text.setTextColor(res.getColor(R.color.unknown_color));
                    this.row.setBackgroundResource(R.drawable.bg_msg_unknown);
                    break;
                default:
                    throw new RuntimeException("Invalid trust level in person");
            }
        }

        @Override
        public void setView(View row) {
            this.row = row;
            name = (TextView)row.findViewById(R.id.name);
            verified_text = (TextView)row.findViewById(R.id.verified_text);
            message_text = (TextView)row.findViewById(R.id.message_text);
            lock_icon = (ImageView)row.findViewById(R.id.lock_icon);
            
        }
    }
}
