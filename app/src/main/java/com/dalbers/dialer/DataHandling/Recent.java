package com.dalbers.dialer.DataHandling;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;
/**
 * Created by davidalbers on 10/29/16.
 */
public class Recent implements Parcelable,Comparable {

    private String contactNameOrNumber;
    private Date date;
    private RecentTypes type;

    public long getContactId() {
        return contactId;
    }

    private long contactId;

    public String getContactLookup() {
        return contactLookup;
    }

    private String contactLookup = "";

    public enum RecentTypes {
        SMS,
        CALL,
        UNKNOWN
    }

    public Recent(String contactNameOrNumber, Date date, RecentTypes type,String contactLookup, long contactId) {
        this.contactNameOrNumber = contactNameOrNumber;
        this.date = date;
        this.type = type;
        this.contactLookup = contactLookup;
        this.contactId = contactId;
    }

    public String getContactNameOrNumber() {
        return contactNameOrNumber;
    }

    public RecentTypes getType() {
        return type;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public int compareTo(Object toCompare) {
        return this.date.compareTo(((Recent)toCompare).getDate());
    }

    protected Recent(Parcel in) {
        contactNameOrNumber = in.readString();
        long tmpDate = in.readLong();
        date = tmpDate != -1 ? new Date(tmpDate) : null;
        type = (RecentTypes) in.readValue(RecentTypes.class.getClassLoader());
        contactLookup = in.readString();
        contactId = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(contactNameOrNumber);
        dest.writeLong(date != null ? date.getTime() : -1L);
        dest.writeValue(type);
        dest.writeString(contactLookup);
        dest.writeLong(contactId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Recent> CREATOR = new Parcelable.Creator<Recent>() {
        @Override
        public Recent createFromParcel(Parcel in) {
            return new Recent(in);
        }

        @Override
        public Recent[] newArray(int size) {
            return new Recent[size];
        }
    };
}
