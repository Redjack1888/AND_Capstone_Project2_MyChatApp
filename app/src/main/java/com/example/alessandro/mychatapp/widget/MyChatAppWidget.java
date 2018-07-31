package com.example.alessandro.mychatapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import android.widget.RemoteViews;

import com.example.alessandro.mychatapp.R;
import com.example.alessandro.mychatapp.activities.MainActivity;
import com.squareup.picasso.Picasso;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MyChatAppWidgetConfigureActivity MyChatAppWidgetConfigureActivity}
 */
public class MyChatAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        String widgetText = MyChatAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_chat_app_widget);
        views.setImageViewUri(R.id.widget_user_single_image, Uri.parse(widgetText));

        Picasso.get()
                .load(widgetText)
                .into(views,R.id.widget_user_single_image, new int[]{appWidgetId});

        Intent intent = new Intent(context, MainActivity.class);
//        intent.putExtra("user_id", listUserId);
//        intent.putExtra("chatUserName",chat_user_name);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.widget_user_single_image, pendingIntent);



        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            MyChatAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

