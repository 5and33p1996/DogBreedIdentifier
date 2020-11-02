package com.example.dogbreedidentifier;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

//A man who thinks he can and a man who thinks he cannot are both right!!

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAPTURE_CODE = 1;
    private static final int PICK_FILE_CODE = 7;
    private static final int WRITE_PERMISSION_CODE = 2;

    private static final int NUMBER_OF_RESULTS = 5;

    private UIDataViewModel viewModel;
    private Uri photoUri;

    private ImageView imageView;
    private TextView timeView;
    private CardView cardView;
    private LinearLayout progressLayout;
    private LinearLayout resultLayout;
    private Button inferenceButton;

    private TextView[] results;
    private TextView[] confidences;

    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.img_view);
        timeView = findViewById(R.id.time);
        cardView = findViewById(R.id.card_view);
        progressLayout = findViewById(R.id.progress_layout);
        inferenceButton = findViewById(R.id.inference_button);
        resultLayout = findViewById(R.id.result_layout);

        results = new TextView[NUMBER_OF_RESULTS];
        confidences = new TextView[NUMBER_OF_RESULTS];

        results[0] = findViewById(R.id.result_1);
        confidences[0] = findViewById(R.id.confidence_1);
        results[1] = findViewById(R.id.result_2);
        confidences[1] = findViewById(R.id.confidence_2);
        results[2] = findViewById(R.id.result_3);
        confidences[2] = findViewById(R.id.confidence_3);
        results[3] = findViewById(R.id.result_4);
        confidences[3] = findViewById(R.id.confidence_4);
        results[4] = findViewById(R.id.result_5);
        confidences[4] = findViewById(R.id.confidence_5);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        classifier = new Classifier();
        try {
            classifier.Initialize(this);
        }catch (IOException ioe){

            ToastUtil.showToast(this, "Fatal Error Occurred!!");
            classifier = null;
            return;
        }

        viewModel = new ViewModelProvider(this).get(UIDataViewModel.class);

        if(viewModel.hasData()){

            displayViewModelData();
        }
    }

    @Override
    protected void onDestroy(){

        super.onDestroy();

        if(classifier != null){

            classifier.Finalize();
        }
    }

    @Override
     public boolean onCreateOptionsMenu(Menu menu){

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){

        if(item.getItemId() == R.id.about){

            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }

        else if(item.getItemId() == R.id.getting_best_results){

            Intent intent = new Intent(this, BestResultsActivity.class);
            startActivity(intent);

            return true;
        }

        else{

            return super.onOptionsItemSelected(item);
        }
    }

    public void onCaptureClick(View view){

        //Take a picture
        //TODO: In the manifest it is declared that camera is not essential, thus check at runtime
        //if a device has camera or not

        if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_PERMISSION_CODE)){

            captureImage();
        }
    }

    public void onPickFile(View view){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        startActivityForResult(intent, PICK_FILE_CODE);
    }

    public void onResultHelp(View view){

        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    public void onNotSatisfied(View view){

        Intent intent = new Intent(this, BestResultsActivity.class);
        startActivity(intent);
    }

    public boolean checkPermission(String permission, int requestCode){

        if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{permission},
                    requestCode);

            return false;
        }

        return true;
    }

    void displayViewModelData(){

        cardView.setVisibility(View.VISIBLE);

        imageView.setImageBitmap(viewModel.getBitmap());

        if(viewModel.getPredictions() == null){

            inferenceButton.setVisibility(View.VISIBLE);
            resultLayout.setVisibility(View.GONE);
            return;
        }

        inferenceButton.setVisibility(View.GONE);
        displayResult();
    }

    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                              @NonNull int[] grantResults){

        if(requestCode == WRITE_PERMISSION_CODE){

            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                captureImage();
            }
        }
    }

    private void captureImage(){

        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String relativeLocation = Environment.DIRECTORY_PICTURES + File.separator + "Doge";

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        startActivityForResult(captureIntent, REQUEST_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){

            ToastUtil.showToast(this, "No image selected");
            return;
        }

        viewModel.clearData();

        if (requestCode == REQUEST_CAPTURE_CODE) {

            Bitmap capturedPhoto = getBitmapFromUri(photoUri);

            if(capturedPhoto == null){

                ToastUtil.showToast(this, "Unable to get the file!");
                return;
            }

            int rotateAngle = getScreenOrientation();

            if(rotateAngle == 0){

                viewModel.setBitmap(capturedPhoto);
            }

            else{

                Matrix matrix = new Matrix();
                matrix.postRotate(rotateAngle);

                Bitmap bitmap = Bitmap.createBitmap(capturedPhoto, 0, 0, capturedPhoto.getWidth(),
                        capturedPhoto.getHeight(), matrix, true);

                viewModel.setBitmap(bitmap);
            }

            imageView.setImageBitmap(viewModel.getBitmap());
        }

        else if (requestCode == PICK_FILE_CODE){

            Uri uri = data.getData();

            if(uri == null){

                ToastUtil.showToast(this, "Unable to get the image!");
                return;
            }

            Bitmap bitmap = getBitmapFromUri(uri);

            if(bitmap == null){
                //Error
                ToastUtil.showToast(this, "Unable to load Bitmap!");
                return;
            }

            viewModel.setBitmap(bitmap);

            imageView.setImageBitmap(viewModel.getBitmap());
        }

        cardView.setVisibility(View.VISIBLE);
        inferenceButton.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);

    }

    Bitmap getBitmapFromUri(Uri uri){

        Bitmap bitmap = null;

        try{

            InputStream inputStream = getContentResolver().openInputStream(uri);

            if(inputStream == null){
                return null;
            }

            bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
        }catch(FileNotFoundException fne){

            return null;
        }catch (IOException ioe){

            return null;
        }

        return bitmap;
    }

    public void onStartInference(View view){

        inferenceButton.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

        new Thread(new Runnable(){
            public void run(){

                final long startTime = SystemClock.uptimeMillis();
                final HashMap<String, Float> predictions = classifier.predict(viewModel.getBitmap());
                final long endTime = SystemClock.uptimeMillis();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.setPredictions(predictions);
                        viewModel.setTimeTaken(endTime - startTime);
                        displayResult();
                    }
                });
            }
        }).start();
    }

    private void displayResult(){

        String timeString = "Time Taken for inference - " + viewModel.getTimeTaken() + "ms";
        timeView.setText(timeString);

        DecimalFormat df = new DecimalFormat("0.00");

        int i = 0;

        for(Map.Entry<String, Float> entry : viewModel.getPredictions().entrySet()){

            if(i == NUMBER_OF_RESULTS){

                break;
            }

            results[i].setText(entry.getKey());

            String confidence = df.format(entry.getValue() * 100) + "%";
            confidences[i].setText(confidence);

            i++;
        }

        progressLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);
    }

    private int getScreenOrientation(){

        ExifInterface exifInterface = null;

        try {

            InputStream inputStream = getContentResolver().openInputStream(photoUri);

            if(inputStream == null){

                return 0;
            }

            exifInterface = new ExifInterface(inputStream);

        }catch (IOException ioe){

            return 0;
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

        if(orientation == ExifInterface.ORIENTATION_ROTATE_90){

            return 90;
        }

        else if(orientation == ExifInterface.ORIENTATION_ROTATE_180){

            return 180;
        }

        else if(orientation == ExifInterface.ORIENTATION_ROTATE_270){

            return 270;
        }

        return 0;
    }
}