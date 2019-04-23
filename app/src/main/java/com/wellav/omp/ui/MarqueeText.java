package com.wellav.omp.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

@SuppressLint("AppCompatCustomView")
public class MarqueeText extends TextView implements Runnable {
    private static final int MARQUEE_DELAY = 1000;
    private int currentScrollX; // 当前滚动的位置
    private int textWidth;
    private boolean isMeasure = false;
    private String s;
    private int marqueeSpeed = 3;

    public MarqueeText(Context context) {
        super(context);
    }

    public MarqueeText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isMeasure) {
            setText(s);
            currentScrollX = 0;
            textWidth = getTextWidth();// 文字宽度只需要获取一次就可以了
            if (textWidth < getWidth()) {
                setGravity(Gravity.CENTER);
            } else {
                setGravity(Gravity.NO_GRAVITY);
                scrollTo(currentScrollX, 0);
            }
            isMeasure = true;
        }
    }

    private int getTextWidth() {
        Paint paint = this.getPaint();
        String str = this.getText().toString();
        textWidth = (int) paint.measureText(str);
        return textWidth;
    }

    @Override
    public void run() {
        if (textWidth > this.getWidth()) {
            currentScrollX += marqueeSpeed;// 滚动速度.+号表示往左边-
            scrollTo(currentScrollX, 0);
        }
        if (getScrollX() >= (textWidth)) {
            currentScrollX = -(this.getWidth());// 当前出现的位置
        }
        postDelayed(this, 10);
    }

    public void setMarqueeSpeed(int velocity) {
        this.marqueeSpeed = velocity;
    }

    @Override
    public boolean isFocused() {
        return false;
    }

    @Override
    public boolean isSelected() {
        return false;
    }

    // 从头开始滚动
    public void startFromHead() {
        currentScrollX = 0;
        startScroll();
    }

    public void addMessage(String s) {
        setText(s);
        this.s = s;
        invalidate();
        this.removeCallbacks(this);
        setEllipsize(null);
        startScroll();
        isMeasure = false;
        setVisibility(VISIBLE);
        postDelayed(this, MARQUEE_DELAY);
    }

    private void startScroll() {
        isMeasure = false;
        this.removeCallbacks(this);
        this.invalidate();
    }
}
