package com.example.dogbreedidentifier;

import android.graphics.Bitmap;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;

public class UIDataViewModel extends ViewModel {

    private Bitmap photo = null;
    private HashMap<String, Float> predictions = null;

    public HashMap<String, Float> getPredictions() {
        return predictions;
    }

    public void setPredictions(HashMap<String, Float> predictions) {
        this.predictions = predictions;
    }

    void setBitmap(Bitmap photo){

        this.photo = photo;
    }

    Bitmap getPhoto(){

        return photo;
    }
}
