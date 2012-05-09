package edu.mit.securemessaging.widgets;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.stmt.PreparedQuery;

import edu.mit.securemessaging.Common;
import edu.mit.securemessaging.Conversation;
import edu.mit.securemessaging.Message;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationAdapter extends SimpleQueryAdapter<Conversation> {

    final int viewResourceId;
    final Resources res;

    public ConversationAdapter(Activity context, int viewResourceId, PreparedQuery<Conversation> q, OrmLiteSqliteOpenHelper dbh) throws SQLException {
        super(context, viewResourceId, Conversation.class, q, dbh, ConversationMapping.class);
        this.viewResourceId = viewResourceId;
        res = context.getResources();
        
    }
    
    private class ConversationMapping extends Mapper {
        ImageView icon;
        TextView memberText;
        TextView messagePreview;
        TextView timeDisplay;

        @Override
        public void setView(View row) {
        	icon = (ImageView)row.findViewById(R.id.icon);
            memberText = (TextView)row.findViewById(R.id.memberText);
            messagePreview = (TextView)row.findViewById(R.id.messagePreview);
            timeDisplay = (TextView)row.findViewById(R.id.timestamp);
            
        }

        @Override
        public void update(Conversation convo) {
        	memberText.setText(Common.formatConversationTitle(convo));
        	
        	//set timestamp Textfield
        	timeDisplay.setText(Common.formatDate(convo.getTimestamp()));
        		
            try {
            	Message m = convo.getLatestMessage();
            	if (m == null) {
            	    this.messagePreview.setText("No messages");
            	} else {
                    this.messagePreview.setText(m.getContents());
            	}
            } catch (SQLException e) {
                this.messagePreview.setText("");
            }
            
            icon.setImageResource(Common.getConversationIcon(convo));
            
           //Bitmap bitmap = person.getPhoto();
            //if (bitmap != null) {
            //    this.photo.setImageBitmap(bitmap);
            //}
            
        }
    }
}
