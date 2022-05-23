package com.zuescoder69.wordle.notification;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.zuescoder69.wordle.R;
import com.zuescoder69.wordle.userData.SessionManager;
import com.zuescoder69.wordle.utils.CommonValues;

/**
 * Created by Gagan Kumar on 23/05/22.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    NotificationManager mNotificationManager;
    NotificationCompat.Builder mBuilder;
    Context context = this;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Log.e("Custom", "onMessageReceived: " + message.getData() + "  " + " " + message.getMessageId());

        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Test";
            String description = "Notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            @SuppressLint("WrongConstant") NotificationChannel channel = new NotificationChannel("testNotif", name, importance);


            channel.setDescription(description);
            channel.setShowBadge(true);

            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            mNotificationManager.createNotificationChannel(channel);
        }

        RemoteViews remoteWidget = new RemoteViews(context.getPackageName(), R.layout.custom_notification_layout);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            remoteWidget.setChronometerCountDown(R.id.noteChronometer, true);
        }
        remoteWidget.setChronometer(R.id.noteChronometer, SystemClock.elapsedRealtime() + CommonValues.notificationTime, null, true);
        remoteWidget.setTextViewText(R.id.note_title, "Firebase");

        mBuilder = new NotificationCompat.Builder(context, "testNotif")
                .setContentTitle(message.getData().get("title"))
                .setContentText(message.getData().get("body"))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .setCustomContentView(remoteWidget)
                .setCustomBigContentView(remoteWidget)
                .setTimeoutAfter(CommonValues.notificationTime);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mBuilder.setChannelId("testNotif");
        }

        mNotificationManager.notify(2000, mBuilder.build());

        SessionManager sessionManager = new SessionManager(this);

        Thread thread1 = new Thread() {
            public void run() {
                Looper.prepare();

                new CountDownTimer(CommonValues.notificationTime, 1000) {

                    public void onTick(long millisUntilFinished) {
                        sessionManager.addLongKey("secondsLeft", millisUntilFinished);
                    }

                    public void onFinish() {
                        NotificationCompat.Builder mBuilder1 = new NotificationCompat.Builder(context, "testNotif")
                                .setContentTitle("title")
                                .setContentText("broadcast")
                                .setSmallIcon(R.drawable.ic_launcher)
                                .setAutoCancel(true)
                                .setOnlyAlertOnce(true)
                                .setPriority(Notification.PRIORITY_HIGH);
                        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                        mNotificationManager.notify(2000, mBuilder1.build());
                    }
                }.start();

                Looper.loop();
            }
        };
        thread1.start();

        /* Intent MyIntent = new Intent(getApplicationContext(), MyBroadcastReceiver.class);
                    PendingIntent MyPendIntent = PendingIntent.getBroadcast(getApplicationContext(), 2000, MyIntent, PendingIntent.FLAG_ONE_SHOT);
                    AlarmManager MyAlarm = (AlarmManager) getSystemService(ALARM_SERVICE);
                    MyAlarm.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+20000, MyPendIntent);*/


    }

    @Override
    public void onNewToken(@NonNull String refreshedToken) {
        super.onNewToken(refreshedToken);
        SessionManager sessionManager = new SessionManager(this);
        sessionManager.addStringKey("token", refreshedToken);
        Log.d("DEMON", "token " + refreshedToken);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("Custom", "onTaskRemoved: ");
        super.onTaskRemoved(rootIntent);

    }
}