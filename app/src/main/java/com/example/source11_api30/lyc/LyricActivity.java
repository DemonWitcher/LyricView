package com.example.source11_api30.lyc;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;

import com.example.source11_api30.L;
import com.example.source11_api30.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class LyricActivity extends AppCompatActivity {

    LyricView lyricView;
    SeekBar sb;
    private List<Lyric> radioCaptions;
    private MediaPlayer mediaPlayer;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyc);

        handler = new Handler(getMainLooper());
        initView();
        initLyc();
        initMediaPlayer();
    }

    private void initLyc() {
        Type type = new TypeToken<List<Lyric>>() {
        }.getType();
        radioCaptions = new Gson().fromJson(LyricData.lyc, type);
        lyricView.setData(radioCaptions);
    }

    private void initView() {
        lyricView = findViewById(R.id.lyc_view);
        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.start();
                updateTime();
            }
        });
        findViewById(R.id.bt_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.pause();
                handler.removeCallbacks(runnable);
            }
        });
        sb = findViewById(R.id.sb);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });
    }

    private void initMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.aaa);
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    L.i("onPrepared");
                    sb.setMax(mediaPlayer.getDuration());
                    sb.setProgress(mediaPlayer.getCurrentPosition());
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    L.i("error what:" + what + ",extra:" + extra);
                    return false;
                }
            });
            mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    L.i("onSeekComplete "+mediaPlayer.getCurrentPosition());
                }
            });

            mediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateTime() {
        handler.postDelayed(runnable, 50);
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            sb.setProgress(currentPosition);
            lyricView.updateTime(currentPosition);
            updateTime();
        }
    };

}
