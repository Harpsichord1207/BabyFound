package cn.harpsichord.babyfound;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationClickReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent newIntent = new Intent(context, DetailActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        newIntent.putExtra("data", intent.getSerializableExtra("data"));
        context.startActivity(newIntent);
    }
}