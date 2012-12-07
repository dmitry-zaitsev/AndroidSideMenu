/*
 * Copyright dmitry.zaicew@gmail.com Dmitry Zaitsev
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.agimind.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.FrameLayout;

public class SlideHolder extends FrameLayout {

	protected final static int MODE_READY = 0;
	protected final static int MODE_SLIDE = 1;
	protected final static int MODE_FINISHED = 2;
	
	private Bitmap mCachedBitmap;
	private Canvas mCachedCanvas;
	private View mTopView;
	
	private int mSlideMode = MODE_READY;
	
	private int mOffset = 0;
	private int mStartOffset;
	private int mEndOffset;
	
	private boolean mEnabled = true;
	
	private OnSlideListener mListener;
	
	public SlideHolder(Context context) {
		this(context, null, 0);
	}
	
	public SlideHolder(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public SlideHolder(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}
	
	@Override
	public boolean isEnabled() {
		return mEnabled;
	}
	
	public void setOnSlideListener(OnSlideListener lis) {
		mListener = lis;
	}
	
	public boolean isOpened() {
		return mSlideMode == MODE_FINISHED;
	}
	
	public void toggle() {
		if(isOpened()) {
			close();
		} else {
			open();
		}
	}
	
	public boolean open() {
		if(isOpened()) {
			return false;
		}
		
		startSlideMode();
		
		Animation anim = new SlideAnimation(mOffset, mEndOffset);
		anim.setAnimationListener(mOpenListener);
		startAnimation(anim);
		
		invalidate();
		
		return true;
	}
	
	public boolean close() {
		if(!isOpened()) {
			return false;
		}
		
		startSlideMode();
		
		Animation anim = new SlideAnimation(mOffset, mEndOffset);
		anim.setAnimationListener(mCloseListener);
		startAnimation(anim);
		
		invalidate();
		
		return true;
	}

	private byte mFrame = 0;
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		try {
			if(mSlideMode == MODE_READY) {
				getChildAt(1).draw(canvas);
			} else if(mSlideMode == MODE_SLIDE || mSlideMode == MODE_FINISHED) {
				if(++mFrame % 5 == 0) getChildAt(1).draw(mCachedCanvas);	//перерисовываем каждый 5 кадр
				
				getChildAt(0).draw(canvas);
				canvas.drawBitmap(mCachedBitmap, mOffset, 0, null);
			}
		} catch(IndexOutOfBoundsException e) {
			/*
			 * Ќа некоторых устройствах (в основном на телефонах Samsung) возможны
			 * вылеты при отрисовке пустого ListView.
			 * 
			 * Ќичего здесь не делаем.
			 */
		}
	}
	
	private int mHistoricalX = 0;
	
	private boolean mClosing = false;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if(!mEnabled) {
			return false;
		}
		
		mClosing = false;
		
		if(mSlideMode != MODE_FINISHED) {
			return onTouchEvent(ev);
		} else {
			Rect rect = new Rect();
			getChildAt(0).getHitRect(rect);
			
			if(!rect.contains((int) ev.getX(), (int) ev.getY())) {
				mClosing = true;
				return true;
			} else {
				return onTouchEvent(ev);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if(!mEnabled) {
			return false;
		}
		
		float x = ev.getX();
		
		if(ev.getAction() == MotionEvent.ACTION_DOWN) {
			mHistoricalX = (int) x;
			
			return mClosing;
		}
		
		if(ev.getAction() == MotionEvent.ACTION_MOVE) {
			float diff = x - mHistoricalX;
			if((diff > getWidth()/8 && mSlideMode == MODE_READY) || (diff < -getWidth()/8 && mSlideMode == MODE_FINISHED)) {
				mHistoricalX = (int) x;
				
				startSlideMode();
			} else if(mSlideMode == MODE_SLIDE) {
				mOffset += (int) x - mHistoricalX;
				
				mHistoricalX = (int) x;
				
				if(!isSlideAllowed()) {
					finishSlide();
				}
			} else {
				return false;
			}
			
			invalidate();
		}
		
		if(ev.getAction() == MotionEvent.ACTION_UP) {
			if(mSlideMode == MODE_SLIDE) {
				finishSlide();
			}
			
			invalidate();
			
			return false;
		}
		
		invalidate();
		
		return mSlideMode == MODE_SLIDE;
	}
	
	private void startSlideMode() {
		View v = getChildAt(1);
		
		if(mSlideMode == MODE_READY) {
			mStartOffset = 0;
			mEndOffset = getChildAt(0).getWidth();
		} else {
			mStartOffset = getChildAt(0).getWidth();
			mEndOffset = 0;
		}
		
		mOffset = mStartOffset;
		
		if(mCachedBitmap == null) {
			mCachedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
			mCachedCanvas = new Canvas(mCachedBitmap);
		}
		
		v.setVisibility(View.VISIBLE);
		
		v.draw(mCachedCanvas);
		
		mTopView = v;
		
		mSlideMode = MODE_SLIDE;
	}
	
	private boolean isSlideAllowed() {
		return (mEndOffset > 0 && mOffset < mEndOffset && mOffset >= mStartOffset)
				|| (mEndOffset == 0 && mOffset > mEndOffset && mOffset <= mStartOffset);
	}
	
	private Animation.AnimationListener mOpenListener = new Animation.AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mSlideMode = MODE_FINISHED;
			mTopView.setVisibility(View.GONE);
			
			if(mListener != null) {
				mListener.onSlideCompleted(true);
			}
		}
	};
	
	private Animation.AnimationListener mCloseListener = new Animation.AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			mSlideMode = MODE_READY;
			mTopView.setVisibility(View.VISIBLE);
			
			if(mListener != null) {
				mListener.onSlideCompleted(false);
			}
		}
	};
	
	private void finishSlide() {
		if(mEndOffset > 0) {
			if(mOffset > mEndOffset/2) {
				if(mOffset > mEndOffset) mOffset = mEndOffset;
				
				Animation anim = new SlideAnimation(mOffset, mEndOffset);
				anim.setAnimationListener(mOpenListener);
				startAnimation(anim);
			} else {
				if(mOffset < mStartOffset) mOffset = mStartOffset;
				
				Animation anim = new SlideAnimation(mOffset, mStartOffset);
				anim.setAnimationListener(mCloseListener);
				startAnimation(anim);
			}
		} else {
			if(mOffset < mStartOffset/2) {
				if(mOffset < mEndOffset) mOffset = mEndOffset;
				
				Animation anim = new SlideAnimation(mOffset, mEndOffset);
				anim.setAnimationListener(mCloseListener);
				startAnimation(anim);
			} else {
				if(mOffset > mStartOffset) mOffset = mStartOffset;
				
				Animation anim = new SlideAnimation(mOffset, mStartOffset);
				anim.setAnimationListener(mOpenListener);
				startAnimation(anim);
			}
		}
	}
	
	private class SlideAnimation extends Animation {
		
		private static final float SPEED = 0.6f;
		
		private float mStart;
		private float mEnd;
		
		public SlideAnimation(float fromX, float toX) {
			mStart = fromX;
			mEnd = toX;
			
			setInterpolator(new DecelerateInterpolator());

			float duration = Math.abs(mEnd - mStart) / SPEED;
			setDuration((long) duration);
		}
		
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			super.applyTransformation(interpolatedTime, t);
			
			float offset = (mEnd - mStart) * interpolatedTime + mStart;
			mOffset = (int) offset;
			postInvalidate();
		}
		
	}
	
	public static interface OnSlideListener {
		public void onSlideCompleted(boolean opened);
	}

}
