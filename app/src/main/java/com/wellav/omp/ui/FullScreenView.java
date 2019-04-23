package com.wellav.omp.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class FullScreenView extends VideoView {

	public FullScreenView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FullScreenView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FullScreenView(Context context) {
		super(context);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		int width = getDefaultSize(0, widthMeasureSpec);
		int height = getDefaultSize(0, heightMeasureSpec);
		setMeasuredDimension(height*16/9, height);
	}
}