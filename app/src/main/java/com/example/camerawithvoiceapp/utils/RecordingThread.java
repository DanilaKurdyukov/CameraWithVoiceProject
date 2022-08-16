package com.example.camerawithvoiceapp.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

public class RecordingThread extends Thread{

    private boolean mShouldContinue = true;
    private static final int SAMPLING_RATE = 44100;
    private int bufferSize;
    private Context mContext;
    private short[] audioBuffer;
    private WaveSoundView waveSoundView;

    public RecordingThread(Context mContext,WaveSoundView waveSoundView) {
        this.mContext = mContext;
        this.waveSoundView = waveSoundView;
        bufferSize = AudioRecord.getMinBufferSize(SAMPLING_RATE, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        audioBuffer = new short[bufferSize / 2];
    }

    @Override
    public void run() {
        super.run();
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(mContext, "Необходмио разрешение для работы с микрофоном!", Toast.LENGTH_SHORT).show();
        }

        AudioRecord record = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLING_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );
        record.startRecording();
        while(mShouldContinue){
            record.read(audioBuffer,0,bufferSize/2);
            waveSoundView.updateAudioData(audioBuffer);
        }

        record.stop();
        record.release();
    }
    private synchronized boolean shouldContinue() {
        return mShouldContinue;
    }

    public synchronized void stopRunning() {
        mShouldContinue = false;
    }
}
