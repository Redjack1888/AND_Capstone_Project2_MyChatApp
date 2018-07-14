package com.example.alessandro.mychatapp;

import android.app.Application;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

public class MyChatApp extends Application{

    DatabaseReference myChatAppDatabase;

    @Override
    public void onCreate() {
        super.onCreate();

        // Firebase Offline Capabilities

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Keep Firebase Database Root synced
        myChatAppDatabase = FirebaseDatabase.getInstance().getReference();
        myChatAppDatabase.keepSynced(true);

        // Picasso Offline Capabilities

        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttp3Downloader(this, Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

    }
}
