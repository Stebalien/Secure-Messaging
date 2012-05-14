package edu.mit.securemessaging.widgets;

import java.sql.SQLException;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.stmt.PreparedQuery;

import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends SimpleQueryAdapter<Person> {

    final int viewResourceId;
    final Resources res;

    public ContactAdapter(Activity context, int viewResourceId, PreparedQuery<Person> q, OrmLiteSqliteOpenHelper dbh) throws SQLException {
        super(context, viewResourceId, Person.class, q, dbh, ContactMapping.class);
        this.viewResourceId = viewResourceId;
        res = context.getResources();
    }
    
    private class ContactMapping extends Mapper{
        ImageView photo;
        TextView name;
        TextView verified_text;
        
        public void update(Person person) {
            this.name.setText(person.getName());
            
            switch (person.getTrustLevel()) {
                case VERIFIED:
                    this.verified_text.setText(R.string.verified);
                    this.verified_text.setTextColor(res.getColor(R.color.verified_color));
                    break;
                case KNOWN:
                    this.verified_text.setText(R.string.unverified);
                    this.verified_text.setTextColor(res.getColor(R.color.unverified_color));
                    break;
                case UNKNOWN:
                    throw new RuntimeException("Contacts is unknown (invalid)");
                case ME:
                    throw new RuntimeException("I can't be in my own contacts.");
                default:
                    throw new RuntimeException("Invalid trust level in person");
            }
            Bitmap bitmap = person.getPhoto();
            if (bitmap != null) {
                this.photo.setImageBitmap(bitmap);
            }
        }

        @Override
        public void setView(View row) {
            photo = (ImageView)row.findViewById(R.id.photo);
            name = (TextView)row.findViewById(R.id.name);
            verified_text = (TextView)row.findViewById(R.id.verified_text);
        }
    }
}
