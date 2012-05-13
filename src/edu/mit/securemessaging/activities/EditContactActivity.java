package edu.mit.securemessaging.activities;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;


import edu.mit.securemessaging.Backend;
import edu.mit.securemessaging.Common;
import edu.mit.securemessaging.Person;
import edu.mit.securemessaging.R;
import edu.mit.securemessaging.TrustLevel;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class EditContactActivity extends Activity {
    // Intents
    private static final int SELECT_PHOTO = 1;
    private static final int TAKE_PHOTO = 2;
    
    // Dialogs
    private static final int DIALOG_SET_PHOTO = 1;
    
    // Set photo results
    private static final int DIALOG_SET_PHOTO_SELECT = 0;
    private static final int DIALOG_SET_PHOTO_TAKE = 1;
    private static final int DIALOG_SET_PHOTO_CLEAR = 2;
    
    /** Called when the activity is first created. */
    private Backend backend = Backend.getInstance();
    private EditText nameField;
    private TextView nameError;
    private CheckBox verifiedField;
    private Person contact;
    private ImageButton photoButton;
    
    private Uri updatedUri = null;
    private boolean photoChanged = false;
    private File cache = null;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String id = getIntent().getStringExtra("id");
        setContentView(R.layout.edit_contact_activity);
        
        nameField = (EditText)findViewById(R.id.txtName);
        verifiedField = ((CheckBox)findViewById(R.id.chkVerified));
        nameError = (TextView)findViewById(R.id.lableNameError);
        photoButton = (ImageButton)findViewById(R.id.photoButton);
        
        try {
            contact = backend.getPerson(id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        Common.setFormatedKey(id, (TextView)findViewById(R.id.lableFingerprint1), (TextView)findViewById(R.id.lableFingerprint2));
        
        if (contact == null) {
            ((TextView)findViewById(R.id.labelHeader)).setText(R.string.edit_contact_title);
            String name = getIntent().getStringExtra("name");
            if (name != null) {
                nameField.setText(name);
            }
            verifiedField.setChecked(true);
        } else {
            ((TextView)findViewById(R.id.labelHeader)).setText(R.string.add_contact_title);
            nameField.setText(contact.getName());
            verifiedField.setChecked(contact.getTrustLevel().equals(TrustLevel.VERIFIED));
            Bitmap photo = contact.getPhoto();
            if (photo != null) {
                photoButton.setImageBitmap(photo);
            }
        }
        
        photoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                showDialog(DIALOG_SET_PHOTO);
            }
        });
        
        
        ((Button)findViewById(R.id.btnSave)).setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (nameField.getText().toString().equals("")) {
                    nameError.setText(R.string.invalid_name);
                    nameError.setVisibility(View.VISIBLE);
                    return;
                } else {
                    nameError.setVisibility(View.GONE);
                }
                
                TrustLevel trustLevel = verifiedField.isChecked() ? TrustLevel.VERIFIED : TrustLevel.KNOWN;
                String name = nameField.getText().toString();
                try {
                    if (contact == null) {
                        contact = new Person(id, name, trustLevel);
                    } else {
                        contact.setName(name);
                        contact.setTrustLevel(trustLevel);
                    }
                    if (photoChanged) {
                        if (updatedUri == null) {
                            contact.deletePhoto();
                        } else {
                            contact.setPhoto(updatedUri);
                        }
                    }
                    backend.addOrUpdateContact(contact);
                    nameError.setVisibility(View.GONE);
                } catch (SQLException e) {
                    nameError.setText(R.string.conflict_name);
                    nameError.setVisibility(View.VISIBLE);
                    return;
                }
                Backend.getInstance().fireContactsUpdated();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        final Dialog dialog;
        switch(id) {
            case DIALOG_SET_PHOTO:
                dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.dialog_set_photo_title)
                        .setItems(R.array.dialog_set_photo_menu, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                Intent intent = new Intent();
                                switch(item) {
                                    case DIALOG_SET_PHOTO_SELECT:
                                        intent.setType("image/*");
                                        intent.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(Intent.createChooser(intent, "Select Photo"), SELECT_PHOTO);
                                        break;
                                    case DIALOG_SET_PHOTO_TAKE:
                                        try {
                                            cache = File.createTempFile("photo", ".jpg", getBaseContext().getCacheDir());
                                        } catch (IOException e) {
                                            throw new RuntimeException(e);
                                        }
                                        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cache));
                                        startActivityForResult(intent, SELECT_PHOTO);
                                        break;
                                    case DIALOG_SET_PHOTO_CLEAR:
                                        updatedUri = null;
                                        photoChanged = true;
                                        photoButton.setImageResource(R.drawable.ic_default_photo);
                                        break;
                                }
                            }
                        }).create();
                dialog.setCancelable(true);
                break;
            default:
                dialog = null;
        }
        return dialog;
    }
    
    public void onDestroy() {
        super.onDestroy();
        if (cache != null) {
            cache.delete();
        }
    }
    
    @Override
    public void onActivityResult(int request, int result, Intent data) {
        if (result != RESULT_OK) {
            return;
        }
        switch(request) {
            case SELECT_PHOTO:
            case TAKE_PHOTO:
                photoChanged = true;
                updatedUri = data.getData();
                photoButton.setImageURI(updatedUri);
                break;
            default:
                //
        }
    }
    
    @Override
    public void onBackPressed() {
        finish();
    }
}