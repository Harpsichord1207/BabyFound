package cn.harpsichord.babyfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PublishActivity extends AppCompatActivity {

    public ImageView imageView;
    public TextView locTextView;

    public LocationClient mLocationClient = null;
    public MapView mapView = null;
    public BaiduMap baiduMap = null;
    private final MyLocationListener myListener = new MyLocationListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        locTextView = findViewById(R.id.loc_text_view);
        myListener.setFields(baiduMap, locTextView);

        Button selectImageBtn = findViewById(R.id.select_image_btn);
        selectImageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            // TODO: https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
            startActivityForResult(intent, 100);
        });

        imageView = findViewById(R.id.selected_image);

        Button submitBtn = findViewById(R.id.submit_information);
        submitBtn.setOnClickListener(v -> {
            Toast.makeText(PublishActivity.this, "Wait Backend!", Toast.LENGTH_SHORT).show();
        });

        mLocationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!(requestCode == 100 && resultCode == RESULT_OK)) {
            return;
        }

        try {
            InputStream inputStream = this.getContentResolver().openInputStream(data.getData());
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            bitmap = Bitmap.createScaledBitmap(bitmap,  400 ,400, true);
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

}

class MyLocationListener extends BDAbstractLocationListener {

    private BaiduMap baiduMap;
    private TextView textView;

    @Override
    public void onReceiveLocation(BDLocation location){
        baiduMap.setMyLocationData(
                new MyLocationData.Builder().
                        accuracy(location.getRadius()).
                        direction(location.getDirection()).
                        latitude(location.getLatitude()).
                        longitude(location.getLongitude()).build()
        );

        textView.setText("当前位置：" + location.getAddrStr());

    }

    public void setFields(BaiduMap baiduMap, TextView textView) {
        this.baiduMap = baiduMap;
        this.textView = textView;
    }


}