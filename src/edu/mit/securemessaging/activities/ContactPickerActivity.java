package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;

import edu.mit.securemessaging.DatabaseHelper;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import edu.mit.securemessaging.widgets.ContactAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class ContactPickerActivity extends OrmLiteBaseActivity<DatabaseHelper> {
    /** Called when the activity is first created. */
    private ListView contactList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_picker);
        contactList = (ListView)findViewById(R.id.contactList);
        try {
            contactList.setAdapter(
                    new ContactAdapter(this,
                            R.layout.contact,
                            getHelper().getPersonDao().queryBuilder().orderBy("name", false).where().notIn(Person.TRUST_FIELD, TrustLevel.UNKNOWN, TrustLevel.ME).prepare(),
                            getHelper())
                    );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
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