package com.sandeept.doge;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.UUID;

public class IncorrectResultActivity extends AppCompatActivity {

    private RadioButton iKnow;
    private RadioButton iDontKnow;
    private AutoCompleteTextView autoCompleteTextView;
    private SwitchMaterial switchMaterial;
    private TextView breedInformText;

    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incorrect_result);

        iKnow = findViewById(R.id.i_know);
        iDontKnow = findViewById(R.id.i_dont_know);
        switchMaterial = findViewById(R.id.switch_button);
        breedInformText = findViewById(R.id.breed_inform_text);

        ArrayList<String> breeds = new ArrayList<>();

        try {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(getAssets().open("labels.txt")));

            String line = bufferedReader.readLine();
            while(line!= null) {

                breeds.add(line);
                line = bufferedReader.readLine();
            }

            bufferedReader.close();

        }catch (IOException ioe){
            //Do something
        }

        ArrayAdapter<String> breedAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, breeds);

        autoCompleteTextView = findViewById(R.id.autoComplete);
        autoCompleteTextView.setThreshold(2);
        autoCompleteTextView.setAdapter(breedAdapter);

        storage = FirebaseStorage.getInstance();
    }

    public void onRadioClick(View view)
    {
        boolean checked = ((RadioButton)view).isChecked();

        switch (view.getId())
        {
            case R.id.i_know:
                if(checked){

                    iDontKnow.setChecked(false);
                    breedInformText.setVisibility(View.VISIBLE);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.i_dont_know:
                if(checked){

                    iKnow.setChecked(false);
                    breedInformText.setVisibility(View.INVISIBLE);
                    autoCompleteTextView.setVisibility(View.INVISIBLE);
                }

                break;
        }
    }

    public void onDoneClick(View view)
    {
        String claim_breed = checkBreedString();

        if(claim_breed == null && iKnow.isChecked()){

            ToastUtil.showToast(this, "Please enter the name of the breed");
            return;
        }

        if(iDontKnow.isChecked())
        {
            claim_breed = "Unknown";
        }

        UploadTask uploadTask;

        if(switchMaterial.isChecked()){

            uploadTask = uploadWithFile(claim_breed);
        }
        else{

            uploadTask = uploadWithoutFile(claim_breed);
        }

        if(uploadTask == null)
        {
            finish();
            return;
        }

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ToastUtil.showToast(getApplicationContext(), "Uploaded " + taskSnapshot.getBytesTransferred() + "bytes");
            }
        });

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                ToastUtil.showToast(getApplicationContext(), "Upload Failed!");
            }
        });

        finish();
    }

    private String checkBreedString()
    {
        String claim_breed = autoCompleteTextView.getText().toString();
        String claim_breed_trimmed = claim_breed.trim();

        if(claim_breed_trimmed.length() == 0)
        {
            return null;
        }

        return claim_breed_trimmed;
    }

    private UploadTask uploadWithFile(String claimBreed)
    {
        Intent intent = getIntent();
        String uriString = intent.getStringExtra("PhotoUri");

        Uri uri = Uri.parse(uriString);

        Bitmap bitmap = BitmapUtil.getBitmapFromUri(uri, this);

        if(bitmap == null)
        {
            return null;
        }

        String topBreed = intent.getStringExtra("TopBreed");
        float topPercent = intent.getFloatExtra("TopPercent", 0);

        String fileName = UUID.randomUUID() + ".jpg";

        StorageReference storageReference = storage.getReference();
        StorageReference imageRef = storageReference.child("withImages/" + fileName);

        StorageMetadata metadata = new StorageMetadata.Builder().setContentType("image/jpg")
                .setCustomMetadata("Top Predicted Breed", topBreed)
                .setCustomMetadata("Top Prediction Percent", String.valueOf(topPercent))
                .setCustomMetadata("User Claim Breed", claimBreed)
                .build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);

        byte[] data = outputStream.toByteArray();

        return imageRef.putBytes(data, metadata);
    }

    private UploadTask uploadWithoutFile(String claimBreed)
    {
        Intent intent = getIntent();

        String topBreed = intent.getStringExtra("TopBreed");
        float topPercent = intent.getFloatExtra("TopPercent", 0);

        String fileName = UUID.randomUUID() + ".txt";

        String fileContent = new StringBuilder().append(topBreed).append(" - ")
                .append(topPercent).append("\n")
                .append(claimBreed).toString();

        File file = new File(getFilesDir(), fileName);
        FileWriter writer;

        try{
            writer = new FileWriter(file);
            writer.write(fileContent);
            writer.close();
        }catch(IOException ioe){
            //Do something
        }

        Uri uri = Uri.fromFile(file);

        StorageReference storageReference = storage.getReference();
        StorageReference imageRef = storageReference.child("withoutImages/" + fileName);

        return imageRef.putFile(uri);
    }
}