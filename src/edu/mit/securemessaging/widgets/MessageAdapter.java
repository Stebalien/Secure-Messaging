package edu.mit.securemessaging.widgets;

import java.util.List;

import edu.mit.securemessaging.Message;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<Message> {

    final int viewResourceId;
    final Resources res;

    public MessageAdapter(Context context, int viewResourceId, List<Message> messages) {
        super(context, viewResourceId, messages);
        this.viewResourceId = viewResourceId;
        res = context.getResources();
        
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        MessageMapping map;
        if (row == null) {
            row = ((Activity)this.getContext()).getLayoutInflater().inflate(viewResourceId, parent, false);
            map = new MessageMapping(row);
            row.setTag(map);
        } else {
            map = (MessageMapping) row.getTag();
        }
        
        map.update(this.getItem(position));
        
        return row;
    }
    
    private class MessageMapping {
        TextView name;
        TextView verified_text;
        TextView message_text;
        ImageView lock_icon;
        View row;
        //TextView username;
        
        public MessageMapping(View row) {
            this.row = row;
            name = (TextView)row.findViewById(R.id.name);
            //username = (TextView)row.findViewById(R.id.username);
            verified_text = (TextView)row.findViewById(R.id.verified_text);
            message_text = (TextView)row.findViewById(R.id.message_text);
            lock_icon = (ImageView)row.findViewById(R.id.lock_icon);
        }
        
        public void update(Message message) {
            Person sender = message.getSender();
            this.name.setText(sender.getName());
            this.message_text.setText(message.getContents());
            
            switch (sender.getTrustLevel()) {
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
    }
}
