package cn.harpsichord.babyfound;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class PublishActivity extends AppCompatActivity {

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_publish);

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
            bitmap = Bitmap.createScaledBitmap(bitmap,  600 ,600, true);
            imageView.setImageBitmap(bitmap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}