package com.dalbers.dialer.DataHandling;
import java.util.Date;

/**
 * Created by davidalbers on 10/26/16.
 */
public class Sms {
    private String address;

    private String body;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    private Date date;

    /**
     * Did this phone send the sendMessage or was it received (incoming)
     */
    private boolean outgoing;

    public Sms(String address, String body, Date date, boolean outgoing, String lookup) {
        this.address = address;
        this.body = body;
        this.date = date;
        this.outgoing = outgoing;
        this.lookup = lookup;
    }
    public boolean isOutgoing() {
        return outgoing;
    }

    public void setOutgoing(boolean outgoing) {
        this.outgoing = outgoing;
    }
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLookup() {
        return lookup;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    private String lookup;
}
