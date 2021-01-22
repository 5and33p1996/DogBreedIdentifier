package com.sandeept.doge;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class IncorrectResultActivity extends AppCompatActivity {

    private RadioButton iKnow;
    private RadioButton iDontKnow;
    private AutoCompleteTextView autoCompleteTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incorrect_result);

        iKnow = findViewById(R.id.i_know);
        iDontKnow = findViewById(R.id.i_dont_know);

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
    }

    public void onRadioClick(View view)
    {
        boolean checked = ((RadioButton)view).isChecked();

        switch (view.getId())
        {
            case R.id.i_know:
                if(checked){

                    iDontKnow.setChecked(false);
                    autoCompleteTextView.setVisibility(View.VISIBLE);
                }
                break;

            case R.id.i_dont_know:
                if(checked){

                    iKnow.setChecked(false);
                    autoCompleteTextView.setVisibility(View.INVISIBLE);
                }

                break;
        }
    }

    public void onDoneClick(View view)
    {
        String claim_breed = autoCompleteTextView.getText().toString();
        String claim_breed_trimmed = claim_breed.trim();

        if(claim_breed_trimmed.length() == 0)
        {
            ToastUtil.showToast(this, "Please enter the breed name");
            return;
        }

        ToastUtil.showToast(this, claim_breed_trimmed);
    }
}