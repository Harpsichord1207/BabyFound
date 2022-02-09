package cn.harpsichord.babyfound;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<Information> informationArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        for (int i=0; i<25; i++) {
            Information information = new Information();
            information.imageURL = "test_image_url";
            information.informationText = "Text" + i;
            informationArrayList.add(information);
        }

        final ListView listview = (ListView) findViewById(R.id.publish_information_list);
        listview.setAdapter(
                new InformationAdapter(this, informationArrayList)
        );


    }
}

class InformationAdapter extends ArrayAdapter<Information> {

    Context context;
    List<Information> data;

    public InformationAdapter(Context context, List<Information> data) {
        super(context, -1, data);
        this.context = context;
        this.data = data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        // TODO 使用ViewHolder优化性能
        View rowView = inflater.inflate(R.layout.information_item, parent, false);
        ImageView imageView = rowView.findViewById(R.id.baby_image);
        TextView textView = rowView.findViewById(R.id.baby_text);

        // imageView.setImageResource(R.drawable.test_baby);
        // 使用Picasso通过URL设置图片
        Picasso.with(context).load("https://www-cdn.cigdata.cn/gkbf/online_test.jpg").error(R.drawable.test_baby).into(imageView);
        textView.setText(data.get(position).informationText);
        return rowView;
    }

}