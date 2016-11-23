package com.dalbers.dialer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dalbers.dialer.DataHandling.Contact;
import com.dalbers.dialer.DataHandling.Recent;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import static android.provider.ContactsContract.Contacts.openContactPhotoInputStream;
import static com.dalbers.dialer.DataHandling.GetData.getBitmapFromXML;

/**
 * Created by davidalbers on 10/29/16.
 */
public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ViewHolder> {
    public void setContacts(ArrayList<Contact> contacts) {
        this.contacts = contacts;
    }

    private ArrayList<Contact> contacts;
    private ArrayList<Recent> recents;
    private RecyclerView recyclerView;
    private int mExpandedPosition = -1;
    private HashMap<String,Bitmap> contactPhotos = new HashMap<>();
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView number;
        public ImageView image;
        public LinearLayout expandedViews;
        public Button createNew;
        public Button call;
        public Button sendMessage;
        public Button history;
        public ViewHolder(View v) {
            super(v);
            name = (TextView)v.findViewById(R.id.name);
            number = (TextView)v.findViewById(R.id.number);
            image = (ImageView)v.findViewById(R.id.profilePicture);
            expandedViews = (LinearLayout)v.findViewById(R.id.expanded_views);
            createNew = (Button)v.findViewById(R.id.create_new);
            call = (Button)v.findViewById(R.id.call);
            sendMessage = (Button)v.findViewById(R.id.send_message);
            history = (Button)v.findViewById(R.id.history);
        }
    }

    /**
     * Displays a list of contacts as cards
     * @param contacts A list of contacts
     * @param recents A list of recents to link the contacts to
     * @param recyclerView the view that uses this adapter
     */
    public ContactsAdapter(ArrayList<Contact> contacts, ArrayList<Recent> recents, RecyclerView recyclerView) {
        this.contacts = contacts;
        this.recyclerView = recyclerView;
        this.recents = recents;
        //preload any available contact thumbnails
        //this would be better to do in the background
        ContentResolver contentResolver = recyclerView.getContext().getContentResolver();
        for(Contact contact : this.contacts) {
            Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contact.getId());
            InputStream is = openContactPhotoInputStream(contentResolver,contactUri,false);
            if(is != null)
                contactPhotos.put(contact.getLookup(),BitmapFactory.decodeStream(is));
        }
    }

    @Override
    public ContactsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_card_layout, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.name.setText(contacts.get(position).getName());
        holder.number.setText(contacts.get(position).getNumber());

        if(contactPhotos.containsKey(contacts.get(position).getLookup()))
            holder.image.setImageBitmap(contactPhotos.get(contacts.get(position).getLookup()));
        else {
            Bitmap bm = getBitmapFromXML(R.drawable.generic_person, holder.image.getContext());
            holder.image.setImageBitmap(bm);
        }
        final boolean isExpanded = position==mExpandedPosition;
        holder.expandedViews.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
        holder.itemView.setActivated(isExpanded);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //expand/shrink specific card
                if(isExpanded)
                    mExpandedPosition = -1;
                else
                    mExpandedPosition = position;
                TransitionManager.beginDelayedTransition(recyclerView);
                notifyDataSetChanged();
            }
        });
        holder.createNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Create New Contact From " + contacts.get(position).getNumber(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Call " + contacts.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Send Message to " + contacts.get(position).getName(), Toast.LENGTH_SHORT).show();
            }
        });
        holder.history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start up the history activity
                Intent showHistory = new Intent(view.getContext(),HistoryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putParcelableArrayList("recents",recents);
                bundle.putParcelable("contact",contacts.get(position));
                showHistory.putExtra("data",bundle);
                view.getContext().startActivity(showHistory);
            }}
        );
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

}
