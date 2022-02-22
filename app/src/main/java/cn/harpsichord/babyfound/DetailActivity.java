package cn.harpsichord.babyfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        Vibrator vibrator = (Vibrator) this.getSystemService(VIBRATOR_SERVICE);

        Intent intent = getIntent();
        Information information = (Information) intent.getSerializableExtra("data");

        ImageView imageView = findViewById(R.id.detail_img);

        Picasso.with(this).load(information.imageURL).error(R.drawable.test_baby).into(imageView);

        TextView nameText = findViewById(R.id.detail_text_name);
        TextView detailText = findViewById(R.id.detail_text_detail);
        TextView dateText = findViewById(R.id.detail_text_date);
        TextView contactText = findViewById(R.id.detail_text_contact);
        contactText.setOnLongClickListener(v -> {
            String contactString = contactText.getText().toString();
            contactString = contactString.replaceFirst("联系方式：", "").trim();
            if (contactString.length() == 0) {
                return false;
            }
            // clipboardManager.setText(contactString);
            clipboardManager.setPrimaryClip(ClipData.newPlainText(null, contactString));
            vibrator.vibrate(VibrationEffect.createOneShot(50, 192));
            Toast.makeText(DetailActivity.this, "已复制: " + contactString, Toast.LENGTH_SHORT).show();
            return false;
        });
        TextView addressText = findViewById(R.id.detail_text_address);
        TextView posText = findViewById(R.id.detail_pos);

        nameText.setText(information.getName());
        detailText.setText(information.getDetail());
        dateText.setText(information.getDate());
        contactText.setText(information.getContact());
        addressText.setText(information.getAddress());

        posText.setText("坐标：" + information.longitude + "/" + information.latitude);

        Button back = findViewById(R.id.detail_btn);
        back.setOnClickListener(v -> {
            Intent backIntent = new Intent(this, MainActivity.class);
            backIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(backIntent);
        });

    }
}