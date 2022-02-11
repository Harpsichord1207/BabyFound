package cn.harpsichord.babyfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        Information information = (Information) intent.getSerializableExtra("data");

        ImageView imageView = findViewById(R.id.detail_img);

        Picasso.with(this).load(information.imageURL).error(R.drawable.test_baby).into(imageView);

        TextView nameText = findViewById(R.id.detail_text_name);
        TextView detailText = findViewById(R.id.detail_text_detail);
        TextView dateText = findViewById(R.id.detail_text_date);
        TextView addressText = findViewById(R.id.detail_text_address);
        TextView posText = findViewById(R.id.detail_pos);

        int i = 0;

        for (String part: information.informationText.split("\\*#\\*")) {
            i += 1;
            String prefix;
            if (i == 1) {
                prefix = "姓名：";
                nameText.setText(prefix + part);
            } else if (i == 2) {
                prefix = "描述：";
                detailText.setText(prefix + part);
            } else if (i == 3) {
                prefix = "走失时间：";
                dateText.setText(prefix + part);
            } else {
                prefix = "";
                addressText.setText(prefix + part);
            }
        }

        posText.setText("坐标：" + information.longitude + "/" + information.latitude);

        Button back = findViewById(R.id.detail_btn);
        back.setOnClickListener(v -> {
            Intent backIntent = new Intent(this, MainActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(backIntent);
        });

    }
}