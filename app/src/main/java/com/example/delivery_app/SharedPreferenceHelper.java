package com.example.delivery_app;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceHelper {

    private SharedPreferences sharedPreferences;

    public SharedPreferenceHelper(Context context){
        sharedPreferences = context.getSharedPreferences("profilePreference", Context.MODE_PRIVATE);
    }

    public void saveCounter(int counter){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("counter",counter);
        editor.commit();
    }

    public int getCounter(){
        return sharedPreferences.getInt("counter", 0);
    }

    public void saveWeight(Float weight){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("weight",weight);
        editor.commit();
    }

    public Float getWeight(){
        return sharedPreferences.getFloat("weight", 0);
    }

}
