package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class IdentityActivity extends Activity {
    /** Called when the activity is first created. */
    TextView labelName, labelFingerprint1, labelFingerprint2;
    Person me;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity);
        me = Backend.getInstance().getMe();
        
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.identity_title);
        
        labelName = (TextView)findViewById(R.id.myName);
        labelName.setText(me.getName());
        
        String fingerprint = me.getID();
        int half = fingerprint.length()/2;
        
        labelFingerprint1 = (TextView)findViewById(R.id.myFingerprint1);
        labelFingerprint2 = (TextView)findViewById(R.id.myFingerprint2);
        
        labelFingerprint1.setText(fingerprint.substring(0, half).replaceAll("(....)", "$1 "));
        labelFingerprint2.setText(fingerprint.substring(half).replaceAll("(....)", "$1 "));
    }
}