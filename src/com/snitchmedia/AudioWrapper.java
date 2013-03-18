package com.snitchmedia;

import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.SoundPool;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class AudioWrapper {

    final MediaRecorder recorder = new MediaRecorder();
    private int soundId;
    final SoundPool soundpool;
    final String path;
    MediaPlayer mediaPlayer;
    private int streamId = 0;
    private boolean firstPlay = true;
    private float volume = 100.0F;
    public static int MAX_LENGTH = 100;

    /**
     * Creates a new audio recording at the given path (relative to root of SD card).
     */
    public AudioWrapper(SoundPool soundPool) {
       this.path = generateFileName();
       this.soundpool = soundPool;
    }

    private String generateFileName() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return sanitizePath(randomStringBuilder.toString());
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
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare();
            mediaPlayer.setLooping(true);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });
        } else {
            mediaPlayer.start();
        }
    }
    
    public float getDuration() {
        return mediaPlayer != null ? (float)mediaPlayer.getDuration() : 0;
    }
    
    public float getCurrentPosition() {
        return mediaPlayer != null ? (float)mediaPlayer.getCurrentPosition() : 0;
    }

    public int getProgress() {
        float duration = getDuration();
        float position = getCurrentPosition();
        return Math.round(position/duration * 100);
    }

    public void toggleMute() {
        volume = volume > 0 ? 0.0F : 1.0F;
        mediaPlayer.setVolume(volume, volume);
    }

    public void pause() {
        mediaPlayer.pause();
    }

}
