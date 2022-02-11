package cn.harpsichord.babyfound;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

public class AlarmService extends Service {
    private static final int interval = 15 * 1000;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomNotification.init(AlarmService.this);
                Information information = new Information();
                information.informationText = System.currentTimeMillis() + "";
                CustomNotification.send(information, AlarmService.this);
            }
        }).start();

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long atTime = SystemClock.elapsedRealtime() + interval;
        Intent newIntent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 0, newIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT
        );

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, atTime, pendingIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
