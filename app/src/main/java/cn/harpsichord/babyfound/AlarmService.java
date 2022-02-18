package cn.harpsichord.babyfound;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AlarmService extends Service {
    private static final int interval = 15 * 1000;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                CustomAPP app = (CustomAPP) getApplication();
                if (app.globalInformationSet.isEmpty()) {
                    // 第一次启动这个还是空的 也不用提醒
                    return;
                }

                CustomNotification.init(AlarmService.this);

                Request request = new Request.Builder()
                        .url("http://52.81.88.46:8000/get_all/")
                        .get()
                        .build();
                OkHttpClient client = new OkHttpClient();
                try {
                    Response response = client.newCall(request).execute();
                    String body = response.body().string();
                    JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
                    for (JsonElement ele: jsonBody.getAsJsonArray("data")) {
                        int id = ele.getAsJsonObject().get("id").getAsInt();
                        String imageURL = ele.getAsJsonObject().get("img_url").getAsString();
                        String text = ele.getAsJsonObject().get("img_details").getAsString();
                        double longitude = ele.getAsJsonObject().get("img_longitude").getAsDouble();
                        double latitude = ele.getAsJsonObject().get("img_latitude").getAsDouble();

                        Information information = new Information();
                        information.id = id;
                        information.detail = new Information.Detail(text);
                        information.imageURL = imageURL;
                        information.longitude = longitude;
                        information.latitude = latitude;

                        if (!app.globalInformationSet.contains(id)) {
                            app.globalInformationSet.add(id);

                            // 在一定范围内才通知
                            if ((app.longitude * app.latitude == 0) && (Math.abs(longitude - app.longitude) < 0.1 && Math.abs(latitude - app.latitude) < 0.1)) {
                                CustomNotification.send(information, AlarmService.this);
                            }

                            break;
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
