package com.snitchmedia;

import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

public class AudioWrapper {

    final MediaRecorder recorder = new MediaRecorder();
    private int soundId;
    final SoundPool soundpool;
    final String path;
    private int streamId = 0;
    private boolean firstPlay = true;
    private float volume = 100.0F;

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioWrapper(String path, SoundPool soundPool) {
        this.path = sanitizePath(path);
        this.soundpool = soundPool;
    }

    private String sanitizePath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        if (!path.contains(".")) {
            path += ".3gp";
        }
        return Environment.getExternalStorageDirectory().getAbsolutePath() + path;
    }

    /**
     * Starts a new recording.
     */
    public void record() throws IOException {
        String state = android.os.Environment.getExternalStorageState();
        if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
            throw new IOException("SD Card is not mounted.  It is " + state + ".");
        }

        // make sure the directory we plan to store the recording in exists
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created.");
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(path);
        recorder.prepare();
        recorder.start();
    }

    public void stop() throws IOException {
        recorder.stop();
        recorder.release();
        soundId = soundpool.load(path, 1);
    }

    public void play() throws IOException {
        streamId = soundpool.play(soundId, volume, volume, 1, -1, 1f);
    }
    
    public int getDuration() {
        return 0; //player.getDuration();
    }
    
    public int getCurrentPosition() {
        return 0; //player.getCurrentPosition();
    }

    public void toggleMute() {
        volume = volume > 0 ? 0.0F : 100.0F;
        soundpool.setVolume(streamId, volume, volume);
    }

    public void pause() {
        if(streamId > 0) {
          soundpool.resume(streamId);
          soundpool.pause(streamId);
        }
    }

}
