package com.example.alessandro.mychatapp.models;

import android.app.LauncherActivity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.alessandro.mychatapp.R;

import java.util.ArrayList;

public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private ArrayList<Users> listItemList = new ArrayList<>();
    private Context context = null;
    private int appWidgetId;

    public ListProvider(Context context, Intent intent) {
        this.context = context;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);

        populateListItem();
    }

    private void populateListItem() {

            Users listItem = new Users();
            listItem.name = "Heading";
            listItem.status = "Status"
                    + " This is the content of the app widget listview.Nice content though";
            listItemList.add(listItem);


    }

    @Override
    public int getCount() {
        return listItemList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /*
     *Similar to getView of Adapter where instead of View
     *we return RemoteViews
     *
     */
    @Override
    public RemoteViews getViewAt(int position) {
        final RemoteViews remoteView = new RemoteViews(
                context.getPackageName(), R.layout.user_single_chat_item_fragment);
        Users listItem = listItemList.get(position);
        remoteView.setTextViewText(R.id.user_single_name, listItem.name);
        remoteView.setTextViewText(R.id.user_single_status, listItem.status);

        return remoteView;
    }


    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
    }

}