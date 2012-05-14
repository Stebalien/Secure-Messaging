package edu.mit.securemessaging.activities;

import java.util.Timer;
import java.util.TimerTask;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.R;
import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends TabActivity {
    private Timer messagePoller; 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.main);
        super.onCreate(savedInstanceState);
        Resources res = getResources(); // Resource object to get Drawables
        TabHost tabHost = getTabHost();  // The activity TabHost
        TabHost.TabSpec spec;  // Resusable TabSpec for each tab
        Intent intent;  // Reusable Intent for each tab

        // Create an Intent to launch an Activity for the tab (to be reused)
        intent = new Intent().setClass(this, InboxActivity.class);

        // Initialize a TabSpec for each tab and add it to the TabHost
        spec = tabHost.newTabSpec("tabInbox").setIndicator("Inbox",
                          res.getDrawable(R.drawable.ic_tab_inbox)
                ).setContent(intent);
        tabHost.addTab(spec);

        // Do the same for the other tabs
        intent = new Intent().setClass(this, ContactsActivity.class);
        spec = tabHost.newTabSpec("tabContacts").setIndicator("Contacts",
                          res.getDrawable(R.drawable.ic_tab_contacts)
                          ).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent().setClass(this, IdentityActivity.class);
        spec = tabHost.newTabSpec("tabIdentity").setIndicator("Identity",
                          res.getDrawable(R.drawable.ic_tab_fingerprint)
                ).setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent().setClass(this, SettingsActivity.class);
        spec = tabHost.newTabSpec("tabSettings").setIndicator("Settings",
                          res.getDrawable(R.drawable.ic_tab_settings)
                ) .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
        messagePoller = new Timer();
        final Backend backend = Backend.getInstance();
        messagePoller.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                backend.catchupMessages();
            }
        }, 0, 6000);
        
    }
    
    public void onDestroy() {
        messagePoller.cancel();
        super.onDestroy();
    }
}