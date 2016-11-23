package com.dalbers.dialer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

import com.dalbers.dialer.DataHandling.Contact;
import com.dalbers.dialer.DataHandling.Recent;

import java.io.InputStream;
import java.util.ArrayList;

import static android.provider.ContactsContract.Contacts.openContactPhotoInputStream;
import static com.dalbers.dialer.DataHandling.GetData.getBitmapFromXML;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        //pull data from bundle
        Bundle bundle = getIntent().getBundleExtra("data");
        ArrayList<Recent> recents = bundle.getParcelableArrayList("recents");
        ArrayList<Recent> recentFromContact = new ArrayList<>();
        Contact contact = bundle.getParcelable("contact");
        //get recents from this contact
        for (Recent recent : recents) {
            if (contact.getLookup().equals(recent.getContactLookup()))
                recentFromContact.add(recent);
        }
        //put recents in a list
        RecyclerView history = (RecyclerView) findViewById(R.id.history);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        history.setLayoutManager(layoutManager);
        RecentsAdapter adapter = new RecentsAdapter(recentFromContact, false, this);
        history.setAdapter(adapter);

        String name = contact.getName();
        String number = contact.getNumber();

        TextView nameTextView = (TextView) findViewById(R.id.name);
        nameTextView.setText(name);

        TextView numberTextView = (TextView) findViewById(R.id.number);
        numberTextView.setText(number);

        //get the contact's photo with a preference for high quality
        ContentResolver contentResolver = getContentResolver();
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
        InputStream is = openContactPhotoInputStream(contentResolver, contactUri, true);
        ImageView image = (ImageView)findViewById(R.id.profilePicture);
        Bitmap profilePic;
        if(is != null)
            profilePic = BitmapFactory.decodeStream(is);
        else //no image found, use generic
            profilePic = getBitmapFromXML(R.drawable.generic_person, this, 480, 480);
        //make the picture at least 480x480
        if(profilePic.getWidth() < 480)
            profilePic = Bitmap.createScaledBitmap(profilePic,480,480,false);
        image.setImageBitmap(profilePic);
    }
}
