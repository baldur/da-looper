package com.snitchmedia;

import android.app.Activity;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class RecorderActivity extends Activity {
    private String TAG = "Looper#RecorderActivity";
    private ToggleButton recordBtn;
    private static int recordCount = 0;
    private static String[] fileNames = {"first_file", "second_file", "third_file", "forth_file"};
    private static AudioWrapper[] audioDevices = new AudioWrapper[4];
    private static SoundPool soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

    private void stopRecording() {
        try {
            audioDevices[recordCount].stop();
            recordCount++;
        } catch (IOException e) {

        }
    }

    @Override
    public void onCreate(Bundle icicle) {
        soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener(){
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
            int status) {
                Log.v(TAG, "\n\n\n************\n\nloaded sample");
            }
        });
        super.onCreate(icicle);
        setContentView(R.layout.studio);

        View.OnClickListener recClicker;
        recClicker = new View.OnClickListener() {
            public void onClick(View v) {
                ToggleButton btn = (ToggleButton)v;
                if (btn.isChecked()) {
                    if(recordCount >= 4) {
                        Toast.makeText(getApplicationContext(), "Only supports 4 tracks.", Toast.LENGTH_SHORT).show();
                        btn.setChecked(false);
                        return;
                    } else {
                        btn.setEnabled(false);
                        Intent intent = new Intent(getApplicationContext(), Recordings.class);
                        startService(intent);
                        recordBtn.setEnabled(true);
                    }
                } else {
                    addRow(recordCount);
                    stopRecording();
                }
            }
        };

        recordBtn = (ToggleButton)findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(recClicker);
    }

    private void addRow(int track) {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableLayout container = (TableLayout)findViewById(R.id.container);
        inflater.inflate(R.layout.play_row, container, true);
        ToggleButton playBtn = (ToggleButton)container.findViewWithTag("unused");
        playBtn.setTag(track);
        final ProgressBar seekBar = (ProgressBar)container.findViewWithTag("unused progressbar");
        seekBar.setTag(track);
        playBtn.setOnClickListener(
                new View.OnClickListener() {
                    public void onClick(View v) {
                        ToggleButton btn = (ToggleButton) v;
                        final int track = ((Number) btn.getTag()).intValue();
                        if (btn.isChecked()) {
                            try {
                                audioDevices[track].play();
                                Timer t = new Timer();
                                TimerTask tt = new TimerTask() {
                                    @Override
                                    public void run() {
                                        float duration = (float)audioDevices[track].getDuration();
                                        float position = (float)audioDevices[track].getCurrentPosition();
                                        int percent = Math.round(position/duration * 100);
                                        //Log.v(TAG, "Tick Tack" + percent);
                                        seekBar.setProgress(percent);
                                    }
                                };
                                t.schedule(tt, 100, 100);
                            } catch (IOException e) {
                            }
                        } else {
                            audioDevices[track].pause();
                        }
                    }
                }
        );

        ToggleButton muteBtn = (ToggleButton)container.findViewWithTag("unused mute button");
        muteBtn.setTag(track);
        muteBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ToggleButton btn = (ToggleButton) v;
                        int track = ((Number) btn.getTag()).intValue();
                        audioDevices[track].toggleMute();
                    }
                }
        );

    }

    @Override
    public void onPause() {
        super.onPause();
        if (audioDevices != null) {
            audioDevices = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        soundPool.release();
    }

    static public class Recordings extends IntentService {
        private boolean startRecording() {
            audioDevices[recordCount] = new AudioWrapper(fileNames[recordCount], soundPool);
            try {
                audioDevices[recordCount].record();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        public Recordings() {
            super("Recordings");
        }

        @Override
        protected void onHandleIntent(Intent intent) {
            startRecording();
        }
    }
}