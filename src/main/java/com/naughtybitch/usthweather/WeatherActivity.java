package com.naughtybitch.usthweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity {

    MediaPlayer mediaPlayer;
    File convertedFile;
    FileInputStream fileInputStream;
    FileOutputStream out;

    int mediaPos, mediaMax;
    SeekBar seekBar;
    Handler handler;

    private ViewPager viewPager;

    TextView current, duration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.i("create_tag", "Creating...");
        HomeFragmentPagerAdapter adapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        handler = new Handler();
        handler.removeCallbacks(moveSeekBarThread);
        handler.postDelayed(moveSeekBarThread, 200);

        // No brain way
//        mediaPlayer = MediaPlayer.create(this, R.raw.champion_of_the_world_coldplay);
//        mediaPlayer.start();

        // Programmatically way
        InputStream inputStream = this.getResources().openRawResource(R.raw.champion_of_the_world_coldplay);
        try {
            takeInputStream(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

//        // Create a new fragment to be placed in the activity !
//        ForecastFragment firstFragment =  new ForecastFragment();
//        firstFragment.getView().setBackgroundColor(Color.RED);
//
//        // Add the fragment to the container LinearLayout
//        getSupportFragmentManager().beginTransaction().add(
//                R.id.container, firstFragment).commit();
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format(Locale.US, "%02d", minutes))
                .append(":")
                .append(String.format(Locale.US, "%02d", seconds));

        return buf.toString();
    }

    private Runnable moveSeekBarThread = new Runnable() {
        @Override
        public void run() {
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
            WeatherAndForecastFragment fragment = (WeatherAndForecastFragment) page;
            if (page != null) {
                current = fragment.getView().findViewById(R.id.current_position);
                duration = fragment.getView().findViewById(R.id.duration);
                seekBar = fragment.getView().findViewById(R.id.seek_bar);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mediaPlayer.seekTo(progress);
                            seekBar.setProgress(progress);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                if (mediaPlayer.isPlaying()) {
                    mediaPos = mediaPlayer.getCurrentPosition();
                    mediaMax = mediaPlayer.getDuration();
                    current.setText(getTimeString(mediaPos));
                    duration.setText(getTimeString(mediaMax));
                    seekBar.setMax(mediaMax);
                    seekBar.setProgress(mediaPos);
                    handler.postDelayed(this, 1); // Looping the thread after 0.001 second
                }
            }
        }
    };

    private void takeInputStream(InputStream inputStream) throws IOException {
        try {
            convertedFile = File.createTempFile("convertedFile", ".dat", getDir("filez", 0));
            out = new FileOutputStream(convertedFile);
            byte[] buffer = new byte[16384];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            out.close();
            playFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playFile() {
        try {
            mediaPlayer = new MediaPlayer();
            fileInputStream = new FileInputStream(convertedFile);
            mediaPlayer.setDataSource(fileInputStream.getFD());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
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
        mediaPlayer.stop();
        super.onDestroy();
        Log.i("destroy_tag", "Destroying...");
    }

}
