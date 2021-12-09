package com.example.source11_api30;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * -------------------------------------
 * 作者：wushi
 * -------------------------------------
 * 时间： 2021/10/28 11:01 AM
 * -------------------------------------
 * 备注：
 * 动画过程
 * 1.从底部向上位移，到某个高度后换红包图，然后再向下回弹一点
 * 2.整体左右抖动
 * 3.流光透明度出来同时开始旋转，旋转最后阶段透明度隐藏
 * 4.循环234
 * 5.点击后整体透明度消失
 * -------------------------------------
 */
public class SmallRedPacketView extends ConstraintLayout {

    /**
     * 红包view
     */
    private ImageView mIvRedPacket;

    /**
     * 流光view
     */
    private ImageView mIvLight;

    /**
     * 整体处理动效所需根节点view
     */
    private ViewGroup mViewRoot;

    /**
     * 是否已经将红包资源从模糊替换为正常
     */
    private boolean mHaveReplaceNormalIcon;

    /**
     * 初始化时整体的TranslationY
     */
    private int mInitTranslationY;

    /**
     * 刚出场时向上的额外位移距离，用于回弹效果
     */
    private int mMoreTopTranslationY;

    /**
     * 红包抖动动画
     */
    private ObjectAnimator mShakeAnimator;

    /**
     * 流光出现动画
     */
    private ObjectAnimator mLightShowAnimator;

    /**
     * 流光旋转动画
     */
    private ObjectAnimator mLightRotationAnimator;

    /**
     * 流光隐藏动画
     */
    private ObjectAnimator mLightHideAnimator;

    /**
     * 红包退场动画
     */
    private ObjectAnimator mRedPacketHideAnimator;

    /**
     * 红包入场动画
     */
    private ValueAnimator mRedPacketShowAnimator;

    /**
     * 流光动画集
     */
    private AnimatorSet mLightShowAnimatorSet;

    /**
     * 线性变速器 用于所有动效过程
     */
    private LinearInterpolator mLinearInterpolator;

    public SmallRedPacketView(@NonNull Context context) {
        super(context);
        init();
    }

    public SmallRedPacketView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmallRedPacketView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_small_red_packet, this, true);
        mViewRoot = findViewById(R.id.view_root);
        mIvRedPacket = findViewById(R.id.iv_red_packet);
        mIvLight = findViewById(R.id.iv_light);

        mInitTranslationY = dip2px(getContext(), 54);
        mMoreTopTranslationY = dip2px(getContext(), -5);

        mLinearInterpolator = new LinearInterpolator();

        resetAnimValue();
    }

    /**
     * 重置红包view为初始属性
     */
    private void resetAnimValue() {
        mIvRedPacket.setImageResource(R.drawable.icon_live_red_packet_blur);
        mViewRoot.setTranslationY(mInitTranslationY);
        mViewRoot.setAlpha(1f);
        mIvLight.setAlpha(0f);
        mHaveReplaceNormalIcon = false;
    }

    /**
     * 小红包出场
     */
    public void showRedPacket() {
        resetAnimValue();

        if (mRedPacketShowAnimator == null) {
            mRedPacketShowAnimator = new ValueAnimator();
            mRedPacketShowAnimator.setIntValues(mInitTranslationY, mMoreTopTranslationY, 0);
            mRedPacketShowAnimator.setDuration(500);
            mRedPacketShowAnimator.setInterpolator(mLinearInterpolator);
            mRedPacketShowAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    mViewRoot.setTranslationY(value);
                    if (value <= 0 && !mHaveReplaceNormalIcon) {
                        //更换图片
                        mHaveReplaceNormalIcon = true;
                        mIvRedPacket.setImageResource(R.drawable.icon_live_red_packet_normal);
                    }
                }
            });
            mRedPacketShowAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shakeRedPacket();
                }
            });
        }
        mRedPacketShowAnimator.start();
    }

    /**
     * 红包抖动
     */
    private void shakeRedPacket() {
        if (mShakeAnimator == null) {
            mShakeAnimator = ObjectAnimator.ofFloat(mViewRoot, "rotation", 0, -7, 7, -7, 7, 0);
            mShakeAnimator.setDuration(800);
            mShakeAnimator.setInterpolator(mLinearInterpolator);
            mShakeAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showLight();
                }
            });
        }
        mShakeAnimator.start();
    }

    /**
     * 中间的流光出现
     */
    private void showLight() {
        //流光透明度出场
        if (mLightShowAnimator == null) {
            mLightShowAnimator = ObjectAnimator.ofFloat(mIvLight, "alpha", 0f, 1f);
            mLightShowAnimator.setDuration(300);
        }
        //流光开始旋转
        if (mLightRotationAnimator == null) {
            mLightRotationAnimator = ObjectAnimator.ofFloat(mIvLight, "rotation", 0, 720);
            mLightRotationAnimator.setDuration(5000);
        }
        //流光透明度隐藏
        if (mLightHideAnimator == null) {
            mLightHideAnimator = ObjectAnimator.ofFloat(mIvLight, "alpha", 1f, 0f);
            mLightHideAnimator.setDuration(300);
            mLightHideAnimator.setStartDelay(4700);
        }
        if (mLightShowAnimatorSet == null) {
            mLightShowAnimatorSet = new AnimatorSet();
            mLightShowAnimatorSet.setInterpolator(mLinearInterpolator);
            mLightShowAnimatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    shakeRedPacket();
                }
            });
            mLightShowAnimatorSet.playTogether(mLightShowAnimator, mLightRotationAnimator, mLightHideAnimator);
        }
        mLightShowAnimatorSet.start();
    }

    /**
     * 小红包退场
     */
    public void hideRedPacket() {
        if (mRedPacketHideAnimator == null) {
            mRedPacketHideAnimator = ObjectAnimator.ofFloat(mViewRoot, "alpha", 1f, 0f);
            mRedPacketHideAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    releaseAnimator();
                }
            });
            mRedPacketHideAnimator.setInterpolator(mLinearInterpolator);
            mRedPacketHideAnimator.setDuration(200);
        }
        mRedPacketHideAnimator.start();
    }

    /**
     * 停止所有动画
     */
    private void releaseAnimator() {
        if (mShakeAnimator != null) {
            mShakeAnimator.removeAllListeners();
            mShakeAnimator.cancel();
            mShakeAnimator = null;
        }
        if (mLightShowAnimator != null) {
            mLightShowAnimator.removeAllListeners();
            mLightShowAnimator.cancel();
            mLightShowAnimator = null;
        }
        if (mLightRotationAnimator != null) {
            mLightRotationAnimator.removeAllListeners();
            mLightRotationAnimator.cancel();
            mLightRotationAnimator = null;
        }
        if (mLightHideAnimator != null) {
            mLightHideAnimator.removeAllListeners();
            mLightHideAnimator.cancel();
            mLightHideAnimator = null;
        }
        if (mRedPacketHideAnimator != null) {
            mRedPacketHideAnimator.removeAllListeners();
            mRedPacketHideAnimator.cancel();
            mRedPacketHideAnimator = null;
        }
        if (mLightShowAnimatorSet != null) {
            mLightShowAnimatorSet.removeAllListeners();
            mLightShowAnimatorSet.cancel();
            mLightShowAnimatorSet = null;
        }
        if (mRedPacketShowAnimator != null) {
            mRedPacketShowAnimator.removeAllListeners();
            mRedPacketShowAnimator.removeAllUpdateListeners();
            mRedPacketShowAnimator.cancel();
            mRedPacketShowAnimator = null;
        }
    }

    public void pauseAnim(){
        if (mShakeAnimator != null&&mShakeAnimator.isRunning()) {
            mShakeAnimator.pause();
        }
        if (mRedPacketHideAnimator != null&&mRedPacketHideAnimator.isRunning()) {
            mRedPacketHideAnimator.pause();
        }
        if (mLightShowAnimatorSet != null&&mLightShowAnimatorSet.isRunning()) {
            mLightShowAnimatorSet.pause();
        }
        if (mRedPacketShowAnimator != null&&mRedPacketShowAnimator.isRunning()) {
            mRedPacketShowAnimator.pause();
        }
    }

    public void resumeAnim(){
        if (mShakeAnimator != null&&mShakeAnimator.isPaused()) {
            mShakeAnimator.resume();
        }
        if (mRedPacketHideAnimator != null&&mRedPacketHideAnimator.isPaused()) {
            mRedPacketHideAnimator.resume();
        }
        if (mLightShowAnimatorSet != null&&mLightShowAnimatorSet.isPaused()) {
            mLightShowAnimatorSet.resume();
        }
        if (mRedPacketShowAnimator != null&&mRedPacketShowAnimator.isPaused()) {
            mRedPacketShowAnimator.resume();
        }
    }

    public static int dip2px(Context context, float dipValue) {
        if (null == context) {
            return 0;
        }
        final float scaleValue = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scaleValue + 0.5f);
    }
}
