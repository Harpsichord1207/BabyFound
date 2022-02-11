package cn.harpsichord.babyfound;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomAPP extends Application {

    public Set<Integer> globalInformationSet = new HashSet<>();

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
    }
}
