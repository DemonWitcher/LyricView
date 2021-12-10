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
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.OverScroller;

import com.example.source11_api30.L;

import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LyricView extends View {

    //惯性滑动的速度
    public static final int INERTIA_VELOCITY = 1000;
    //第一行歌词上下居中往上偏的距离
    public static final int FIRST_LINE_OFFSET = 60;
    //字体大小
    public static final int TV_SIZE = 45;
    //句间距
    public static final int LYC_SPACE = 45;
    //歌词绘制时左右padding 左右各留出这个距离的空白
    public static final int PADDING_LEFT_RIGHT = 30;
    //切换下一句的过渡时间
    public static final int AUTO_CHANGE_LINE_TIME = 1500;
    //松手后滚动到指导线位置的时间
    public static final int UP_TOUCH_GUIDE_CHANGE_LINE_TIME = 500;

    private final SparseArray<StaticLayout> mStaticLayoutMap = new SparseArray<>();

    private List<Lyric> mLyricList;

    private OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private TextPaint mPaintNormal;

    private int mTvSize;

    private int mNormalColor, mCurrentColor, mGuideColor;

    //当前歌词行数
    private int mCurrentLine;
    //指导线的当前行数
    private int mGuideLine;
    //用户是否触控中
    private boolean mInUserTouch;
    //缓存的歌词总高度
    private int mLycHeight;

    private OnGuideLineChangeListener mOnGuideLineChangeListener;

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
        mVelocityTracker = VelocityTracker.obtain();

        mPaintNormal = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintNormal.setColor(Color.GRAY);
        mPaintNormal.setTextSize(mTvSize);

        mNormalColor = Color.GRAY;
        mCurrentColor = Color.WHITE;
        mGuideColor = Color.parseColor("#653EAB");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mLyricList == null || mLyricList.size() == 0) {
            return;
        }
        canvas.translate(PADDING_LEFT_RIGHT, getHeight() / 2f - FIRST_LINE_OFFSET);

        int size = mLyricList.size();
        StaticLayout staticLayout;
        for (int i = 0; i < size; ++i) {
            if (i == mGuideLine && mInUserTouch) {
                mPaintNormal.setColor(mGuideColor);
            } else if (i == mCurrentLine) {
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
            int width = getWidth() - PADDING_LEFT_RIGHT - PADDING_LEFT_RIGHT;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                staticLayout = StaticLayout.Builder.obtain(srtLyc, 0, srtLyc.length(), mPaintNormal, width)
                        .setAlignment(Layout.Alignment.ALIGN_CENTER).build();
            } else {
                staticLayout = new StaticLayout(srtLyc, mPaintNormal, width, Layout.Alignment.ALIGN_CENTER,
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
            if (mInUserTouch) {
                removeCallbacks(mScrollToGuideLineRunnable);
                checkGuideLine();
                if (mOverScroller.isFinished()) {
                    scrollToGuideLine();
                }
            }
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

    private int getLineOffset(int targetLine) {
        int offset = 0;
        for (int i = 0; i < targetLine; ++i) {
            offset = offset + mStaticLayoutMap.get(i).getHeight() + LYC_SPACE;
        }
        return offset - getScrollY();
    }

    private final Runnable mChangeLineRunnable = new Runnable() {
        @Override
        public void run() {
            if (!mOverScroller.isFinished()) {
                mOverScroller.abortAnimation();
            }
            int dy = getLineOffset(mCurrentLine);
            if (dy == 0) {
                return;
            }
            int currentScrollY = getScrollY();
            L.i("currentScrollY:" + currentScrollY + ",dy:" + dy);
            mOverScroller.startScroll(0, currentScrollY, 0, dy, AUTO_CHANGE_LINE_TIME);
            invalidate();
        }
    };

    private float mDownY;
    private int mDownScrollY;

    public void onParentViewTouch(MotionEvent event) {
        mVelocityTracker.addMovement(event);
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                removeCallbacks(mScrollToGuideLineRunnable);
                mInUserTouch = true;
                mGuideLine = mCurrentLine;
                mDownY = event.getY();
                mDownScrollY = getScrollY();
                notifyGuideLineTime();
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                int moveOffset = (int) (event.getY() - mDownY);
                int scrollToY = mDownScrollY - moveOffset;
                scrollToY = Math.max(0, scrollToY);
                if (mLycHeight == 0) {
                    mLycHeight = getLycHeight();
                }
                scrollToY = Math.min(mLycHeight, scrollToY);
                scrollTo(0, scrollToY);
                if (!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }
                checkGuideLine();
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                //先处理惯性
                mVelocityTracker.computeCurrentVelocity(INERTIA_VELOCITY);
                float yVelocity = mVelocityTracker.getYVelocity();
                mOverScroller.fling(0, getScrollY(), 0, -(int) yVelocity, 0, 0, 0, mLycHeight);
                mVelocityTracker.clear();
                postDelayed(mScrollToGuideLineRunnable, 50);
            }
            break;
            default:
        }
    }

    private final Runnable mScrollToGuideLineRunnable = new Runnable() {
        @Override
        public void run() {
            scrollToGuideLine();
        }
    };

    private void scrollToGuideLine() {
        //松手后滚动到指导线行数的中间
        int dy = getLineOffset(mGuideLine);
        if (dy == 0) {
            return;
        }
        int currentScrollY = getScrollY();
        L.i("currentScrollY:" + currentScrollY + ",dy:" + dy);
        mOverScroller.startScroll(0, currentScrollY, 0, dy, UP_TOUCH_GUIDE_CHANGE_LINE_TIME);
        invalidate();
    }

    private void notifyGuideLineTime() {
        if (mOnGuideLineChangeListener != null) {
            if (mGuideLine >= 0 && mGuideLine < mLyricList.size()) {
                mOnGuideLineChangeListener.onGuideLineChange(mLyricList.get(mGuideLine));
            }
        }
    }

    private int getLycHeight() {
        int size = mLyricList.size();
        int offset = 0;
        for (int i = 0; i < size; ++i) {
            offset = offset + mStaticLayoutMap.get(i).getHeight();
            if (i != size - 1) {
                offset = offset + LYC_SPACE;
            }
        }
        return offset;
    }

    private void checkGuideLine() {
        int scrollY = getScrollY();
        int size = mLyricList.size();
        int offset = 0;
        for (int i = 0; i < size; ++i) {
            offset = offset + mStaticLayoutMap.get(i).getHeight();
            if (offset >= scrollY) {
//                L.i("就这行 " + i + ",offset:" + offset + ",scrollY:" + scrollY);
                mGuideLine = i;
                invalidate();
                //指导线更新后 更新上层的时间
                notifyGuideLineTime();
                break;
            }
            offset = offset + LYC_SPACE;
        }
    }

    public void whenGuideLineGone() {
        mInUserTouch = false;
        //指导线隐藏后 需要更新一下位置
        post(mChangeLineRunnable);
    }

    public void setData(@NonNull List<Lyric> lyricList) {
        reset();
        this.mLyricList = lyricList;
    }

    public void release() {
        mVelocityTracker.recycle();
        if (!mOverScroller.isFinished()) {
            mOverScroller.abortAnimation();
        }
        removeCallbacks(mChangeLineRunnable);
    }

    public void updateTime(@IntRange(from = 0) int currentPosition) {
        if (mLyricList == null || mLyricList.size() == 0) {
            return;
        }
        int currentLine = findLineByTime(currentPosition);
//        L.i("findLine " + currentLine);
        if (currentLine == this.mCurrentLine) {
            return;
        }
        this.mCurrentLine = currentLine;
        if (mInUserTouch) {
            return;
        }
        post(mChangeLineRunnable);
    }

    public int getGuideLineStartTime() {
        if (mLyricList != null && mGuideLine >= 0 && mGuideLine < mLyricList.size()) {
            Lyric lyric = mLyricList.get(mGuideLine);
            return (int) lyric.getStartTime();
        }
        return 0;
    }

    public void setOnGuideLineChangeListener(OnGuideLineChangeListener onGuideLineChangeListener) {
        this.mOnGuideLineChangeListener = onGuideLineChangeListener;
    }

    public interface OnGuideLineChangeListener {
        void onGuideLineChange(Lyric lyric);
    }

}
