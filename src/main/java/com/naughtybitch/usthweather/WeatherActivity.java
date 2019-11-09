package com.naughtybitch.usthweather;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class WeatherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.i("create_tag", "Creating...");

//        // Create a new fragment to be placed in the activity !
//        ForecastFragment firstFragment = new ForecastFragment();
//        firstFragment.getView().setBackgroundColor(Color.RED);
//
//        // Add the fragment to the container LinearLayout
//        getSupportFragmentManager().beginTransaction().add(
//                R.id.container, firstFragment).commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("start_tag", "Starting...");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("resume_tag", "Resuming...");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("pause_tag", "Pausing...");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("stop_tag", "Stopping...");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("destroy_tag", "Destroying...");
    }

}
