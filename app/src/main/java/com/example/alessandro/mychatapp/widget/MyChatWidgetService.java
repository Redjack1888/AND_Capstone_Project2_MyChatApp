package com.example.alessandro.mychatapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.RemoteViewsService;

import com.example.alessandro.mychatapp.models.ListProvider;

public class MyChatWidgetService extends RemoteViewsService {
    /*
     * So pretty simple just defining the Adapter of the listview
     * here Adapter is ListProvider
     * */

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int appWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        return (new ListProvider(this.getApplicationContext(), intent));
    }

}