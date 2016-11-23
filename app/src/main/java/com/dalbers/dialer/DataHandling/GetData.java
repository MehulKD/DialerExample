package com.dalbers.dialer.DataHandling;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * Created by davidalbers on 10/29/16.
 */
public class GetData {
    public static ArrayList<Contact> getContacts(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionContacts = checkSelfPermission(context,android.Manifest.permission.READ_CONTACTS);
            //if on Marshmallow or up, return no calls if the user has removed our permission
            if(permissionContacts != PermissionChecker.PERMISSION_GRANTED) return null;
        }
        ArrayList<Contact> contacts = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        //select all contacts from database
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, "DISPLAY_NAME ASC");
        //iterate over contacts
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String lookupKey = cursor.getString(
                        cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                long id = cursor.getLong(
                        cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));
                if (cursor.getInt(cursor.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    //group phone numbers held by same contact
                    Cursor matchCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY +" = ?",
                            new String[]{lookupKey}, null);
                    if(matchCursor != null) {
                        while (matchCursor.moveToNext()) {
                            String number = matchCursor.getString(matchCursor.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contacts.add(new Contact(name,lookupKey,number,id));
                        }
                        matchCursor.close();
                    }
                }
            }
            cursor.close();
       }
        Log.d("got", contacts.size() + " contacts");
        return contacts;
    }

    public static ArrayList<Sms> getSms(Context context, int maxDaysAgo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionSms = checkSelfPermission(context, android.Manifest.permission.READ_SMS);
            int permissionContacts = checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS);
            //if on Marshmallow or up, return no calls if the user has removed our permission
            if(permissionSms != PermissionChecker.PERMISSION_GRANTED ||
                    permissionContacts != PermissionChecker.PERMISSION_GRANTED)
                return null;
        }
        ArrayList<Sms> sms = new ArrayList<>();
        String timeLimitSelection = null;
        if(maxDaysAgo != -1) {
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_MONTH, -1*maxDaysAgo);
            Date sevenDaysAgo = cal.getTime();
            timeLimitSelection = "DATE >= " + sevenDaysAgo.getTime();
        }
        ContentResolver contentResolver = context.getContentResolver();
        //select all sms from database
        Cursor cursor = contentResolver.query(Telephony.Sms.Inbox.CONTENT_URI,
                null, timeLimitSelection, null, "DATE DESC");
        //iterate over sms
        HashMap<String, String> numberToAddress = new HashMap<>();
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String address = cursor.getString(
                        cursor.getColumnIndex(Telephony.TextBasedSmsColumns.ADDRESS));
                String body = cursor.getString(
                        cursor.getColumnIndex(Telephony.TextBasedSmsColumns.BODY));
                String dateStr = cursor.getString(
                        cursor.getColumnIndex(Telephony.TextBasedSmsColumns.DATE));
                String type = cursor.getString(cursor.getColumnIndex(
                        Telephony.TextBasedSmsColumns.TYPE));
                boolean outgoing = false;
                if(Integer.parseInt(type) == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX)
                    outgoing = true;
                Date date = new Date(Long.parseLong(dateStr));
                String lookup = "";
                if(numberToAddress.containsKey(address))
                   lookup = numberToAddress.get(address);
                else {
                    lookup = matchNumberToLookup(address, context);
                    if(lookup != null)
                        numberToAddress.put(address,lookup);
                }
                sms.add(new Sms(address,body,date,outgoing,lookup));
            }
            cursor.close();
            if(sms.size() > 20)
                return sms;
        }
        return sms;
    }

    public static ArrayList<Recent> getRecents(Context context, ArrayList<Contact> contacts, int maxDaysAgo) {
        ArrayList<Recent> recents = new ArrayList<>();
        ArrayList<Call> calls = getCalls(context, maxDaysAgo);
        for(Call call : calls) {
            Contact matchedContact = findContactByLookup(call.getLookup(),contacts);
            if(matchedContact != null)
                recents.add(new Recent(matchedContact.getName(),call.getDate(), Recent.RecentTypes.CALL, matchedContact.getLookup(),matchedContact.getId()));
            else
                recents.add(new Recent(call.getNumber(),call.getDate(), Recent.RecentTypes.CALL,"",Contact.INVALID_ID));
        }
        ArrayList<Sms> smses = getSms(context,maxDaysAgo);
        for(Sms sms : smses) {
            Contact matchedContact = findContactByLookup(sms.getLookup(),contacts);
            if(matchedContact != null)
                recents.add(new RecentSms(matchedContact.getName(),sms.getDate(), Recent.RecentTypes.SMS,sms.getBody(),matchedContact.getLookup(),matchedContact.getId()));
            else
                recents.add(new RecentSms(sms.getAddress(),sms.getDate(), Recent.RecentTypes.SMS,sms.getBody(),"",Contact.INVALID_ID));
        }
        Collections.sort(recents);
        Collections.reverse(recents);
        return recents;
    }

    public static Contact findContactByLookup(String lookup, ArrayList<Contact> contacts) {
        for(Contact contact : contacts) {
            if(contact.getLookup().equals(lookup))
                return contact;
        }
        return null;
    }

    public static ArrayList<Call> getCalls(Context context, int maxDaysAgo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionCalls = checkSelfPermission(context, android.Manifest.permission.READ_CALL_LOG);
            int permissionContacts = checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS);
            //if on Marshmallow or up, return no calls if the user has removed our permission
            if(permissionCalls != PermissionChecker.PERMISSION_GRANTED ||
                    permissionContacts != PermissionChecker.PERMISSION_GRANTED)
                return null;
        }
        ArrayList<Call> calls = new ArrayList<>();
        String timeLimitSelection = null;
        if(maxDaysAgo != -1) {
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_MONTH, -1*maxDaysAgo);
            Date sevenDaysAgo = cal.getTime();
            timeLimitSelection = "DATE >= " + sevenDaysAgo.getTime();
        }
        ContentResolver contentResolver = context.getContentResolver();
        //select all calls from database
        Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI,
                null, timeLimitSelection, null, "DATE DESC");
        //iterate over calls
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String number = cursor.getString(
                        cursor.getColumnIndex(CallLog.Calls.NUMBER));
                String dateStr = cursor.getString(
                        cursor.getColumnIndex(CallLog.Calls.DATE));
                Date date = new Date(Long.parseLong(dateStr));
                String lookup = matchNumberToLookup(number, context);
                calls.add(new Call(number,date,lookup));
            }
            cursor.close();
        }
        Log.d("got", calls.size() + " calls");
        return calls;
    }

    public static String matchNumberToLookup(String numToMatch, Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionContacts = checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS);
            //if on Marshmallow or up, return no calls if the user has removed our permission
            if(permissionContacts != PermissionChecker.PERMISSION_GRANTED) return null;
        }
        if(numToMatch == null || numToMatch.isEmpty())
            return null;
        String lookup = null;
        //Special uri for matching a phone number to contact
        Uri uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(numToMatch));
        ContentResolver contentResolver = context.getContentResolver();
        //match a contact to a number
        Cursor cursorContact = contentResolver.query(uri,
                null, null, null, null);
        if(cursorContact != null) {
            if(cursorContact.getCount() > 0) {
                cursorContact.moveToFirst();
                lookup = cursorContact.getString(cursorContact.getColumnIndex(
                        ContactsContract.PhoneLookup.LOOKUP_KEY));
            }
            cursorContact.close();
        }

        return lookup;
    }

    public static Bitmap getBitmapFromXML(int drawableRes,Context context) {
        Drawable drawable = context.getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = android.graphics.Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Bitmap getBitmapFromXML(int drawableRes,Context context, int width, int height) {
        Drawable drawable = context.getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = android.graphics.Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }


}
