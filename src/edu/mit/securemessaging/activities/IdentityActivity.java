package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Common;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class IdentityActivity extends Activity {
    /** Called when the activity is first created. */
    private TextView labelName, labelFingerprint1, labelFingerprint2;
    private ImageView imgQRCode;
    private Person me;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.identity);
        me = Backend.getInstance().getMe();
        
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.identity_title);
        labelFingerprint1 = (TextView)findViewById(R.id.myFingerprint1);
        labelFingerprint2 = (TextView)findViewById(R.id.myFingerprint2);
        labelName = (TextView)findViewById(R.id.myName);
        imgQRCode = (ImageView)findViewById(R.id.QRCode);
    }

    @Override
    public void onResume() {
        super.onResume();
        imgQRCode.setImageBitmap(me.getQRCode());
        Common.setFormatedKey(me.getID(), labelFingerprint1, labelFingerprint2);
        labelName.setText(me.getName());
    }
}