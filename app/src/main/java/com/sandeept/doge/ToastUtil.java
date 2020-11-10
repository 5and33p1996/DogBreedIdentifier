package com.sandeept.doge;

import android.content.Context;
import android.widget.Toast;

public final class ToastUtil {

    static void showToast(Context context, String message){

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }
}
