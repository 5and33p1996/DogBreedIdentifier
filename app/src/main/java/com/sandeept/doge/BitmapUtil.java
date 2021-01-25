package com.sandeept.doge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class BitmapUtil {

    static Bitmap getBitmapFromUri(Uri uri, Context context){

        Bitmap bitmap = null;

        try{

            InputStream inputStream = context.getContentResolver().openInputStream(uri);

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
}
