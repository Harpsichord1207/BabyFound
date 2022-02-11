package cn.harpsichord.babyfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
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
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PublishActivity extends AppCompatActivity {

    public ImageView imageView;

    public LocationClient mLocationClient = null;
    public MapView mapView = null;
    public BaiduMap baiduMap = null;

    public EditText nameEditText;
    public EditText detailEditText;
    public EditText timeEditText;
    public EditText contactEditText;
    public EditText locEditText;

    public byte[] image = null;

    private final MyLocationListener myListener = new MyLocationListener();

    public final String logTag = "PublishActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

        nameEditText = findViewById(R.id.input_text_name);
        detailEditText = findViewById(R.id.input_text_detail);
        timeEditText = findViewById(R.id.input_text_time);
        SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss", Locale.CHINESE);
        timeEditText.setText(df.format(new Date()));
        contactEditText = findViewById(R.id.input_text_contact);
        locEditText = findViewById(R.id.input_text_loc);

        mapView = findViewById(R.id.bmapView);
        baiduMap = mapView.getMap();
        baiduMap.setMyLocationEnabled(true);
        baiduMap.setOnMapLongClickListener(latLng -> {
            double latitude = latLng.latitude;
            double longitude = latLng.longitude;
            baiduMap.setMyLocationData(
                    new MyLocationData.Builder().
                            latitude(latitude).
                            longitude(longitude).build()
            );
            new Thread(() -> {latLng2Address(latLng, locEditText);}).start();

        });
        myListener.setFields(baiduMap, locEditText);

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
                customToast("上传失败：" + e);
                e.printStackTrace();
            }

        });

        Button resetBtn = findViewById(R.id.reset_location);
        resetBtn.setOnClickListener(v -> {
            locate();
        });
        locate();
    }

    public void locate() {
        mLocationClient = new LocationClient(this);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setIsNeedAddress(true);
        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);
        mLocationClient.start();
    }

    public void latLng2Address(LatLng latLng, EditText editText) {
        final String[] address = {"未知地点"};
        final boolean[] getRes = {false};
        GeoCoder geoCoder = GeoCoder.newInstance();
        geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

            }

            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                if (reverseGeoCodeResult == null || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
                    getRes[0] = true;
                } else {
                    address[0] = reverseGeoCodeResult.getAddress();
                    getRes[0] = true;
                }

            }
        });
        geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latLng).newVersion(1).radius(100));

        int i = 0;
        while (i<=5) {
            if (getRes[0]) {
                break;
            }
            i += 1;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignored) {
            }

        }
        runOnUiThread(() -> {editText.setText("走失位置：" + address[0]);});
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
        mLocationClient.stop();
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

        String name = nameEditText.getText().toString();
        String detail = detailEditText.getText().toString();
        String date = timeEditText.getText().toString();
        String address = locEditText.getText().toString();
        String contact = contactEditText.getText().toString();

        if (name.trim().length() == 0 || detail.trim().length() == 0 || date.trim().length() == 0 || address.trim().length() == 0 || contact.trim().length() == 0) {
            customToast("存在未填写的字段！");
            return;
        }

        String text = name + "*#*" + detail + "*#*" + date + "*#*" + contact + "*#*" + address;

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
                information.detail = new Information.Detail(text);
                information.longitude = longitude;
                information.latitude = latitude;

                JsonObject jsonBody = JsonParser.parseString(respText).getAsJsonObject();

                int code = jsonBody.get("code").getAsInt();

                if (code != 200) {
                    customToast("上传失败，Code " + code);
                    return;
                }

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
    private EditText editText;

    @Override
    public void onReceiveLocation(BDLocation location){
        baiduMap.setMyLocationData(
                new MyLocationData.Builder().
                        accuracy(location.getRadius()).
                        direction(location.getDirection()).
                        latitude(location.getLatitude()).
                        longitude(location.getLongitude()).build()
        );

        editText.setText("走失位置：" + location.getAddrStr());

    }

    public void setFields(BaiduMap baiduMap, EditText textView) {
        this.baiduMap = baiduMap;
        this.editText = textView;
    }


}
