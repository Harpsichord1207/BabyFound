package cn.harpsichord.babyfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    ArrayList<Information> informationArrayList = new ArrayList<>();
    InformationAdapter informationAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listview = findViewById(R.id.publish_information_list);
        listview.setOnItemClickListener((parent, view, position, id) -> {
            Information selectedItem = (Information) parent.getItemAtPosition(position);
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra("data", selectedItem);
            startActivity(intent);
        });
        informationAdapter = new InformationAdapter(this, informationArrayList);
        listview.setAdapter(informationAdapter);

        Button publishBtn = findViewById(R.id.publish_information_btn);
        publishBtn.setOnClickListener(v -> {
            Intent intent = new Intent(this, PublishActivity.class);
            startActivity(intent);
        });

        getAllInformation();

        Button refreshBtn = findViewById(R.id.refresh_information);
        refreshBtn.setOnClickListener(v -> {
            getAllInformation();
        });

//        CustomNotification.init(this);
//        Information testI = new Information();
//        testI.informationText = "test note!";
//        CustomNotification.send(testI, this);

        Intent bIntent = new Intent(this, AlarmService.class);
        startService(bIntent);
    }

    public void getAllInformation() {
        Request request = new Request.Builder()
                .url("http://52.81.88.46:8000/get_all/")
                .get()
                .build();
        OkHttpClient client = new OkHttpClient();
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                String body = response.body().string();
                informationArrayList.clear();
                JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();
                for (JsonElement ele: jsonBody.getAsJsonArray("data")) {
                    int id = ele.getAsJsonObject().get("id").getAsInt();
                    String imageURL = ele.getAsJsonObject().get("img_url").getAsString();
                    String text = ele.getAsJsonObject().get("img_details").getAsString();
                    double longitude = ele.getAsJsonObject().get("img_longitude").getAsDouble();
                    double latitude = ele.getAsJsonObject().get("img_latitude").getAsDouble();

                    Information information = new Information();
                    information.id = id;
                    information.informationText = text;
                    information.imageURL = imageURL;
                    information.longitude = longitude;
                    information.latitude = latitude;

                    informationArrayList.add(information);
                }

                runOnUiThread(() -> {
                    CustomAPP app = (CustomAPP) getApplication();
                    for (Information information: informationArrayList) {
                        app.globalInformationSet.add(information.id);
                    }
                    informationAdapter.notifyDataSetChanged();
                    Toast.makeText(
                            MainActivity.this,
                            "刷新成功，获取到" + informationArrayList.size() + "/" + app.globalInformationSet.size() + "条信息",
                            Toast.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void customToast(String content) {
        // TODO: 默认参数Toast.LENGTH_SHORT
        runOnUiThread(() -> Toast.makeText(MainActivity.this, content, Toast.LENGTH_SHORT).show());
    }

}

class InformationAdapter extends BaseAdapter {

    Context context;
    List<Information> data;
    LayoutInflater inflater;

    public InformationAdapter(Context context, List<Information> data) {
        this.context = context;
        this.data = data;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 原来这个ViewHolder是要自己定义的？
    private static class ViewHolder {
        ImageView imageView;
        TextView textView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.information_item, parent, false);
            viewHolder.imageView = convertView.findViewById(R.id.baby_image);
            viewHolder.textView = convertView.findViewById(R.id.baby_text);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // imageView.setImageResource(R.drawable.test_baby);
        // 使用Picasso通过URL设置图片
        Information information = data.get(position);
        Picasso.with(context).load(information.imageURL).error(R.drawable.test_baby).into(viewHolder.imageView);
        String displayText = "";
        int i = 0;
        for (String part: information.informationText.split("\\*#\\*")) {
            i += 1;
            String prefix;
            if (i == 1) {
                prefix = "姓名：";
            } else if (i == 2) {
                prefix = "描述：";
            } else if (i == 3) {
                prefix = "走失时间：";
            } else {
                prefix = "";
            }
            String thisLine = prefix + part;
            if (thisLine.length() > 15) {
                thisLine = thisLine.substring(0, 12) + "...";
            }
            displayText += thisLine + "\n";
        }
        viewHolder.textView.setText(displayText);
        return convertView;
    }

}
