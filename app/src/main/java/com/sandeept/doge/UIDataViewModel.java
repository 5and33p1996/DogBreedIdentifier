package com.sandeept.doge;

import android.graphics.Bitmap;

import androidx.lifecycle.ViewModel;

import java.util.HashMap;

public class UIDataViewModel extends ViewModel {

    private Bitmap photo = null;
    private HashMap<String, Float> predictions = null;

    private long timeTaken;

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public HashMap<String, Float> getPredictions() {
        return predictions;
    }

    public void setPredictions(HashMap<String, Float> predictions) {
        this.predictions = predictions;
    }

    void setBitmap(Bitmap photo){

        if(this.photo != null){

            this.photo.recycle();
        }

        this.photo = photo;
    }

    Bitmap getBitmap(){

        return photo;
    }

    boolean hasData(){

        return photo != null || predictions != null;
    }

    void clearData(){

        if(photo != null) {

            photo.recycle();
            photo = null;
        }

        predictions = null;
    }
}