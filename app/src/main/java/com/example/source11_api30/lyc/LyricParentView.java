package com.example.source11_api30.lyc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.source11_api30.R;

import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class LyricParentView extends ConstraintLayout {

    //抬手后5秒延迟去隐藏seek指导线UI
    public static final int UP_HIDE_DELAY = 3000;

    private ImageView mIvPlay;
    private View mViewLine;
    private TextView mTvGuideTime;
    private LyricView mLyricView;
    private OnSeekToGuideLineListener mOnSeekToGuideLineListener;

    public LyricParentView(@NonNull Context context) {
        super(context);
        init();
    }

    public LyricParentView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricParentView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_lyric_parent, this, true);

        mIvPlay = findViewById(R.id.iv_play);
        mViewLine = findViewById(R.id.view_line);
        mTvGuideTime = findViewById(R.id.tv_guide_time);
        mLyricView = findViewById(R.id.lyc_view);

        mLyricView.setOnGuideLineChangeListener(new LyricView.OnGuideLineChangeListener() {
            @Override
            public void onGuideLineChange(Lyric lyric) {
                if (lyric != null) {
                    mTvGuideTime.setText(formatTime(lyric.getStartTime()));
                }
            }
        });

        mIvPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                removeCallbacks(mHideGuideLineRunnable);
                hideGuideLine();
                if (mOnSeekToGuideLineListener != null) {
                    mOnSeekToGuideLineListener.seekToGuideLine(mLyricView.getGuideLineStartTime());
                }
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mLyricView.onParentViewTouch(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                removeCallbacks(mHideGuideLineRunnable);
                showGuideLine();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                postDelayed(mHideGuideLineRunnable, UP_HIDE_DELAY);
            }
            break;
            default:
        }

        return true;
    }

    private final Runnable mHideGuideLineRunnable = new Runnable() {
        @Override
        public void run() {
            hideGuideLine();
        }
    };

    private void showGuideLine() {
        mIvPlay.setVisibility(View.VISIBLE);
        mViewLine.setVisibility(View.VISIBLE);
        mTvGuideTime.setVisibility(View.VISIBLE);
    }

    private void hideGuideLine() {
        mIvPlay.setVisibility(View.GONE);
        mViewLine.setVisibility(View.GONE);
        mTvGuideTime.setVisibility(View.GONE);
        mLyricView.whenGuideLineGone();
    }

    public void setData(@NonNull List<Lyric> lyricList) {
        mLyricView.setData(lyricList);
    }

    public void updateTime(@IntRange(from = 0) int currentPosition) {
        mLyricView.updateTime(currentPosition);
    }

    public void release(){
        removeCallbacks(mHideGuideLineRunnable);
        mLyricView.release();
    }

    private String formatTime(long time) {
        int min = (int) (time / 60000);
        int sec = (int) (time / 1000 % 60);
        return adjustFormat(min) + ":" + adjustFormat(sec);
    }

    private String adjustFormat(int time) {
        if (time < 10) {
            return "0" + time;
        }
        return time + "";
    }

    public void setOnSeekToGuideLineListener(OnSeekToGuideLineListener onSeekToGuideLineListener) {
        this.mOnSeekToGuideLineListener = onSeekToGuideLineListener;
    }

    public interface OnSeekToGuideLineListener {
        void seekToGuideLine(int startTime);
    }
}
