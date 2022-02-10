package cn.harpsichord.babyfound;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

public class CustomAPP extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
