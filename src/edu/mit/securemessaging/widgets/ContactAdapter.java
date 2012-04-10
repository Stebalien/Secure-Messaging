package edu.mit.securemessaging.widgets;

import java.util.List;

import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactAdapter extends ArrayAdapter<Person> {

    final int viewResourceId;
    final Resources res;

    public ContactAdapter(Context context, int viewResourceId, List<Person> contacts) {
        super(context, viewResourceId, contacts);
        this.viewResourceId = viewResourceId;
        res = context.getResources();
        
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ContactMapping map;
        if (row == null) {
            row = ((Activity)this.getContext()).getLayoutInflater().inflate(viewResourceId, parent, false);
            map = new ContactMapping(row);
            row.setTag(map);
        } else {
            map = (ContactMapping) row.getTag();
        }
        
        map.update(this.getItem(position));
        
        return row;
    }
    
    private class ContactMapping {
        ImageView photo;
        TextView name;
        TextView verified_text;
        TextView username;
        
        public ContactMapping(View row) {
            photo = (ImageView)row.findViewById(R.id.photo);
            name = (TextView)row.findViewById(R.id.name);
            username = (TextView)row.findViewById(R.id.username);
            verified_text = (TextView)row.findViewById(R.id.verified_text);
        }
        
        public void update(Person person) {
            this.name.setText(person.getName());
            this.username.setText(person.getUsername());
            
            switch (person.getTrustLevel()) {
                case VERIFIED:
                    this.verified_text.setText(R.string.verified);
                    this.verified_text.setTextColor(R.color.verified_color);
                    this.verified_text.setTextColor(res.getColor(R.color.verified_color));
                    break;
                case KNOWN:
                    this.verified_text.setText(R.string.unverified);
                    this.verified_text.setTextColor(res.getColor(R.color.unverified_color));
                    break;
                case UNKNOWN:
                    throw new RuntimeException("Contacts is unknown (invalid)");
                default:
                    throw new RuntimeException("Invalid trust level in person");
            }
            Bitmap bitmap = person.getPhoto();
            if (bitmap != null) {
                this.photo.setImageBitmap(bitmap);
            }
        }
    }
}
