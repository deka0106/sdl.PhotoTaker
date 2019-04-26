package jp.ac.titech.itpro.sdl.phototaker;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final static String FILEPROVIDER = "jp.ac.titech.itpro.sdl.fileprovider";
    private final static int REQ_PHOTO = 1234;
    private String photoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button photoButton = findViewById(R.id.photo_button);
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                PackageManager manager = getPackageManager();
                List activities = manager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
                if (!activities.isEmpty()) {
                    File photoFile;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, R.string.toast_failed_create_file, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (photoFile != null) {
                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                FILEPROVIDER,
                                photoFile);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(intent, REQ_PHOTO);
                    }
                } else {
                    Toast.makeText(MainActivity.this, R.string.toast_no_activities, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(new Date());
        String imageFileName = "PHOTO_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        photoPath = image.getAbsolutePath();
        return image;
    }

    private void showPhoto() {
        if (photoPath == null) {
            return;
        }
        ImageView photoView = findViewById(R.id.photo_view);
        int viewW = photoView.getWidth();
        int viewH = photoView.getHeight();
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, options);
        int photoW = options.outWidth;
        int photoH = options.outHeight;
        int scaleFactor = Math.min(photoW / viewW, photoH / viewH);
        options.inJustDecodeBounds = false;
        options.inSampleSize = scaleFactor;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
        photoView.setImageBitmap(bitmap);
    }

    @Override
    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        switch (reqCode) {
            case REQ_PHOTO:
                if (resCode == RESULT_OK) {
                    showPhoto();
                }
                break;
        }
    }

}
