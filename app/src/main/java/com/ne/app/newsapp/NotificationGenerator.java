package com.ne.app.newsapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by ashrafiqubal on 21/01/16.
 */
public class NotificationGenerator {
    public static final String NOTIFY_PREVIOUS = "com.ne.app.newsapp.previous";
    public static final String NOTIFY_DELETE = "com.ne.app.newsapp.delete";
    public static final String NOTIFY_PAUSE = "com.ne.app.newsapp.pause";
    public static final String NOTIFY_PLAY = "com.ne.app.newsapp.play";
    public static final String NOTIFY_NEXT = "com.ne.app.newsapp.next";
    private static final int NOTIFICATION_ID_CUSTOM_SIMPLE = 7779777;
    static String appName = "Audibyte Running";
    static String openApp = "Tap to open";
    static Notification notification;
    static NotificationManager nm;
    public static void customSimpleNotification(Context context) {
        RemoteViews simpleView = new RemoteViews(context.getPackageName(), R.layout.custom_notification);

        notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Custom Big View").build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.contentView = simpleView;
        notification.contentView.setTextViewText(R.id.textAppName, appName);
        notification.contentView.setTextViewText(R.id.textOpenApp, openApp);
        if(android.os.Build.VERSION.SDK_INT> Build.VERSION_CODES.JELLY_BEAN){
            notification.priority=Notification.PRIORITY_MAX;
        }
        if(MainActivity.mediaPlayer.isPlaying()){
            notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);
        }else {
            notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
            notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
        }
        Intent notifyIntent = new Intent(MainActivity.parent, MainActivity.class);
        notifyIntent.setAction("android.intent.action.MAIN");
        notifyIntent.addCategory("android.intent.category.LAUNCHER");
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.parent , 0 , notifyIntent , PendingIntent.FLAG_UPDATE_CURRENT);
        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        //startForeground(NOTIFICATION_ID, notification);
        notification.contentIntent=pendingIntent;
        setListeners(simpleView, context);
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID_CUSTOM_SIMPLE, notification);
    }
    public static void cancelNotification(){
        nm.cancel(NOTIFICATION_ID_CUSTOM_SIMPLE);
    }
    private static void setListeners(RemoteViews view, Context context) {
        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent delete = new Intent(NOTIFY_DELETE);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);
        Intent play = new Intent(NOTIFY_PLAY);

        PendingIntent pPrevious = PendingIntent.getBroadcast(context, 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pDelete = PendingIntent.getBroadcast(context, 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPause = PendingIntent.getBroadcast(context, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        PendingIntent pNext = PendingIntent.getBroadcast(context, 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPlay = PendingIntent.getBroadcast(context, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPlay, pPlay);
    }
}
