package edu.mit.securemessaging;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.util.Base64;
import android.util.Log;

public class Communicator {
    private static final String LOG_TAG = "SM_COMMUNICATOR";
    private final Backend BACKEND;
    
    private final String server;
    private String key;
    private String id = null;
    private static String DEFAULT_DOMAIN = "http://sm.drkencode.com";
    private static String REGISTER_TEMPLATE = "/registrar";
    private static String MESSENGER_TEMPLATE = "/messenger/%s/";
    private static final int BUFFER_SIZE = 4096;
    
    private class MessageSaver implements Runnable {
        private final JSONObject mObject;
        
        public MessageSaver(JSONObject obj) {
            mObject = obj;
        }
        
        public void run() {
            String message_id, conversation_id, sender_id, contents, sender_name;
            Date timestamp;

            // This is an error
            try {
                message_id = mObject.getString("id");
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            // These are required
            try {
                conversation_id = mObject.getString("conversation_id");
                sender_id = mObject.getString("sender_id");
                contents = mObject.getString("contents");
            } catch (JSONException e) {
                return;
            }

            // These are optional
            try {
                timestamp = new Date(mObject.getLong("timestamp"));
            } catch (JSONException e) {
                timestamp = new Date();
            }
            try {
                sender_name = mObject.getString("sender_name");
            } catch (JSONException e) {
                sender_name = "Anonymous";
            }

            try {
                Conversation conversation = BACKEND.getOrCreateConversation(conversation_id);
                Person sender = BACKEND.getOrCreatePerson(sender_id, sender_name);
                Message message = new Message(message_id, conversation, sender, timestamp, contents);
                conversation.addMessage(message);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    public Communicator() {
        this(null, null, DEFAULT_DOMAIN);
    }
    
    public Communicator(String server) {
        this(null, null, server);
    }
    
    public Communicator(String key, String id) {
        this(key, id, DEFAULT_DOMAIN);
    }
    
    public Communicator(String key, String id, String server) {
        BACKEND = Backend.getInstance();
        if (this.server == null) {
            this.server = DEFAULT_DOMAIN;
        } else {
            this.server = server;
        }
        
        if (this.key == null) {
            throw new NullPointerException("Key cannot be null.");
        } else {
            this.key = key;
        }
        this.id = id;
    }
    
    public String register(String key) throws MalformedURLException, IOException {
        
        if (id != null) {
            throw new IllegalStateException("ID already set.");
        }
        
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new URL(DEFAULT_DOMAIN + REGISTER_TEMPLATE).openStream()));
            id = in.readLine();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return id;
    }
    
    public void catchupMessages(Activity activity, long timestamp) {
        try {
            storeMessages(activity, retrieveMessageList(timestamp));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to retrieve message list.");
        }
    }

    /**
     * Download messages
     * @param ids - the ids of the messages to download.
     * @throws IOException 
     * @throws MalformedURLException 
     */
    public void storeMessages(Activity activity, String...ids) {
        if (id == null) {
            throw new IllegalStateException("User not registered.");
        }
        for (String id : ids) {
            try {
                activity.runOnUiThread(new MessageSaver(retrieveMessage(id)));
            } catch (IOException e) {
                Log.e(LOG_TAG, "Unable to retrieve message.", e);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Invalid message recieved. Ignoring.", e);
            }
        }
    }
    
    protected JSONObject retrieveMessage(String id) throws IOException, JSONException {
        InputStream in = null;
        try {
            in = new URL(String.format(server + MESSENGER_TEMPLATE, id)).openStream();
            JSONObject obj = new JSONObject(decrypt(in));
            obj.put("id", id);
            return obj;
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    /**
     * Base64 != encryption. This is a place holder.
     * @param in
     * @return
     */
    protected String decrypt(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        try {
            byte buffer[] = new byte[BUFFER_SIZE];
            int read = 0;
            while ((read = in.read(buffer)) != 0) {
                out.write(buffer, 0, read);
            }
        } finally {
            try { in.close(); } catch (IOException e) { }
        }
        
        return new String(Base64.decode(out.toByteArray(), Base64.DEFAULT), "UTF-8");
    }
    
    protected String[] retrieveMessageList(long after, long before) throws IOException {
        return retrieveMessageListHelper(String.format(server + MESSENGER_TEMPLATE + "?after=%l&before=%l", id, after, before));
    }
    
    protected String[] retrieveMessageList(long after) throws IOException {
        return retrieveMessageListHelper(String.format(server + MESSENGER_TEMPLATE + "?after=%l", id, after));
    }
    
    protected String[] retrieveMessageList() throws IOException {
        return retrieveMessageListHelper(String.format(server + MESSENGER_TEMPLATE, id));
    }
    
    private String[] retrieveMessageListHelper(String url) throws IOException {
        BufferedReader in = null;
        ArrayList<String> messageList = new ArrayList<String>();
        try {
            in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                messageList.add(line);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return messageList.toArray(new String[messageList.size()]);
    }
}
