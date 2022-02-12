package cn.harpsichord.babyfound;

import android.app.Application;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;

import java.util.HashSet;
import java.util.Set;

public class CustomAPP extends Application {

    public Set<Integer> globalInformationSet = new HashSet<>();
    public double longitude = 0;
    public double latitude = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.initialize(this);
        locate();
    }

    public void locate() {
        LocationClient mLocationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setIsNeedAddress(true);
        option.setCoorType("bd09ll"); // 设置坐标类型
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                latitude = bdLocation.getLatitude();
                longitude = bdLocation.getLongitude();
            }
        });
        mLocationClient.start();
    }
}
