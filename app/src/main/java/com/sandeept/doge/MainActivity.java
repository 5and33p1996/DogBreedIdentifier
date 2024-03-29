package com.sandeept.doge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;

import androidx.exifinterface.media.ExifInterface;
import androidx.lifecycle.ViewModelProvider;

import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.android.play.core.tasks.OnCompleteListener;
import com.google.android.play.core.tasks.Task;

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

    private static final String STAT_PREFERENCES = "com.sandeeptadepalli.doge.STAT_PREFERENCE";
    private static final String NO_OF_TIMES_USED_KEY = "TIMES_USED";

    private static final int TIMES_USED_DEF_VALUE = 0;

    private UIDataViewModel viewModel;

    private ImageView imageView;
    private TextView timeView;
    private TextView resultConfidence;
    private TextView amIRight;
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
        resultConfidence = findViewById(R.id.result_confidence);
        amIRight = findViewById(R.id.am_i_right);
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

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        Bundle networkBundleExtra = new Bundle();
        networkBundleExtra.putInt("rdp",1);

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class,
                networkBundleExtra).build();
        adView.loadAd(adRequest);
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

        else if(item.getItemId() == R.id.supported_breeds){

            Intent intent = new Intent(this, SupportedBreedsActivity.class);
            startActivity(intent);

            return true;
        }

        else{

            return super.onOptionsItemSelected(item);
        }
    }

    public void onCaptureClick(View view){

        //Take a picture
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){

            ToastUtil.showToast(this, "No camera detected!!");
            return;
        }

        if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, WRITE_PERMISSION_CODE)){

            captureImage();
        }
    }

    public void onPickFile(View view){

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/jpeg");
        startActivityForResult(intent, PICK_FILE_CODE);
    }

    public void onAmIRight(View view){

        String[] items = {"The Result is correct", "The result is not correct"};
        int checkedItem = 0;

        if(viewModel.getIsFeedbackProvided())
        {
            return;
        }

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        dialogBuilder.setTitle("Select one option")
                .setNeutralButton("Cancel", null)
                .setSingleChoiceItems(items, checkedItem, null)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        ListView listView = ((AlertDialog)dialogInterface).getListView();
                        int pos = listView.getCheckedItemPosition();

                        amIRight.setText(getResources().getText(R.string.am_i_right_post_feedback));
                        viewModel.setFeedbackProvided(true);

                        if(pos == 0)
                        {
                            ToastUtil.showToast(getApplicationContext(), "Thanks for your feedback!");
                        }

                        else if(pos == 1)
                        {
                            Intent intent = new Intent(getApplicationContext(), IncorrectResultActivity.class);
                            intent.putExtra("PhotoUri", viewModel.getPhotoUri().toString());

                            Map.Entry<String, Float> firstElem = viewModel.getPredictions().entrySet().iterator().next();
                            String topBreed = firstElem.getKey();
                            Float topPercent = firstElem.getValue() * 100;

                            intent.putExtra("TopBreed", topBreed);
                            intent.putExtra("TopPercent", topPercent);

                            startActivity(intent);
                        }
                    }
                }).show();
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
        displayResult(false);
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

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
//        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);

        Uri photoUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        if(photoUri == null)
        {
            ToastUtil.showToast(this, "Unable to capture image!");
            return;
        }

        viewModel.setPhotoUri(photoUri);

        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.getPhotoUri());
        startActivityForResult(captureIntent, REQUEST_CAPTURE_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){

            ToastUtil.showToast(this, "No image selected");
            return;
        }

        if (requestCode == REQUEST_CAPTURE_CODE) {

            Uri photoUri = viewModel.getPhotoUri();
            Bitmap capturedPhoto = BitmapUtil.getBitmapFromUri(photoUri, this);

            if(capturedPhoto == null){

                ToastUtil.showToast(this, "Unable to get the file!");
                return;
            }

            int rotateAngle = getScreenOrientation();

            if(rotateAngle == 0){

                viewModel.clearData();
                viewModel.setBitmap(capturedPhoto);
                viewModel.setPhotoUri(photoUri);
            }

            else{

                Matrix matrix = new Matrix();
                matrix.postRotate(rotateAngle);

                Bitmap bitmap = Bitmap.createBitmap(capturedPhoto, 0, 0, capturedPhoto.getWidth(),
                        capturedPhoto.getHeight(), matrix, true);

                viewModel.clearData();
                viewModel.setBitmap(bitmap);
                viewModel.setPhotoUri(photoUri);
            }

            imageView.setImageBitmap(viewModel.getBitmap());
        }

        else if (requestCode == PICK_FILE_CODE){

            Uri uri = data.getData();

            if(uri == null){

                ToastUtil.showToast(this, "Unable to open the file");
                return;
            }

            viewModel.setPhotoUri(uri);

            Bitmap bitmap = BitmapUtil.getBitmapFromUri(viewModel.getPhotoUri(), this);

            if(bitmap == null){
                //Error
                ToastUtil.showToast(this, "Unable to open the file");
                return;
            }

            int rotateAngle = getScreenOrientation();

            if(rotateAngle == 0){

                viewModel.clearData();
                viewModel.setBitmap(bitmap);
                viewModel.setPhotoUri(uri);
            }

            else{

                Matrix matrix = new Matrix();
                matrix.postRotate(rotateAngle);

                Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, true);

                viewModel.clearData();
                viewModel.setBitmap(rotatedBitmap);
                viewModel.setPhotoUri(uri);
            }

            imageView.setImageBitmap(viewModel.getBitmap());
        }

        cardView.setVisibility(View.VISIBLE);
        inferenceButton.setVisibility(View.VISIBLE);
        resultLayout.setVisibility(View.GONE);

        amIRight.setText(R.string.am_i_right);
        viewModel.setFeedbackProvided(false);
    }

    public void onStartInference(View view){

        inferenceButton.setVisibility(View.GONE);
        progressLayout.setVisibility(View.VISIBLE);

        SharedPreferences sharedPreferences = getSharedPreferences(STAT_PREFERENCES, Context.MODE_PRIVATE);
        int no_of_times_used = sharedPreferences.getInt(NO_OF_TIMES_USED_KEY, TIMES_USED_DEF_VALUE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(NO_OF_TIMES_USED_KEY, ++no_of_times_used);
        editor.apply();

        final boolean shouldAskReview;

        shouldAskReview = no_of_times_used == 2 || no_of_times_used % 4 == 0;

        new Thread(new Runnable(){
            public void run(){

                //Lock the screen rotation to avoid crashes....This is not the perfect solution to the crash
                //but for now this works
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

                final long startTime = SystemClock.uptimeMillis();
                final HashMap<String, Float> predictions = classifier.predict(viewModel.getBitmap());
                final long endTime = SystemClock.uptimeMillis();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        viewModel.setPredictions(predictions);
                        viewModel.setTimeTaken(endTime - startTime);
                        displayResult(shouldAskReview);

                        //Can Unlock the screen rotation now
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
                    }
                });
            }
        }).start();
    }

    private void displayResult(boolean shouldAskReview){

        String timeString = "Time Taken for inference - " + viewModel.getTimeTaken() + "ms";
        timeView.setText(timeString);

        DecimalFormat df = new DecimalFormat("0.00");

        int i = 0;

        double highest_conf = 0;

        for(Map.Entry<String, Float> entry : viewModel.getPredictions().entrySet()){

            if(i == NUMBER_OF_RESULTS){

                break;
            }

            results[i].setText(entry.getKey());

            String confidence = df.format(entry.getValue() * 100) + "%";
            confidences[i].setText(confidence);

            if(i == 0)
            {
                highest_conf = entry.getValue() * 100;
            }

            i++;
        }

        if(highest_conf < 70.0)
        {
            resultConfidence.setVisibility(View.VISIBLE);

            if(highest_conf < 30.0)
            {
                resultConfidence.setText(getResources().getText(R.string.result_confidence_low));
            }
            else
            {
                resultConfidence.setText(getResources().getText(R.string.result_confidence_medium));
            }
        }
        else
        {
            resultConfidence.setVisibility(View.GONE);
        }

        progressLayout.setVisibility(View.GONE);
        resultLayout.setVisibility(View.VISIBLE);

        if(viewModel.getIsFeedbackProvided()) {
            amIRight.setText(R.string.am_i_right_post_feedback);
        }

        if(shouldAskReview)
        {
            requestReview();
        }
    }

    private void requestReview()
    {
        final ReviewManager reviewManager = ReviewManagerFactory.create(this);
        final Activity activity = this;

        Task<ReviewInfo> request = reviewManager.requestReviewFlow();

        request.addOnCompleteListener(new OnCompleteListener<ReviewInfo>() {
            @Override
            public void onComplete(@NonNull Task<ReviewInfo> task) {

                if(task.isSuccessful())
                {
                    ReviewInfo info = task.getResult();

                    Task<Void> flow = reviewManager.launchReviewFlow(activity, info);
                    flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            //No matter what, continue
                        }
                    });
                }
            }
        });
    }

    private int getScreenOrientation(){

        ExifInterface exifInterface;

        try {

            InputStream inputStream = getContentResolver().openInputStream(viewModel.getPhotoUri());

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