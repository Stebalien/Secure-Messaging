package edu.mit.securemessaging.activities;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.widgets.ContactAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class ContactPickerActivity extends Activity {
    /** Called when the activity is first created. */
    private ListView contactList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_picker);
        contactList = (ListView)findViewById(R.id.contactList);
        contactList.setAdapter(new ContactAdapter(this, R.layout.contact, Backend.getInstance().getContacts()));
        
        contactList.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent result = new Intent();
                result.putExtra("id", ((Person)parent.getItemAtPosition(position)).getID());
                setResult(Activity.RESULT_OK, result);
                finish();
            }
        });
        
        // Set title
        ((TextView)findViewById(R.id.labelHeader)).setText(R.string.contact_picker_title);
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
}