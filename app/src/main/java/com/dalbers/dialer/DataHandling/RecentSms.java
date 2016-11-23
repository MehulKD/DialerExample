package com.dalbers.dialer.DataHandling;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by davidalbers on 10/29/16.
 */
public class RecentSms extends Recent implements Parcelable {
   public RecentSms(String contactNameOrNumber,  Date date, RecentTypes type, String body, String lookup, long contactId) {
      super(contactNameOrNumber,date,type,lookup,contactId);
      this.body = body;
   }
   public String getBody() {
      return body;
   }

   private String body;

   protected RecentSms(Parcel in) {
      super(in);
      body = in.readString();
   }

   @Override
   public int describeContents() {
      return 0;
   }

   @Override
   public void writeToParcel(Parcel dest, int flags) {
       super.writeToParcel(dest,flags);
      dest.writeString(body);
   }

   @SuppressWarnings("unused")
   public static final Parcelable.Creator<RecentSms> CREATOR = new Parcelable.Creator<RecentSms>() {
      @Override
      public RecentSms createFromParcel(Parcel in) {
         return new RecentSms(in);
      }

      @Override
      public RecentSms[] newArray(int size) {
         return new RecentSms[size];
      }
   };
}
