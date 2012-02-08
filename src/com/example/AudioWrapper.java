package com.example;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;

import android.util.Log;

import java.io.File;
import java.io.IOException;

public class AudioWrapper {

    final MediaRecorder recorder = new MediaRecorder();
    final MediaPlayer player = new MediaPlayer();
    final String path;

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioWrapper(String path) {
        this.path = sanitizePath(path);
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
    }

    public void play() throws IOException {
        String state = android.os.Environment.getExternalStorageState();
        if(!state.equals(android.os.Environment.MEDIA_MOUNTED))  {
            throw new IOException("SD Card is not mounted.  It is " + state + ".");
        }

        // make sure the directory we plan to store the recording in exists
        File directory = new File(path).getParentFile();
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Path to file could not be created.");
        }

        player.setDataSource(path);
        player.setLooping(true);
        player.prepare();
        player.start();
    }
    
    public int getDuration() {
        return player.getDuration();
    }
    
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void pause() {
        player.pause();
    }

}
