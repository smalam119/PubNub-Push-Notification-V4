package com.smalam.pseudozero.pbpush;

/**
 * Created by SAYED on 11/18/2016.
 */

public class AppEvent {
    public String msg;

    public AppEvent(String type,String msg) {
        this.msg = msg;
    }

    public boolean is(String msg){
        return this.msg.equals(msg);
    }
}
