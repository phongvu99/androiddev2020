package com.naughtybitch.usthweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
    FileOutputStream outputStream;

    int mediaPos, mediaMax;
    SeekBar seekBar;
    Handler handler;

    private ViewPager viewPager;

    TextView current, duration;

    ImageButton imageButton;

    HomeFragmentPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        Log.i("create_tag", "Creating...");
        adapter = new HomeFragmentPagerAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
        // Give the TabLayout the ViewPager
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
        handler = new Handler(Looper.getMainLooper()){
            @Override
            public void handleMessage(Message msg) {
                // This method is executed in main thread
                String content = msg.getData().getString("server_response");
                Toast.makeText(WeatherActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        };

        // Custom ActionBar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Programmatically way
        InputStream inputStream = getResources().openRawResource(R.raw.champion_of_the_world_coldplay);
        takeInputStream(inputStream, "CP.mp3");
        // No brain way
        mediaPlayer = MediaPlayer.create(this, R.raw.champion_of_the_world_coldplay);

        handler.removeCallbacks(moveSeekBarThread);
        handler.removeCallbacks(controlPlayback);
        handler.postDelayed(controlPlayback, 200);
        handler.postDelayed(moveSeekBarThread, 200);


//        // Create a new fragment to be placed in the activity !
//        ForecastFragment firstFragment =  new ForecastFragment();
//        firstFragment.getView().setBackgroundColor(Color.RED);
//
//        // Add the fragment to the container LinearLayout
//        getSupportFragmentManager().beginTransaction().add(
//                R.id.container, firstFragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh:
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // wait for 5 seconds to simulate a long network access
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        // Assume that we got our data from server
                        Bundle bundle = new Bundle();
                        bundle.putString("server_response", "some sample json here");

                        // Notify main thread
                        Message msg = new Message();
                        msg.setData(bundle);
                        handler.sendMessage(msg);
                    }
                });
                t.start();
                Toast.makeText(this, "You did it!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings:
                Intent intent = new Intent(this, PrefActivity.class);
                startActivity(intent);
                break;
            default:
                return true;
        }
        return true;
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format(Locale.US, "%01d", minutes))
                .append(":")
                .append(String.format(Locale.US, "%02d", seconds));

        return buf.toString();
    }

    private Runnable controlPlayback =  new Runnable() {
        @Override
        public void run() {
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
            WeatherAndForecastFragment fragment = (WeatherAndForecastFragment) page;
            if (page != null) {
                try {
                    imageButton = fragment.getView().findViewById(R.id.control_playback);
                    seekBar = fragment.getView().findViewById(R.id.seek_bar);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }

                if (mediaPlayer.isPlaying()) {
                    imageButton.setImageResource(R.drawable.pause_button);
                } else {
                    imageButton.setImageResource(R.drawable.play_button);
                }
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mediaPlayer.isPlaying()) {
                            mediaPlayer.pause();
                            imageButton.setImageResource(R.drawable.play_button);
                        } else {
                            mediaPlayer.start();
                            imageButton.setImageResource(R.drawable.pause_button);
                        }
                    }
                });
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
            }
            handler.postDelayed(this, 1); // Looping the thread after 0.001 second
        }

    };

    private Runnable moveSeekBarThread = new Runnable() {
        @Override
        public void run() {
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
            WeatherAndForecastFragment fragment = (WeatherAndForecastFragment) page;
            if (page != null) {
                try {
                    current = fragment.getView().findViewById(R.id.current_position);
                    duration = fragment.getView().findViewById(R.id.duration);
                    seekBar = fragment.getView().findViewById(R.id.seek_bar);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                if (mediaPlayer.isPlaying()) {
                    mediaPos = mediaPlayer.getCurrentPosition();
                    mediaMax = mediaPlayer.getDuration();
                    current.setText(getTimeString(mediaPos));
                    duration.setText(getTimeString(mediaMax));
                    seekBar.setMax(mediaMax);
                    seekBar.setProgress(mediaPos);
                }
            }
            handler.postDelayed(this, 1); // Looping the thread after 0.001 second
        }
    };

    private void takeInputStream(InputStream inputStream, String resourceName) {
        String path = Environment.getExternalStorageDirectory() + "/Android/data/com.naughtybitch.usthweather/" + resourceName;
        try {
//            convertedFile = File.createTempFile("convertedFile", ".dat", getDir("filez", 0));
//            outputStream = new FileOutputStream(convertedFile);
            outputStream = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
            playFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playFile() {
//            fileInputStream = new FileInputStream();
//            mediaPlayer.setDataSource(fileInputStream.getFD());
        mediaPlayer = new MediaPlayer();
//            mediaPlayer.prepare(); // Prepare to crash
        mediaPlayer.setLooping(true);
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
