package com.naughtybitch.usthweather;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.AsyncTask;
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
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import static java.lang.Thread.sleep;

public class WeatherActivity extends AppCompatActivity {

    Bitmap bitmap;

    URL url;
    MediaPlayer mediaPlayer;
    FileOutputStream outputStream;

    int mediaPos, mediaMax;
    SeekBar seekBar;
    Handler handler;

    private ViewPager viewPager;

    TextView current, duration;
    ImageView lemon;

    RotateAnimation animation;

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
        handler = new Handler(Looper.getMainLooper());

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

    private class NetworkRequest extends AsyncTask<URL, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            try {

                handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        // This method is executed in main thread
                        String content = msg.getData().getString("server_response");
                        Toast.makeText(WeatherActivity.this, content, Toast.LENGTH_SHORT).show();
                    }
                };

                // Initialize URL
                url = new URL("https://ictlab.usth.edu.vn/wp-content/uploads/2018/03/usth-vf-180.png");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(final URL... urls) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // Make a request to server
                        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                        connection.setRequestMethod("GET");
                        connection.setDoInput(true);
                        // Allow reading response code and response data connection
                        connection.connect();
                        // Receive response
                        int response = connection.getResponseCode();
                        Log.i("USTHWeather", "The response is " + response);
                        InputStream is = connection.getInputStream();

                        // Process image response
                        bitmap = BitmapFactory.decodeStream(is);
                        Log.i("bitmap", "Bitmap is " + bitmap);

                        connection.disconnect();
                    } catch (IOException e) {
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
            // Wait for the request, else bitmap might be null
            int timeout = 0;
            while (bitmap == null) {
                if (timeout == 10) {
                    Log.i("timeout", "Timeout, failed to get the image");
                    break;
                }
                try {
                    Thread.sleep(1000);
                    timeout++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
            WeatherAndForecastFragment fragment = (WeatherAndForecastFragment) page;
            if (page != null && bitmap != null) {
                ImageView logo = (ImageView) fragment.getView().findViewById(R.id.lemons_test);
                logo.setImageBitmap(bitmap);
            }
            else {
                Toast.makeText(WeatherActivity.this, "Timeout! \nDid you connect to the internet?", Toast.LENGTH_SHORT).show();
            }

        }
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
                new NetworkRequest().execute(url);
                Toast.makeText(this, "Please wait...", Toast.LENGTH_LONG).show();
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

    private Runnable controlPlayback = new Runnable() {
        @Override
        public void run() {
            animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            animation.setInterpolator(new LinearInterpolator());
            animation.setRepeatCount(Animation.INFINITE);
            animation.setDuration(2000);
            animation.setFillAfter(true);
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
                            lemon.clearAnimation();
                            imageButton.setImageResource(R.drawable.play_button);
                        } else {
                            mediaPlayer.start();
                            lemon.startAnimation(animation);
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
            handler.postDelayed(this, 1000); // Looping the thread after 1 second
        }

    };

    private Runnable moveSeekBarThread = new Runnable() {
        @Override
        public void run() {
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + viewPager.getCurrentItem());
            WeatherAndForecastFragment fragment = (WeatherAndForecastFragment) page;
            if (page != null) {
                try {
                    lemon = fragment.getView().findViewById(R.id.lemons_test);
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
            handler.postDelayed(this, 1000); // Looping the thread after 1 second
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
