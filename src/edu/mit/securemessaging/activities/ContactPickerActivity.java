package edu.mit.securemessaging.activities;

import java.sql.SQLException;

import com.j256.ormlite.stmt.QueryBuilder;

import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Membership;
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

public class ContactPickerActivity extends Activity {
    private static Backend BACKEND = null;
    /** Called when the activity is first created. */
    private ListView contactList;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (BACKEND == null) BACKEND = Backend.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_picker);
        contactList = (ListView)findViewById(R.id.contactList);
        contactList.setEmptyView((TextView)findViewById(R.id.contactListEmpty));
        String id = getIntent().getStringExtra("conversation_id");
        try {
            QueryBuilder<Person, String> b = BACKEND.getPersonDao().queryBuilder();
            b.orderBy(Person.NAME_FIELD, false);
            if (id != null) {
                QueryBuilder<Membership, String> subQuery = BACKEND.getMembershipDao().queryBuilder();
                subQuery.selectColumns(Membership.PERSON_FIELD);
                subQuery.where().eq(Membership.CONVERSATION_FIELD, id);
                b.where().notIn(Person.TRUST_FIELD, TrustLevel.UNKNOWN, TrustLevel.ME).and().notIn(Person.ID_FIELD, subQuery);
            } else {
                b.where().notIn(Person.TRUST_FIELD, TrustLevel.UNKNOWN, TrustLevel.ME);
            }
            contactList.setAdapter(
                    new ContactAdapter(this,
                            R.layout.contact,
                            b.prepare(),
                            BACKEND.getHelper())
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