package com.example.alessandro.mychatapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.alessandro.mychatapp.R;
import com.example.alessandro.mychatapp.activities.ChatActivity;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import io.paperdb.Paper;

import static android.support.constraint.Constraints.TAG;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link MyChatAppWidgetConfigureActivity MyChatAppWidgetConfigureActivity}
 */
public class MyChatAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, String list_user_id, String userName, String userThumb) {

        if (userThumb != null) {
            Uri imageUri = Uri.parse(userThumb);

            Intent chatIntent = new Intent(context, ChatActivity.class);
            chatIntent.putExtra("user_id", list_user_id);
            chatIntent.putExtra("chatUserName", userName);

            PendingIntent pendingIntent = PendingIntent.getActivity (context,0,chatIntent,0);

            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.my_chat_app_widget);
            views.setImageViewUri(R.id.widget_user_single_image, imageUri);
//            views.setTextViewText(R.id.widget_user_single_name, userName);
            views.setOnClickPendingIntent(R.id.layout_wrapper, pendingIntent);

            Picasso.get()
                    .load(userThumb)
                    .into(views, R.id.widget_user_single_image, new int[]{appWidgetId});

            Log.d(TAG, "updateAppWidget: appWidgetId: "+ appWidgetId + ", user_id: " + list_user_id + ", userName: " + userName + ", image: " + userThumb);


            // Instruct the widget manager to update the widget
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId,R.id.layout_wrapper);

            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        super.onReceive(context, intent);
//
//        if (Objects.equals(intent.getAction(), CLICK_ACTION)){
//
//            Intent chatIntent = new Intent(context, ChatActivity.class);
//            chatIntent.putExtra("user_id", list_user_id);
//            chatIntent.putExtra("chatUserName", userName);
//
//            PendingIntent pendingIntent = PendingIntent.getActivity (context,0,intent,0);
//        }
//
//    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            String list_user_id = MyChatAppWidgetConfigureActivity.loadTitlePref(context, appWidgetId);

            // Init Paper
            Paper.init(context);
            // Retrieve Paper Keys Values
            String userName = Paper.book(String.valueOf(appWidgetId)).read("user_name");
            String userThumb = Paper.book(String.valueOf(appWidgetId)).read("image_thumb");

            Log.d(TAG, "onUpdate: appWidgetId: " + appWidgetId + ", listUserId: " + list_user_id + ", userName: " + userName + ", userThumb: " + userThumb);

            updateAppWidget(context, appWidgetManager, appWidgetId, list_user_id, userName, userThumb);
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

