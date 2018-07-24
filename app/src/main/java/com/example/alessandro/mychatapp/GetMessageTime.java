package com.example.alessandro.mychatapp;

import android.app.Application;
import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;

// “Time Since/Ago” Library for Android/Java based on the Google I/O 2012 App
// source short ulr: http://bit.ly/2LxRm8F
public class GetMessageTime extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getMessageTime(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "JUST NOW";
        } else if (diff < 90 * MINUTE_MILLIS) {
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String messageTime = sfd.format(new Date(time)).toString();
            messageTime = messageTime.substring(11, messageTime.length());
            return messageTime;
        } else if (diff < 24 * HOUR_MILLIS) {
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
            String messageTime = sfd.format(new Date(time)).toString();
            messageTime = messageTime.substring(11, messageTime.length());
            return messageTime;
        } else if (diff < 48 * HOUR_MILLIS) {
            return "YESTERDAY";
        } else {
            SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String messageTime = sfd.format(new Date(time)).toString();
            messageTime = messageTime.substring(0, 10);
            return messageTime;
        }
    }

}