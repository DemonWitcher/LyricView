package com.example.source11_api30.lyc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.OverScroller;

import com.example.source11_api30.L;

import java.util.List;

import androidx.annotation.Nullable;

/**
 * 1.支持seek
 * 2.按下显示定位条
 */
public class LyricView extends View {

    //字体大小
    public static final int TV_SIZE = 45;
    //句间距
    public static final int LYC_SPACE = 45;
    //歌词绘制时左右padding 左右各留出这个距离的空白
    public static final int PADDING_LEFT_RIGHT = 30;
    //切换下一句的过渡时间
    public static final int CHANGE_TIME = 1500;

    private final SparseArray<StaticLayout> mStaticLayoutMap = new SparseArray<>();

    private List<Lyric> mLyricList;

    private OverScroller mOverScroller;
    private TextPaint mPaintNormal;

    private int mTvSize;

    private int mNormalColor, mCurrentColor;

    //当前歌词行数
    private int mCurrentLine;

    public LyricView(Context context) {
        super(context);
        init();
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTvSize = TV_SIZE;
        mOverScroller = new OverScroller(getContext());

        mPaintNormal = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintNormal.setColor(Color.GRAY);
        mPaintNormal.setTextSize(mTvSize);

        mNormalColor = Color.GRAY;
        mCurrentColor = Color.WHITE;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyricList == null || mLyricList.size() == 0) {
            return;
        }
        canvas.translate(PADDING_LEFT_RIGHT, getHeight() / 2f);

        int size = mLyricList.size();
        StaticLayout staticLayout;
        for (int i = 0; i < size; ++i) {
            if (i == mCurrentLine) {
                mPaintNormal.setColor(mCurrentColor);
            } else {
                mPaintNormal.setColor(mNormalColor);
            }
            staticLayout = getStaticLayout(i);
            staticLayout.draw(canvas);
            canvas.translate(0, staticLayout.getHeight() + LYC_SPACE);
        }
    }

    private StaticLayout getStaticLayout(int lyricPosition) {
        StaticLayout staticLayout = mStaticLayoutMap.get(lyricPosition);
        if (staticLayout == null) {
            Lyric lyric = mLyricList.get(lyricPosition);
            String srtLyc = lyric.getDesc();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                staticLayout = StaticLayout.Builder.obtain(srtLyc, 0, srtLyc.length(), mPaintNormal, getWidth() - PADDING_LEFT_RIGHT - PADDING_LEFT_RIGHT)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER).build();
            } else {
                staticLayout = new StaticLayout(srtLyc, mPaintNormal, getWidth() - PADDING_LEFT_RIGHT - PADDING_LEFT_RIGHT, Layout.Alignment.ALIGN_CENTER,
                        1.0f, 0.0f, true);
            }
            mStaticLayoutMap.put(lyricPosition, staticLayout);
        }
        return staticLayout;
    }

    @Override
    public void computeScroll() {
        if (mOverScroller.computeScrollOffset()) {
            scrollTo(0, mOverScroller.getCurrY());
            invalidate();
        }
    }

    private void reset() {
        mCurrentLine = 0;
    }

    private int findLineByTime(int currentPosition) {
        int size = mLyricList.size();
        Lyric lyc;
        for (int i = 0; i < size; ++i) {
            lyc = mLyricList.get(i);
            if (currentPosition < lyc.getNextTime()) {
                return i;
            }
        }
        return size - 1;
    }

    private int getLineOffset() {
        int offset = 0;
        for (int i = 0; i < mCurrentLine; ++i) {
            offset = offset + mStaticLayoutMap.get(i).getHeight() + LYC_SPACE;
        }
        return offset - getScrollY();
    }

    private final Runnable changeLineRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mOverScroller.isFinished()) {
                mOverScroller.abortAnimation();
            }
            int dy = getLineOffset();
            int currentScrollY = getScrollY();
            L.i("currentScrollY:" + currentScrollY + ",dy:" + dy);
            mOverScroller.startScroll(0, currentScrollY, 0, dy, CHANGE_TIME);
            invalidate();
        }
    };

    public void setData(List<Lyric> lyricList) {
        reset();
        this.mLyricList = lyricList;
    }

    public void updateTime(int currentPosition) {
        if (mLyricList == null || mLyricList.size() == 0) {
            return;
        }
        int currentLine = findLineByTime(currentPosition);
        L.i("findLine " + currentLine);
        if (currentLine == this.mCurrentLine) {
            return;
        }
        this.mCurrentLine = currentLine;
        post(changeLineRunnable);
    }

}
