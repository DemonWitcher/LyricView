package com.example.source11_api30.lyc;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.source11_api30.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class TouchSeekView extends ConstraintLayout {

    private ImageView mIvPlay;
    private TextView mTvProgress;

    public TouchSeekView(@NonNull Context context) {
        super(context);
        init();
    }

    public TouchSeekView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchSeekView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        LayoutInflater.from(getContext()).inflate(R.layout.view_touch_seek,this,true);

        mIvPlay = findViewById(R.id.iv_play);
        mTvProgress = findViewById(R.id.tv_progress);
        mIvPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
