package com.zuescoder69.wordle.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.zuescoder69.wordle.R;
import com.zuescoder69.wordle.userData.SessionManager;

/**
 * Created by Gagan Kumar on 23/05/22.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        SessionManager sessionManager = new SessionManager(context);
        long i = sessionManager.getLongKey("secondsLeft")-500;
        Log.e("Custom", "onReceive: " + i);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "testNotif")
                .setContentTitle("title")
                .setContentText("broadcast")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setPriority(Notification.PRIORITY_HIGH);

        Thread thread = new Thread() {
            public void run() {
                Looper.prepare();

                new CountDownTimer(i, 1000) {

                    public void onTick(long millisUntilFinished) {
                        sessionManager.addLongKey("secondsLeft", millisUntilFinished);
                        Log.e("Custom", "onTick: " + millisUntilFinished);
                        if (millisUntilFinished / 1000 == 0) {
                            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(2000, mBuilder.build());
                        }
                    }

                    public void onFinish() {
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(2000, mBuilder.build());
                    }
                }.start();

                Looper.loop();
            }
        };
        thread.start();
    }
}