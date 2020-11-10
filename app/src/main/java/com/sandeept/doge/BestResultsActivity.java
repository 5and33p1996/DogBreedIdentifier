package com.sandeept.doge;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BestResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_best_results);
    }

    public void onSupportedBreeds(View view){

        Intent intent = new Intent(this, SupportedBreedsActivity.class);
        startActivity(intent);
    }
}