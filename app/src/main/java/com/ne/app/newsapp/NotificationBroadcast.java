package com.ne.app.newsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * Created by ashrafiqubal on 21/01/16.
 */
public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(NotificationGenerator.NOTIFY_PLAY)) {
            //Toast.makeText(context, "NOTIFY_PLAY", Toast.LENGTH_LONG).show();
            MainActivity.getInstance().playPause();
            MainActivity.vibrate(50);
            MainActivity.updateNotification();
        } else if (intent.getAction().equals(NotificationGenerator.NOTIFY_PAUSE)) {
           // Toast.makeText(context, "NOTIFY_PAUSE", Toast.LENGTH_LONG).show();
            MainActivity.getInstance().playPause();
            MainActivity.vibrate(50);
            MainActivity.updateNotification();
        } else if (intent.getAction().equals(NotificationGenerator.NOTIFY_NEXT)) {
           // Toast.makeText(context, "NOTIFY_NEXT", Toast.LENGTH_LONG).show();
            MainActivity.getInstance().playNextNewsFunction();
            MainActivity.vibrate(50);
            MainActivity.updateNotification();
        } else if (intent.getAction().equals(NotificationGenerator.NOTIFY_DELETE)) {
            //Toast.makeText(context, "NOTIFY_EXIT", Toast.LENGTH_LONG).show();
            MainActivity.vibrate(50);
            MainActivity.getInstance().finish();
        }else if (intent.getAction().equals(NotificationGenerator.NOTIFY_PREVIOUS)) {
           // Toast.makeText(context, "NOTIFY_PREVIOUS", Toast.LENGTH_LONG).show();
            MainActivity.getInstance().playPrevNewsFunction();
            MainActivity.updateNotification();
            MainActivity.vibrate(50);
        }
    }
}
