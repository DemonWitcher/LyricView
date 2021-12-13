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

    private LyricParentView mLyricParentView;
    private SeekBar mSeekBar;
    private List<Lyric> mLycData;
    private MediaPlayer mMediaPlayer;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lyc);

        mHandler = new Handler(getMainLooper());
        initView();
        initLyc();
        initMediaPlayer();
    }

    private void initLyc() {
        Type type = new TypeToken<List<Lyric>>() {
        }.getType();
        mLycData = new Gson().fromJson(LyricData.lyc, type);
        mLyricParentView.setData(mLycData);
    }

    private void initView() {
        mLyricParentView = findViewById(R.id.lyc_parent_view);
        findViewById(R.id.bt_start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.start();
                mLyricParentView.start();
                updateTime();
            }
        });
        findViewById(R.id.bt_pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaPlayer.pause();
                mLyricParentView.pause();
                mHandler.removeCallbacks(runnable);
            }
        });
        mSeekBar = findViewById(R.id.sb);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
        mLyricParentView.setOnSeekToGuideLineListener(new LyricParentView.OnSeekToGuideLineListener() {
            @Override
            public void seekToGuideLine(int startTime) {
                if (mMediaPlayer != null) {
                    mMediaPlayer.seekTo(startTime);
                    if(!mMediaPlayer.isPlaying()){
                        mMediaPlayer.start();
                        updateTime();
                    }
                }
            }
        });
    }

    private void initMediaPlayer() {
        try {
            mMediaPlayer = MediaPlayer.create(this, R.raw.aaa);
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    L.i("onPrepared");
                    mSeekBar.setMax(mMediaPlayer.getDuration());
                    mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    L.i("error what:" + what + ",extra:" + extra);
                    return false;
                }
            });
            mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    L.i("onSeekComplete " + mMediaPlayer.getCurrentPosition());
                    mLyricParentView.updateTime(mMediaPlayer.getCurrentPosition());
                }
            });

            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void updateTime() {
        mHandler.postDelayed(runnable, 50);
    }

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mMediaPlayer.getCurrentPosition();
            mSeekBar.setProgress(currentPosition);
            mLyricParentView.updateTime(currentPosition);
            updateTime();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLyricParentView.release();
    }
}
