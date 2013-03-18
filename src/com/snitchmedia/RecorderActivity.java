package com.snitchmedia;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
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
    private int[] channelRowViews = {
        R.id.row_1,
        R.id.row_2,
        R.id.row_3,
        R.id.row_4,
        R.id.row_5
    };
    private static SoundPool soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

    private void stopRecording() {
        try {
            audioDevices[recordCount].stop();
            recordCount++;
        } catch (IOException e) { }
    }

    @Override
    public void onCreate(Bundle icicle) {
        soundPool.setOnLoadCompleteListener(createOnCompleteListener());
        super.onCreate(icicle);
        setContentView(R.layout.studio);
        recordBtn = (ToggleButton)findViewById(R.id.record_btn);
        recordBtn.setOnClickListener(createRecListener());
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

    private OnLoadCompleteListener createOnCompleteListener() {
        return new OnLoadCompleteListener(){
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
            }
        };
    }

    private View.OnClickListener createRecListener() {
        View.OnClickListener recClicker;
        recClicker = new View.OnClickListener() {
            public void onClick(View v) {
                ToggleButton btn = (ToggleButton)v;
                if (btn.isChecked()) {
                    if(recordCount >= 4) {
                        btn.setChecked(false);
                        return;
                    } else {
                        btn.setEnabled(false);
                        Intent intent = new Intent(getApplicationContext(), Recordings.class);
                        startService(intent);
                        recordBtn.setEnabled(true);
                    }
                } else {
                    stopRecording();
                    initButtons();
                }
            }
        };
        return recClicker;
    }

    private void initButtons() {
        View channelRow = findViewById(channelRowViews[recordCount-1]);
        final AudioWrapper device = audioDevices[recordCount-1];

        ToggleButton toggleBtn = (ToggleButton)channelRow.findViewById(R.id.playBtn);
        toggleBtn.setOnClickListener(createPlayToggleListener(device));

        ToggleButton muteBtn = (ToggleButton) channelRow.findViewById(R.id.muteBtn);
        muteBtn.setOnClickListener(createMuteListener(device));

        ProgressBar playHead = (ProgressBar) channelRow.findViewById(R.id.playHead);
        Timer timer = new Timer();
        UpdatePlayHead uph = new UpdatePlayHead();
        uph.setPlayHead(playHead);
        uph.setDevice(device);
        timer.schedule(uph, 100, 200);
    }

    private View.OnClickListener createMuteListener(final AudioWrapper device) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                device.toggleMute();
            }
        };
    }

    private View.OnClickListener createPlayToggleListener(final AudioWrapper device) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ToggleButton) v).isChecked()) {
                    try {
                        device.play();
                    } catch (IOException ioE) {
                    }
                } else {
                    device.pause();
                }
            }
        };
    }

    class UpdatePlayHead extends TimerTask {
        ProgressBar playHead;
        AudioWrapper device;

        public void setPlayHead(ProgressBar pb) {
            playHead = pb;
        }
        public void setDevice(AudioWrapper am) {
            device = am;
        }
        public void run() {
            playHead.setProgress(device.getProgress());
        }
    }
}
