package com.ne.app.newsapp;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by ashrafiqubal on 22/01/16.
 */
public class KillNotificationService extends Service {
    private static final int NOTIFICATION_ID_CUSTOM_SIMPLE = 7779777;
    public class KillBinder extends Binder {
        public final Service service;
        public KillBinder(Service service) {
            this.service = service;
        }

    }
    private NotificationManager mNM;
    private final IBinder mBinder = new KillBinder(this);
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }
    @Override
    public void onCreate() {
        Log.d("KillNotification://","onCreate:// ");
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(MainActivity.isNotified){
            mNM.cancel(NOTIFICATION_ID_CUSTOM_SIMPLE);
            MainActivity.isNotified=false;
        }
    }
}