package com.example.source11_api30.lyc;

import android.content.Context;
import android.content.res.TypedArray;
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

import com.example.source11_api30.R;

import java.util.List;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LyricView extends View {

    //默认句颜色
    public static final int NORMAL_COLOR = Color.GRAY;
    //当前句颜色
    public static final int CURRENT_COLOR = Color.WHITE;
    //指导线指句行颜色
    public static final int GUIDE_COLOR = Color.parseColor("#653EAB");

    //惯性滑动的力度
    public static final int INERTIA_VELOCITY = 500;
    //第一行歌词上下居中往上偏的距离
    public static final int FIRST_LINE_OFFSET = 20;
    //字体大小
    public static final int TV_SIZE = 15;
    //句间距
    public static final int LYC_SPACE = 15;
    //歌词绘制时左右padding 左右各留出这个距离的空白
    public static final int PADDING_LEFT_RIGHT = 10;
    //切换下一句的过渡时间
    public static final int AUTO_CHANGE_LINE_TIME = 1500;
    //松手后滚动到指导线位置的时间
    public static final int UP_TOUCH_GUIDE_CHANGE_LINE_TIME = 500;

    private final SparseArray<StaticLayout> mStaticLayoutMap = new SparseArray<>();

    private List<Lyric> mLyricList;

    private OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private TextPaint mPaintNormal;

    //松手后滚动到指导线位置的时间
    private int mUpTouchGuideChangeLineTime;
    //切换下一句的过渡时间
    private int mAutoChangeLineTime;
    //字体大小
    private int mTvSize;
    //默认句颜色，当前句颜色，指导线指句行颜色
    private int mNormalColor, mCurrentColor, mGuideColor;
    //句间距
    private int mLycSpace;
    //歌词绘制时左右padding 左右各留出这个距离的空白
    private int mPaddingLeftRight;
    //第一行歌词上下居中往上偏的距离
    private int mFirstLineOffset;
    //当前歌词行数
    private int mCurrentLine;
    //指导线的当前行数
    private int mGuideLine = -1;
    //用户是否触控中
    private boolean mInUserTouch;
    //缓存的歌词总高度
    private int mLycHeight;
    //是否暂停中
    private boolean mIsPause = true;

    private OnGuideLineChangeListener mOnGuideLineChangeListener;

    public LyricView(Context context) {
        super(context);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LyricView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LyricView);
        mUpTouchGuideChangeLineTime = typedArray.getInteger(R.styleable.LyricView_up_touch_guide_change_line_time, UP_TOUCH_GUIDE_CHANGE_LINE_TIME);
        mAutoChangeLineTime = typedArray.getInteger(R.styleable.LyricView_auto_change_line_time, AUTO_CHANGE_LINE_TIME);
        mTvSize = typedArray.getDimensionPixelSize(R.styleable.LyricView_lyc_size, dip2px(getContext(), TV_SIZE));
        mLycSpace = typedArray.getDimensionPixelSize(R.styleable.LyricView_lyc_space, dip2px(getContext(), LYC_SPACE));
        mFirstLineOffset = typedArray.getDimensionPixelSize(R.styleable.LyricView_first_line_offset, dip2px(getContext(), FIRST_LINE_OFFSET));
        mPaddingLeftRight = typedArray.getDimensionPixelSize(R.styleable.LyricView_padding_left_right, dip2px(getContext(), PADDING_LEFT_RIGHT));
        mNormalColor = typedArray.getColor(R.styleable.LyricView_normal_color, NORMAL_COLOR);
        mCurrentColor = typedArray.getColor(R.styleable.LyricView_current_color, CURRENT_COLOR);
        mGuideColor = typedArray.getColor(R.styleable.LyricView_guide_color, GUIDE_COLOR);
        typedArray.recycle();

        mOverScroller = new OverScroller(getContext());
        mVelocityTracker = VelocityTracker.obtain();

        mPaintNormal = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mPaintNormal.setColor(mNormalColor);
        mPaintNormal.setTextSize(mTvSize);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isLycEmpty()) {
            return;
        }
        canvas.translate(mPaddingLeftRight, getHeight() / 2f - mFirstLineOffset);

        int size = mLyricList.size();
        StaticLayout staticLayout;
        for (int i = 0; i < size; ++i) {
            if (i == mGuideLine && (mInUserTouch || mIsPause)) {
                mPaintNormal.setColor(mGuideColor);
            } else if (i == mCurrentLine) {
                mPaintNormal.setColor(mCurrentColor);
            } else {
                mPaintNormal.setColor(mNormalColor);
            }
            staticLayout = getStaticLayout(i);
            staticLayout.draw(canvas);
            canvas.translate(0, staticLayout.getHeight() + mLycSpace);
        }
    }

    private StaticLayout getStaticLayout(int lyricPosition) {
        StaticLayout staticLayout = mStaticLayoutMap.get(lyricPosition);
        if (staticLayout == null) {
            Lyric lyric = mLyricList.get(lyricPosition);
            String srtLyc = lyric.getDesc();
            int width = getWidth() - mPaddingLeftRight - mPaddingLeftRight;
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
        removeCallbacks(mChangeLineRunnable);
        removeCallbacks(mScrollToGuideLineRunnable);
        mCurrentLine = 0;
        mGuideLine = -1;
        mLycHeight = 0;
        mStaticLayoutMap.clear();
        mIsPause = true;
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
            offset = offset + getStaticLayout(i).getHeight() + mLycSpace;
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
//            L.i("currentScrollY:" + currentScrollY + ",dy:" + dy);
            mOverScroller.startScroll(0, currentScrollY, 0, dy, mAutoChangeLineTime);
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
                mInUserTouch = true;
                mDownY = event.getY();
                mDownScrollY = getScrollY();
                if (!mOverScroller.isFinished()) {
                    mOverScroller.abortAnimation();
                }
                removeCallbacks(mScrollToGuideLineRunnable);
                removeCallbacks(mChangeLineRunnable);
                checkGuideLine();
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
//                L.i("yVelocity:" + yVelocity);
                mOverScroller.fling(0, getScrollY(), 0, -(int) yVelocity, 0, 0, 0, mLycHeight);
                invalidate();
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
//        L.i("scrollToGuideLine");
        //松手后滚动到指导线行数的中间
        int dy = getLineOffset(mGuideLine);
        if (dy == 0) {
            return;
        }
        int currentScrollY = getScrollY();
//        L.i("currentScrollY:" + currentScrollY + ",dy:" + dy);
        mOverScroller.startScroll(0, currentScrollY, 0, dy, mUpTouchGuideChangeLineTime);
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
            offset = offset + getStaticLayout(i).getHeight();
            if (i != size - 1) {
                offset = offset + mLycSpace;
            }
        }
        return offset;
    }

    private void checkGuideLine() {
        if(isLycEmpty()){
            return;
        }
        int scrollY = getScrollY();
        int size = mLyricList.size();
        int offset = 0;
        for (int i = 0; i < size; ++i) {
            offset = offset + getStaticLayout(i).getHeight();
            if (offset >= scrollY) {
                mGuideLine = i;
                invalidate();
                //指导线更新后 更新上层的时间
                notifyGuideLineTime();
                break;
            }
            offset = offset + mLycSpace;
        }
    }

    public boolean isLycEmpty() {
        return mLyricList == null || mLyricList.size() == 0;
    }

    public void whenGuideLineGone() {
        mInUserTouch = false;
        invalidate();
        if (!mIsPause) {
            //播放中 指导线隐藏后 需要更新一下位置
            mGuideLine = -1;
            post(mChangeLineRunnable);
        }
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
        removeCallbacks(mScrollToGuideLineRunnable);
    }

    public void updateTime(@IntRange(from = 0) int currentPosition) {
        if (isLycEmpty()) {
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

    public void pause() {
        mIsPause = true;
    }

    public void start() {
        mIsPause = false;
    }

    public void setOnGuideLineChangeListener(OnGuideLineChangeListener onGuideLineChangeListener) {
        this.mOnGuideLineChangeListener = onGuideLineChangeListener;
    }

    public interface OnGuideLineChangeListener {
        void onGuideLineChange(Lyric lyric);
    }

    public int getUpTouchGuideChangeLineTime() {
        return mUpTouchGuideChangeLineTime;
    }

    public void setUpTouchGuideChangeLineTime(int upTouchGuideChangeLineTime) {
        this.mUpTouchGuideChangeLineTime = upTouchGuideChangeLineTime;
    }

    public int getAutoChangeLineTime() {
        return mAutoChangeLineTime;
    }

    public void setAutoChangeLineTime(int autoChangeLineTime) {
        this.mAutoChangeLineTime = autoChangeLineTime;
    }

    public int getTvSize() {
        return mTvSize;
    }

    public void setTvSize(int tvSize) {
        this.mTvSize = tvSize;
    }

    public int getNormalColor() {
        return mNormalColor;
    }

    public void setNormalColor(int normalColor) {
        this.mNormalColor = normalColor;
    }

    public int getCurrentColor() {
        return mCurrentColor;
    }

    public void setCurrentColor(int currentColor) {
        this.mCurrentColor = currentColor;
    }

    public int getGuideColor() {
        return mGuideColor;
    }

    public void setGuideColor(int guideColor) {
        this.mGuideColor = guideColor;
    }

    public int getLycSpace() {
        return mLycSpace;
    }

    public void setLycSpace(int lycSpace) {
        this.mLycSpace = lycSpace;
    }

    public int getPaddingLeftRight() {
        return mPaddingLeftRight;
    }

    public void setPaddingLeftRight(int paddingLeftRight) {
        this.mPaddingLeftRight = paddingLeftRight;
    }

    public int getFirstLineOffset() {
        return mFirstLineOffset;
    }

    public void setFirstLineOffset(int firstLineOffset) {
        this.mFirstLineOffset = firstLineOffset;
    }

    public static int dip2px(Context context, float dipValue) {
        if (null == context) {
            return 0;
        }
        final float scaleValue = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scaleValue + 0.5f);
    }
}
