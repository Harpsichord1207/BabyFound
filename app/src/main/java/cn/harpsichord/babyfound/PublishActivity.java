package cn.harpsichord.babyfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishActivity extends AppCompatActivity {

    public ImageView imageView;
    public TextView locTextView;
    public EditText editText;

    public LocationClient mLocationClient = null;
    public MapView mapView = null;
    public BaiduMap baiduMap = null;

    public byte[] image = null;

    private final MyLocationListener myListener = new MyLocationListener();

    public final String logTag = "PublishActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        editText = findViewById(R.id.input_text);
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
            Log.i(logTag, "Start to submit!");
            try {
                uploadInformation();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });

        Log.w(logTag, "Here1");

        mLocationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(1000);
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();

        Log.w(logTag, "Here2");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!(requestCode == 100 && resultCode == RESULT_OK)) {
            return;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(data.getData()));
            bitmap = Bitmap.createScaledBitmap(bitmap,  400 ,400, true);
            imageView.setImageBitmap(bitmap);
            image = toByteArray(this.getContentResolver().openInputStream(data.getData()));
        } catch (Exception e){
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
        mLocationClient.start();
        baiduMap.setMyLocationEnabled(false);
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }

    public void uploadInformation() {
        Log.w(logTag, "Start to upload information!");
        MyLocationData locationData = baiduMap.getLocationData();
        double longitude = locationData.longitude;
        double latitude = locationData.latitude;
        String text = editText.getText().toString();
        Log.i("Upload", "L: " + longitude);

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM) // What？
                .addFormDataPart("details", text)
                .addFormDataPart("longitude", String.valueOf(longitude))
                .addFormDataPart("latitude", String.valueOf(latitude))
                .addFormDataPart("FILES", "xx.jpg",  RequestBody.create(image))
                .build();

        Request request = new Request.Builder()
                .url("http://52.81.88.46:8000/upload/")
                .post(requestBody)
                .build();

        OkHttpClient client = new OkHttpClient();
        Log.w(logTag, "Start to execute...");
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String respText = response.body().string();

                Information information = new Information();
                information.informationText = text;
                information.longitude = longitude;
                information.latitude = latitude;

                JsonObject jsonBody = JsonParser.parseString(respText).getAsJsonObject();
                for (JsonElement ele: jsonBody.getAsJsonArray("urls")) {
                    information.imageURL = ele.getAsJsonObject().get("url").getAsString();
                    break;
                }
                customToast("发布成功！");
                Intent intent = new Intent(PublishActivity.this, DetailActivity.class);
                intent.putExtra("data", information);
                startActivity(intent);
            } catch (Exception e) {
                customToast(e+"");
                e.printStackTrace();
            }
        }).start();

    }

    private void customToast(String content) {
        // TODO: 默认参数Toast.LENGTH_SHORT
        runOnUiThread(() -> Toast.makeText(PublishActivity.this, content, Toast.LENGTH_SHORT).show());
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
        return output.toByteArray();
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