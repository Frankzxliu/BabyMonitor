package com.frank.babymonitor.babyalarm;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 *  Capture the sound level from microphones
 */
public class MonitorService extends Service{

    private final String TAG = "monitorService";

    protected static Handler monitorServiceHanlder = null;
    protected static boolean running = false;
    protected static Boolean isServiceRunning = false;
    private int frequency = 22050;
    private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int blockSize = 512;
    private double[] toTransform = new double[blockSize];
    private RealDoubleFFT transformer;
    private double sensitivityLimit = 3.5;
    private int currentVolumeLimit = 40;
    private boolean recording;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Monitor Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            running = true;

            sensitivityLimit = intent.getDoubleExtra("sensitivity", 25);

            sensitivityLimit = (sensitivityLimit + 10) / 10;

            Log.d(TAG, "current sensitivity limit " + sensitivityLimit);

            transformer = new RealDoubleFFT(blockSize);
            recording = true;
            isServiceRunning = true;
            RecordThread recordThread = new RecordThread();
            recordThread.start();
            Toast.makeText(this, "Congrats! My Service Started", Toast.LENGTH_LONG).show();
            return START_STICKY;
        }
        else {
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isServiceRunning = false;
        Log.d(TAG,"Monitor Service stopped");
    }

    class RecordThread extends Thread{

        @Override
        public void run() {
            super.run();
            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
            AudioRecord audioRecord = new AudioRecord(
                    MediaRecorder.AudioSource.MIC, frequency,
                    channelConfiguration, audioEncoding, bufferSize);

            short[] buffer = new short[blockSize];

            audioRecord.startRecording();

            while (recording) {
                Log.d(TAG, "recording");
                int bufferReadResult = audioRecord.read(buffer, 0,blockSize);

                // Test current loudness
                double mean;
                long sum = 0;
                double currentVolume;
                for(int i = 0; i< buffer.length;i++)
                {
                    sum = sum + buffer[i]*buffer[i];
                }
                mean = sum/(double)bufferReadResult;
                currentVolume = 10 * Math.log10(mean);

                // FFT transform
                for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                    toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                }
                transformer.ft(toTransform);

                Log.d(TAG, "outside current volume is " + currentVolume + " 89 " + toTransform[89] + " 90 " + toTransform[90] + " 91 " + toTransform[91]);

                if((currentVolume > currentVolumeLimit) && (toTransform[89]+toTransform[90]+toTransform[91]> sensitivityLimit )){
                    //call number or pass to activity

                    running = false;
                    recording = false;
                    if(null != BabyAlarmActivity.babyAlarmHandler)
                    {
                        Message msgToActivity = new Message();
                        msgToActivity.what = 0;
                        if(isServiceRunning)
                            msgToActivity.obj  = "Monitor Service is Running";
                        else
                            msgToActivity.obj  = "Monitor Service is not Running";

                        BabyAlarmActivity.babyAlarmHandler.sendMessage(msgToActivity);
                    }
                }
            }

            audioRecord.stop();
        }
    }

}
