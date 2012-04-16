package edu.mit.securemessaging;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class Common {
    private Common() {}
    
	private static final SimpleDateFormat dateFormatDay = new SimpleDateFormat("h:mma");
	private static final SimpleDateFormat dateFormatWeek = new SimpleDateFormat("E");
	private static final SimpleDateFormat dateFormatOld = new SimpleDateFormat("h:mma");
    
    public static String formatConversationTitle(Conversation c) {
        List<Person> memberList = c.getMembers();
        switch(memberList.size()) {
            case 0:
        		return "No Members";
            case 1:
                return memberList.get(0).getName();
            default:
                return memberList.get(0).getName()+", "+memberList.get(1).getName();
        }
    }
    public static String formatDate(Date time) {
    	Date now = new Date();
    	Long difference = now.getTime()-time.getTime();
    	if(difference<1000*60*60*24) //timestamp is within a day
    		return dateFormatDay.format(time);
    	else if(difference<2*1000*60*60*24) //timestamp is Yesterday
    		return "Yesterday";
    	else if(difference<7*1000*60*60*24) //timestamp is within a week
    		return dateFormatWeek.format(time);
    	else //more than a week old
    		return dateFormatOld.format(time);
    }
    
    public static int getConversationIcon(Conversation c) {
        switch (c.getTrustLevel()) {
            case VERIFIED:
                //set background color to green
            	if(c.isGroupConversation())//set icon to group conversation, green
                	return R.drawable.group_chat_green_32;
                else//set icon to single person conversation, green
                	return R.drawable.single_chat_green_32;
            case KNOWN:
                //set background color to grey
            	if(c.isGroupConversation())//set icon to group conversation, grey
            		return R.drawable.group_chat_grey_32;
                else //set icon to single person conversation, grey
                	return R.drawable.single_chat_grey_32;
            case UNKNOWN:
                //set background color to red
            	if(c.isGroupConversation())//set icon to group conversation, red
            		return R.drawable.group_chat_red_32;
                else //set icon to single person conversation, red 
                	return R.drawable.single_chat_red_32;
            default:
                throw new RuntimeException("Invalid trust level in person");
        }
    }
}
