package com.RikoKarmenJeanMoe.jonesmap;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatDelegate;

public class IntentHelper {

    public static void openIntent(Context context,  Class passTo)
    {
        //declare intent with context and class to pass the value
        Intent i = new Intent(context,passTo);

        //start the activity
        context.startActivity(i);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }

}
