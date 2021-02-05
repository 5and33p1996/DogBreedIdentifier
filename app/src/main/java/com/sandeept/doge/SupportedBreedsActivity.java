package com.sandeept.doge;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class SupportedBreedsActivity extends AppCompatActivity {

    private BreedRecyclerAdapter adapter;
    private ArrayList<String> breeds;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supported_breeds);

        recyclerView = findViewById(R.id.recycler_view);
        EditText breedFilter = findViewById(R.id.search_filter);

        breeds = new ArrayList<>();

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

        adapter = new BreedRecyclerAdapter(breeds);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        addTextChangedListeners(breedFilter);
    }

    @Override
    protected void onDestroy(){

        recyclerView.setAdapter(null);
        super.onDestroy();
    }

    void addTextChangedListeners(EditText editText){

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                filter(editable.toString());
            }
        });
    }

    void filter(String text){

        ArrayList<String> filteredList = new ArrayList<>();

        for(String name : breeds){

            if(name.toLowerCase().contains(text.toLowerCase())){

                filteredList.add(name);
            }
        }

        adapter.updateList(filteredList);
    }
}