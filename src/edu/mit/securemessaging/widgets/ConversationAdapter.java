package edu.mit.securemessaging.widgets;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import edu.mit.securemessaging.Common;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.Message;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationAdapter extends ArrayAdapter<Conversation> {

    final int viewResourceId;
    final Resources res;

    public ConversationAdapter(Context context, int viewResourceId, List<Conversation> conversations) {
        super(context, viewResourceId, conversations);
        this.viewResourceId = viewResourceId;
        res = context.getResources();
        
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ConversationMapping map;
        if (row == null) {
            row = ((Activity)this.getContext()).getLayoutInflater().inflate(viewResourceId, parent, false);
            map = new ConversationMapping(row);
            row.setTag(map);
        } else {
            map = (ConversationMapping) row.getTag();
        }
        
        map.update(this.getItem(position));
        
        return row;
    }
    
    private class ConversationMapping {
        ImageView icon;
        TextView memberText;
        TextView messagePreview;
        TextView timeDisplay;
        
        public ConversationMapping(View row) {
        	icon = (ImageView)row.findViewById(R.id.icon);
            memberText = (TextView)row.findViewById(R.id.memberText);
            messagePreview = (TextView)row.findViewById(R.id.messagePreview);
            timeDisplay = (TextView)row.findViewById(R.id.timestamp);
        }
        
        public void update(Conversation convo) {
        	List<Message> messageList = convo.getMessages();
        	
        	memberText.setText(Common.formatConversationTitle(convo));
        	
        	//set timestamp Textfield
        	timeDisplay.setText(Common.formatDate(convo.getTimestamp()));
        		
            if(messageList.isEmpty())
            	this.messagePreview.setText("No messages");
            else
            	this.messagePreview.setText(convo.getLatestMessage().getContents());
            
            icon.setImageResource(Common.getConversationIcon(convo));
            
           //Bitmap bitmap = person.getPhoto();
            //if (bitmap != null) {
            //    this.photo.setImageBitmap(bitmap);
            //}
        }
    }
}
