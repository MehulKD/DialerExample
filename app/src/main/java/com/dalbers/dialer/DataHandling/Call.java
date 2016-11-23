package com.dalbers.dialer.DataHandling;

import java.util.Date;
/**
 * Created by davidalbers on 10/26/16.
 */
public class Call {
    public Call(String number, Date date, String lookup) {
        this.number = number;
        this.date = date;
        this.lookup = lookup;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private String number;
    private Date date;

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    private String lookup;
}
