package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.R;
import edu.mit.securemessaging.R.layout;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SettingsActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        
        
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.settings_title);
    }
}