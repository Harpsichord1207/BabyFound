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


        TextView text = findViewById(R.id.detail_text);
        text.setText(information.informationText);

        TextView pos = findViewById(R.id.detail_pos);
        pos.setText("坐标：" + information.longitude + "/" + information.latitude);

        Button back = findViewById(R.id.detail_btn);
        back.setOnClickListener(v -> {
            Intent backIntent = new Intent(this, MainActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(backIntent);
        });

    }
}