package com.dalbers.dialer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dalbers.dialer.DataHandling.Contact;
import com.dalbers.dialer.DataHandling.Recent;
import com.dalbers.dialer.DataHandling.RecentSms;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import static android.provider.ContactsContract.Contacts.openContactPhotoInputStream;
import static com.dalbers.dialer.DataHandling.GetData.getBitmapFromXML;

/**
 * Created by davidalbers on 10/29/16.
 */
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.GenericViewHolder> {
    private ArrayList<Recent> recents;
    private final int GENERIC_CARD_TYPE = 0;
    private final int SMS_CARD_TYPE = 1;
    private boolean showIdentity = false;
    private HashMap<String,Bitmap> contactPhotos = new HashMap<>();

    public static class SmsViewHolder extends GenericViewHolder {
        public TextView body;
        public TextView nameOrNumber;
        public TextView date;
        public ImageView image;
        public SmsViewHolder(View v) {
            super(v);
            body = (TextView)v.findViewById(R.id.body);
            nameOrNumber = (TextView)v.findViewById(R.id.nameOrNumber);
            date = (TextView)v.findViewById(R.id.date);
            image = (ImageView)v.findViewById(R.id.profilePicture);
        }
    }

    public static class GenericViewHolder extends RecyclerView.ViewHolder {
        public TextView nameOrNumber;
        public TextView date;
        public ImageView image;
        public GenericViewHolder(View v) {
            super(v);
            nameOrNumber = (TextView)v.findViewById(R.id.nameOrNumber);
            date = (TextView)v.findViewById(R.id.date);
            image = (ImageView)v.findViewById(R.id.profilePicture);
        }
    }

    /**
     * Displays a list of Recents as cards
     * @param recents a list of Recents, can be mixed Recent or RecentSms
     * @param showIdentity show identifying values like name or picture,
     *                     not needed if recents belong to one person
     * @param context a context to do queries from
     */
    public RecentsAdapter(ArrayList<Recent> recents, boolean showIdentity, Context context) {
        this.recents = recents;
        this.showIdentity = showIdentity;
        //preload any available contact thumbnails
        //this would be better to do in the background
        ContentResolver contentResolver = context.getContentResolver();
        for(Recent recent : this.recents) {
            if(recent.getContactId() != Contact.INVALID_ID) {
                Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, recent.getContactId());
                InputStream is = openContactPhotoInputStream(contentResolver, contactUri, false);
                if (is != null) {
                    contactPhotos.put(recent.getContactLookup(), BitmapFactory.decodeStream(is));
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        switch (recents.get(position).getType())
        {
            case CALL:
               return GENERIC_CARD_TYPE;
            case UNKNOWN:
                return GENERIC_CARD_TYPE;
            case SMS:
                return SMS_CARD_TYPE;
            default:
                return GENERIC_CARD_TYPE;
        }
    }

    @Override
    public RecentsAdapter.GenericViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v;
        GenericViewHolder vh;
        //we have a generic card, used for calls
        //but if this is an SMS, we have a more specific card
        //use view type to determine this
        switch(viewType)
        {
            case SMS_CARD_TYPE:
                v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sms_card_layout, parent, false);
                vh = new SmsViewHolder(v);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.call_card_layout, parent, false);
                vh = new GenericViewHolder(v);
                break;
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(GenericViewHolder holder, int position) {
        //format date like 1:23 PM Monday
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a EEEE", Locale.getDefault());
        String formattedDate = formatter.format(recents.get(position).getDate());
        holder.date.setText(formattedDate);

        holder.nameOrNumber.setText(recents.get(position).getContactNameOrNumber());
        //get photo, if there is one. if not use generic
        if(contactPhotos.containsKey(recents.get(position).getContactLookup()))
            holder.image.setImageBitmap(contactPhotos.get(recents.get(position).getContactLookup()));
        else {
            Bitmap bm = getBitmapFromXML(R.drawable.generic_person, holder.image.getContext());
            holder.image.setImageBitmap(bm);
        }
        //set body if sms
        if(recents.get(position).getType() == Recent.RecentTypes.SMS) {
            ((SmsViewHolder)holder).body.setText(((RecentSms) recents.get(position)).getBody());
        }
        //hide identity views (e.g. name and image)
        if(!showIdentity) {
            holder.nameOrNumber.setVisibility(View.GONE);
            holder.image.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return recents.size();
    }

}
