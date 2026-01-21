package protectedwp.safespace;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

public class MediaPlayerActivity extends Activity {

    private static final int PICK_AUDIO_CODE = 101;
    
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private volatile boolean isRunning = true;
    private Thread updateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(40, 40, 40, 40);

        Button btnPick = new Button(this);
        btnPick.setText("Select media");
        btnPick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("audio/*");
                startActivityForResult(intent, PICK_AUDIO_CODE);
            }
        });

        seekBar = new SeekBar(this);
        LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 
                LinearLayout.LayoutParams.WRAP_CONTENT);
        seekParams.setMargins(0, 60, 0, 60);
        seekBar.setLayoutParams(seekParams);

        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);

        Button btnPlay = new Button(this);
        btnPlay.setText("Play");
        
        Button btnPause = new Button(this);
        btnPause.setText("Pause");

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null) mediaPlayer.start();
            }
        });

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) mediaPlayer.pause();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) mediaPlayer.seekTo(progress);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });

        row.addView(btnPlay);
        row.addView(btnPause);
        layout.addView(btnPick);
        layout.addView(seekBar);
        layout.addView(row);

        setContentView(layout);
        
        startSeekBarThread();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_AUDIO_CODE && resultCode == RESULT_OK && data != null) {
            setupAudio(data.getData());
        }
    }

    private void setupAudio(Uri uri) {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
            }

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build());
            
            mediaPlayer.setDataSource(this, uri);

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    seekBar.setMax(mp.getDuration());
                }
            });

            mediaPlayer.prepareAsync();

        } catch (Exception e) {
            Toast.makeText(this, "Ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void startSeekBarThread() {
        updateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(500);
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            final int current = mediaPlayer.getCurrentPosition();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    seekBar.setProgress(current);
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        });
        updateThread.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;
        if (updateThread != null) updateThread.interrupt();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
