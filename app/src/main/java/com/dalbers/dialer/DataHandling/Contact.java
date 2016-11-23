package com.dalbers.dialer.DataHandling;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by davidalbers on 10/26/16.
 */
public class Contact implements Parcelable {
    public static int INVALID_ID = -1;
    public Contact(String name, String lookup, String number, long id) {
        this.name = name;
        this.lookup = lookup;
        this.number = number;
        this.id = id;
    }


    public long getId() {
        return id;
    }

    private long id;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    private String name;
    private String number;

    public String getLookup() {
        return lookup;
    }

    private String lookup;

    protected Contact(Parcel in) {
        name = in.readString();
        number = in.readString();
        lookup = in.readString();
        id = in.readLong();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(number);
        dest.writeString(lookup);
        dest.writeLong(id);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
        @Override
        public Contact createFromParcel(Parcel in) {
            return new Contact(in);
        }

        @Override
        public Contact[] newArray(int size) {
            return new Contact[size];
        }
    };
}
