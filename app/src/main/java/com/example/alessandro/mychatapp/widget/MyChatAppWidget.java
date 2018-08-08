package com.example.alessandro.mychatapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.util.Log;
import android.widget.RemoteViews;

import com.example.alessandro.mychatapp.R;
import com.example.alessandro.mychatapp.activities.ChatActivity;
import com.squareup.picasso.Picasso;

import static android.support.constraint.Constraints.TAG;


/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MyChatAppWidgetConfigureActivity MyChatAppWidgetConfigureActivity}
 */
public class MyChatAppWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
            String titlePrefix = MyChatAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);
            String namePrefix = MyChatAppWidgetConfigureActivity.loadNamePref(context, appWidgetId);
            String IdPrefix = MyChatAppWidgetConfigureActivity.loadUserIdPref(context, appWidgetId);
            updateAppWidget(context, appWidgetManager, appWidgetId, titlePrefix, namePrefix, IdPrefix);

        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            MyChatAppWidgetConfigureActivity.deleteTitlePref(context, appWidgetId);
//            MyChatAppWidgetConfigureActivity.deleteNamePref(context, appWidgetId);

        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("com.example.alessandro.mychatapp", ".widget.MyChatAppBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("com.example.alessandro.mychatapp", ".widget.MyChatAppBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);

    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String titlePrefix, String namePrefix, String IdPrefix) {

//        String widgetNameText = MyChatAppWidgetConfigureActivity.loadNamePref(context, appWidgetId);

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_chat_app_widget);
        views.setImageViewUri(R.id.widget_user_single_image, Uri.parse(titlePrefix));
//        views.setTextViewText(R.id.widget_user_single_name, "redjak");


        Picasso.get()
                .load(titlePrefix)
                .into(views,R.id.widget_user_single_image, new int[]{appWidgetId});

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("user_id", IdPrefix);
        intent.putExtra("chatUserName",namePrefix);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        views.setOnClickPendingIntent(R.id.widget_user_single_image, pendingIntent);
        Log.d(TAG, "updateAppWidget: userId: "+ IdPrefix  + ",ChatUserName: " + namePrefix);


        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

}

