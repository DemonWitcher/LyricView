package com.example.source11_api30;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import java.util.Random;

import androidx.annotation.Nullable;

public class MyTev extends androidx.appcompat.widget.AppCompatTextView {
    public MyTev(Context context) {
        super(context);
    }

    public MyTev(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTev(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int ri = new Random().nextInt(10000);
        setText(String.valueOf(ri));
        L.i("ri:"+ri);
        invalidate();
    }
}
