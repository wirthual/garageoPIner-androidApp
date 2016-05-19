package com.wirthual.garageopiner.utils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.wirthual.garageopiner.R;
import com.wirthual.garageopiner.communication.CommunicationService;

public class MyTriggerWidgetProvider extends AppWidgetProvider {

    final String BUTTON_CLICKED = "button_Clicked";

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_trigger_layout);
            views.setOnClickPendingIntent(R.id.widget_button, getPendingSelfIntent(context, BUTTON_CLICKED));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        if (BUTTON_CLICKED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            RemoteViews remoteViews;
            ComponentName watchWidget;

            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_trigger_layout);
            watchWidget = new ComponentName(context, MyTimeTriggerWidgetProvider.class);

            Intent serviceIntent = new Intent(context, CommunicationService.class);
            serviceIntent.setAction(CommunicationService.ACTION_TOGGLE_IN1);
            context.startService(serviceIntent);

            remoteViews.setCharSequence(R.id.widget_button, "setBackgroundResource", "0");
            appWidgetManager.updateAppWidget(watchWidget, remoteViews);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }
}