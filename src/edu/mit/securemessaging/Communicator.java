package edu.mit.securemessaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.mit.securemessaging.Message.MessageType;

import android.util.Base64;
import android.util.Log;

public class Communicator {
    private static final String LOG_TAG = "SM_COMMUNICATOR";
    private final Backend BACKEND;
    public long last_check;
    
    private final String server;
    private final URL registrar;
    private String key;
    private String id = null;
    private static String DEFAULT_DOMAIN = "http://sm.drkencode.com";
    private static final String REGISTER_TEMPLATE = "/registrar";
    private static final String LIST_MESSAGES_TEMPLATE = "/messenger/%s/messages/";
    private static final String GET_MESSAGE_TEMPLATE = "/messenger/%s/messages/%s";
    private static final String NEW_MESSAGE_TEMPLATE = "/messenger/%s/new";
    private static final int BUFFER_SIZE = 4096;
    
    public Communicator() throws MalformedURLException {
        this(null, null, DEFAULT_DOMAIN);
    }
    
    public Communicator(String server) throws MalformedURLException {
        this(null, null, server);
    }
    
    public Communicator(String key, String id) throws MalformedURLException {
        this(key, id, DEFAULT_DOMAIN);
    }
    
    public Communicator(String key, String id, String server) throws MalformedURLException {
        BACKEND = Backend.getInstance();
        if (this.server == null) {
            this.server = DEFAULT_DOMAIN;
        } else {
            this.server = server;
        }
        
        this.registrar = new URL(server + REGISTER_TEMPLATE);
        this.key = key;
        this.id = id;
    }
    
    public String register(String key) throws MalformedURLException, IOException {
        
        if (id != null) {
            throw new IllegalStateException("ID already set.");
        }
        
        
        String data = URLEncoder.encode("data", "UTF-8") + "=" + URLEncoder.encode(key, "UTF-8");
        HttpURLConnection con = null;
        BufferedReader out = null;
        
        try {
            con = (HttpURLConnection) registrar.openConnection();
            post(con, data);
            out = new BufferedReader(new InputStreamReader(con.getInputStream()));
            this.id = out.readLine();
            this.key = key;
            return id;
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException e) { }
            }
            if (con != null) {
                con.disconnect();
            }
        }
        
    }
    
    private void assertIdSet() {
        if (id == null) throw new IllegalStateException("No user id specified.");
    }
    
    public void sendInviteAnnounce(Conversation conversation, Person person, String message) {
        try {
            // Copy and remove user to be invited.
            List<Person> people = conversation.getMembers();
            people.remove(person);
            JSONObject pObj = new JSONObject();
            pObj.put("name", person.getName());
            pObj.put("id", person.getID());
            send(people, encodeMessage(new Message(conversation, BACKEND.getMe(), message, MessageType.REFERRAL, pObj.toString())));
            
            JSONArray pArray = new JSONArray();
            for (Person p : people) {
                JSONObject o = new JSONObject();
                o.put("id", p.getID());
                o.put("name", p.getName());
                pArray.put(o);
            }
            send(person, encodeMessage(new Message(conversation, BACKEND.getMe(), message, MessageType.INVITE, pArray.toString())));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    public void sendMessage(Message m) {
        try {
            send(m.getConversation().getMembers(), encodeMessage(m));
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Sends the given message.
     * Fails silently FIXME
     */
    protected void send(List<Person> recipients, String messageData) {
        for (Person p : recipients) {
            send(p, messageData);
        }
    }
    
    protected void send(Person p, String messageData) {
        assertIdSet();
        HttpURLConnection con = null;
        BufferedReader out = null;
        try {
            con = (HttpURLConnection) getNewMessageURL(p.getID()).openConnection();
            con.setDoInput(true);
            post(con, URLEncoder.encode("data", "UTF-8") + "=" + URLEncoder.encode(p.encrypt(messageData), "UTF-8"));
            out = new BufferedReader(new InputStreamReader(con.getInputStream()));
            // Force the connection to complete.
            // I don't care about the output. There must be a better way to do this.
            out.readLine();
        } catch (IOException e) {
            // I really should do something here.
        } finally {
            if (out != null) {
                try { out.close(); } catch (IOException e) { }
            }
            if (con != null) {
                con.disconnect();
            }
        }
    }
    
    private URL getNewMessageURL(String uid) throws MalformedURLException {
        return new URL(String.format(server + NEW_MESSAGE_TEMPLATE, uid));
    }
    
    protected String encodeMessage(Message m) throws JSONException, UnsupportedEncodingException {
        JSONObject obj = new JSONObject();
        obj.put("contents", m.getContents());
        obj.put("sender_id", m.getSender().getID());
        obj.put("sender_name", m.getSender().getName());
        obj.put("conversation_id", m.getConversation().getID());
        obj.put("type", m.getType().toString());
        obj.put("extra", m.getExtra());
        return BACKEND.sign(obj.toString());
    }
    
    /**
     * Post the given data string to the given url.
     * @param url
     * @param data
     * @return
     * @throws IOException
     */
    protected void post(HttpURLConnection con, String data) throws IOException {
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(con.getOutputStream()));
            out.write(data);
            out.flush();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }
    
    public void catchupMessages() {
        catchupMessages(last_check);
    }
    
    public void catchupMessages(long timestamp) {
        try {
            storeMessages(retrieveMessageList(timestamp));
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to retrieve message list.", e);
        }
    }
    
    public void storeMessage(String mid) {
        final JSONObject mObject;
        try {
            mObject = retrieveMessage(mid);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Unable to retrieve message.", e);
            return;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Invalid message recieved. Ignoring.", e);
            return;
        }
                
        String message_id, conversation_id, sender_id, contents, sender_name, extra;
        Date timestamp;
        MessageType type;

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
            type = MessageType.valueOf(mObject.getString("type"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error decoding message", e);
            return;
        } catch (IllegalArgumentException e) {
            Log.e(LOG_TAG, "Invalid message type", e);
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
            extra = mObject.getString("extra");
        } catch (JSONException e) {
            extra = null;
        }
        
        final Conversation conversation;
        switch(type) {
            case INVITE:
                Log.i(LOG_TAG, "Recieved invite");
                try {
                    JSONArray extraObject = new JSONArray(extra);
                    Person[] members = new Person[extraObject.length() + 1];
                    members[0] = BACKEND.getOrCreatePerson(sender_id, sender_name);
                    for (int i = 1; i < extraObject.length(); i++) {
                        JSONObject pObject = extraObject.getJSONObject(i);
                        members[i] = BACKEND.getOrCreatePerson(pObject.getString("id"), pObject.getString("name"));
                    }
                    conversation = BACKEND.createConversation(conversation_id, members);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Failed to parse invite.");
                    return;
                } catch (SQLException e) {
                    Log.e(LOG_TAG, "Conversation cannot be created", e);
                    return;
                }
                break;
                
            case REFERRAL:
                Log.i(LOG_TAG, "Recieved referral");
                conversation = BACKEND.getConversation(conversation_id);
                break;
            case MESSAGE:
                Log.i(LOG_TAG, "Received Message");
                conversation = BACKEND.getConversation(conversation_id);
                break;
            default:
                return;
        }
        if (conversation == null) {
            Log.e(LOG_TAG, "Message without conversation." + conversation_id);
            return;
        }
        try {
            Person sender = BACKEND.getOrCreatePerson(sender_id, sender_name);
            Message message = new Message(message_id, conversation, sender, timestamp, contents, type, extra);
            conversation.addMessage(message);
        } catch (SQLException e) {
            Log.e(LOG_TAG, "Failed to add message.", e);
            return;
        }
    }

    /**
     * Download messages
     * @param ids - the ids of the messages to download.
     * @throws IOException 
     * @throws MalformedURLException 
     */
    public void storeMessages(String...mids) {
        if (id == null) {
            throw new IllegalStateException("User not registered.");
        }
        for (String mid : mids) {
            storeMessage(mid);
        }
    }
    
    protected JSONObject retrieveMessage(String mid) throws IOException, JSONException {
        InputStream in = null;
        try {
            in = new URL(String.format(server + GET_MESSAGE_TEMPLATE, id, mid)).openStream();
            JSONObject obj = new JSONObject(decrypt(in));
            obj.put("id", mid);
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
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } finally {
            try { in.close(); } catch (IOException e) { }
        }
        
        return new String(Base64.decode(out.toByteArray(), Base64.DEFAULT), "UTF-8");
    }
    
    protected String[] retrieveMessageList(long after, long before) throws IOException {
        return retrieveMessageListHelper(String.format(server + LIST_MESSAGES_TEMPLATE + "?after=%d&before=%d", id, after/1000 - 1, before/1000 + 1));
    }
    
    protected String[] retrieveMessageList(long after) throws IOException {
        return retrieveMessageListHelper(String.format(server + LIST_MESSAGES_TEMPLATE + "?after=%d", id, after/1000 - 1));
    }
    
    protected String[] retrieveMessageList() throws IOException {
        return retrieveMessageListHelper(String.format(server + LIST_MESSAGES_TEMPLATE, id));
    }
    
    private String[] retrieveMessageListHelper(String url) throws IOException {
        BufferedReader in = null;
        ArrayList<String> messageList = new ArrayList<String>();
        HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
        this.last_check = con.getDate();
        try {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
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
