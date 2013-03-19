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
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region.Op;
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
	private Paint mCachedPaint;
	private View mMenuView;
	
	private int mMode = MODE_READY;
	
	private int mOffset = 0;
	private int mStartOffset;
	private int mEndOffset;
	
	private boolean mEnabled = true;
	private boolean mInterceptTouch = true;
	private boolean mAlwaysOpened = false;
	
	private OnSlideListener mListener;
	
	public SlideHolder(Context context) {
		super(context);
		
		initView();
	}
	
	public SlideHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initView();
	}
	
	public SlideHolder(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initView();
	}
	
	private void initView() {
		mCachedPaint = new Paint(
					Paint.ANTI_ALIAS_FLAG
					| Paint.FILTER_BITMAP_FLAG
					| Paint.DITHER_FLAG
				);
	}
	
	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int parentLeft = 0;
		final int parentTop = 0;
		final int parentBottom = b - t;
		
		View menu = getChildAt(0);
		int menuWidth = menu.getMeasuredWidth();
		
		menu.layout(parentLeft, parentTop, parentLeft+menuWidth, parentBottom);
		
		if(mAlwaysOpened || mMode == MODE_FINISHED) {
			mOffset = menuWidth;
		} else if(mMode == MODE_READY) {
			mOffset = 0;
		}
		
		View main = getChildAt(1);
		main.layout(
					parentLeft + mOffset,
					parentTop,
					parentLeft + mOffset + main.getMeasuredWidth(),
					parentBottom
				);
		
		invalidate();
	}
	
	@Override
    protected void onMeasure(int wSp, int hSp) {
		mMenuView = getChildAt(0);
		
        if(mAlwaysOpened) {
            View main = getChildAt(1);
            
            if(mMenuView != null && main != null) {
            	measureChild(mMenuView, wSp, hSp);
                LayoutParams lp = (LayoutParams) main.getLayoutParams();
                lp.leftMargin = mMenuView.getMeasuredWidth();
            }
        }
        
        super.onMeasure(wSp, hSp);
    }
	
	@Override
	public void setEnabled(boolean enabled) {
		mEnabled = enabled;
	}
	
	@Override
	public boolean isEnabled() {
		return mEnabled;
	}
	
	public void setAllowInterceptTouch(boolean allow) {
		mInterceptTouch = allow;
	}
	
	public boolean isAllowedInterceptTouch() {
		return mInterceptTouch;
	}
	
	public void setAlwaysOpened(boolean opened) {
		mAlwaysOpened = opened;
		
		requestLayout();
	}
	
	public void setOnSlideListener(OnSlideListener lis) {
		mListener = lis;
	}
	
	public boolean isOpened() {
		return mMode == MODE_FINISHED;
	}
	
	public void toggle(boolean immediately) {
		if(immediately) {
			toggleImmediately();
		} else {
			toggle();
		}
	}
	
	public void toggle() {
		if(isOpened()) {
			close();
		} else {
			open();
		}
	}
	
	public void toggleImmediately() {
		if(isOpened()) {
			closeImmediately();
		} else {
			openImmediately();
		}
	}
	
	public boolean open() {
		if(isOpened() || mAlwaysOpened || mMode == MODE_SLIDE) {
			return false;
		}
		
		initSlideMode();
		
		Animation anim = new SlideAnimation(mOffset, mEndOffset);
		anim.setAnimationListener(mOpenListener);
		startAnimation(anim);
		
		invalidate();
		
		return true;
	}
	
	public boolean openImmediately() {
		if(isOpened() || mAlwaysOpened || mMode == MODE_SLIDE) {
			return false;
		}
		
		mMenuView.setVisibility(View.VISIBLE);
		mMode = MODE_FINISHED;
		requestLayout();
		
		if(mListener != null) {
			mListener.onSlideCompleted(true);
		}
		
		return true;
	}
	
	public boolean close() {
		if(!isOpened() || mAlwaysOpened || mMode == MODE_SLIDE) {
			return false;
		}
		
		initSlideMode();
		
		Animation anim = new SlideAnimation(mOffset, mEndOffset);
		anim.setAnimationListener(mCloseListener);
		startAnimation(anim);
		
		invalidate();
		
		return true;
	}
	
	public boolean closeImmediately() {
		if(!isOpened() || mAlwaysOpened || mMode == MODE_SLIDE) {
			return false;
		}
		
		mMenuView.setVisibility(View.GONE);
		mMode = MODE_READY;
		requestLayout();
		
		if(mListener != null) {
			mListener.onSlideCompleted(false);
		}
		
		return true;
	}

	private byte mFrame = 0;
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		try {
			if(mMode == MODE_SLIDE) {
				if(++mFrame % 5 == 0) {		//redraw every 5th frame
					getChildAt(1).draw(mCachedCanvas);
				}

				/*
				 * Draw only visible part of menu
				 */
				
				View menu = getChildAt(0);
				final int scrollX = menu.getScrollX();
				final int scrollY = menu.getScrollY();
				
				canvas.save();
				
				canvas.clipRect(0, 0, mOffset, getHeight(), Op.REPLACE);
				canvas.translate(-scrollX, -scrollY);
				
				menu.draw(canvas);
				
				canvas.restore();
				
				canvas.drawBitmap(mCachedBitmap, mOffset, 0, mCachedPaint);
			} else {
				if(!mAlwaysOpened && mMode == MODE_READY) {
		        	mMenuView.setVisibility(View.GONE);
		        }
				
				super.dispatchDraw(canvas);
			}
		} catch(IndexOutOfBoundsException e) {
			/*
			 * Possibility of crashes on some devices (especially on Samsung).
			 * Usually, when ListView is empty.
			 */
		}
	}
	
	private int mHistoricalX = 0;
	
	private boolean mClosing = false;
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(((!mEnabled || !mInterceptTouch) && mMode == MODE_READY) || mAlwaysOpened) {
			return super.dispatchTouchEvent(ev);
		}
		
		if(mMode != MODE_FINISHED) {
			onTouchEvent(ev);
			
			if(mMode != MODE_SLIDE) {
				super.dispatchTouchEvent(ev);
			} else {
				MotionEvent cancelEvent = MotionEvent.obtain(ev);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
				super.dispatchTouchEvent(cancelEvent);
			}
			
			return true;
		} else {
			Rect rect = new Rect();
			View menu = getChildAt(0);
			menu.getHitRect(rect);
			
			if(!rect.contains((int) ev.getX(), (int) ev.getY())) {
				mClosing = true;
				onTouchEvent(ev);
				
				return true;
			} else {
				onTouchEvent(ev);
				
				ev.offsetLocation(-menu.getLeft(), -menu.getTop());
				menu.dispatchTouchEvent(ev);
				
				return true;
			}
		}
	}
	
	private boolean handleTouchEvent(MotionEvent ev) {
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

			if((diff > 50 && mMode == MODE_READY) || (diff < -50 && mMode == MODE_FINISHED)) {
				mHistoricalX = (int) x;
				
				initSlideMode();
			} else if(mMode == MODE_SLIDE) {
				mOffset += (int) x - mHistoricalX;
				
				mHistoricalX = (int) x;
				
				if(!isSlideAllowed()) {
					finishSlide();
				}
			} else {
				return false;
			}
		}
		
		if(ev.getAction() == MotionEvent.ACTION_UP) {
			if(mMode == MODE_SLIDE) {
				finishSlide();
			}
			
			return false;
		}
		
		return mMode == MODE_SLIDE;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		boolean handled = handleTouchEvent(ev);
		
		invalidate();
		
		return handled;
	}
	
	private void initSlideMode() {
		View v = getChildAt(1);
		
		if(mMode == MODE_READY) {
			mStartOffset = 0;
			mEndOffset = getChildAt(0).getWidth();
		} else {
			mStartOffset = getChildAt(0).getWidth();
			mEndOffset = 0;
		}
		
		mOffset = mStartOffset;
		
		if(mCachedBitmap == null || mCachedBitmap.isRecycled() || mCachedBitmap.getWidth() != v.getWidth()) {
			mCachedBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
			mCachedCanvas = new Canvas(mCachedBitmap);
		}
		
		v.setVisibility(View.VISIBLE);
		
		mCachedCanvas.translate(-v.getScrollX(), -v.getScrollY());
		v.draw(mCachedCanvas);
		
		mMode = MODE_SLIDE;
		
		mMenuView.setVisibility(View.VISIBLE);
	}
	
	private boolean isSlideAllowed() {
		return (mEndOffset > 0 && mOffset < mEndOffset && mOffset >= mStartOffset)
				|| (mEndOffset == 0 && mOffset > mEndOffset && mOffset <= mStartOffset);
	}
	
	private void completeOpening() {
		mOffset = mMenuView.getWidth();
		requestLayout();
		
		post(new Runnable() {
			
			@Override
			public void run() {
				mMode = MODE_FINISHED;
				mMenuView.setVisibility(View.VISIBLE);
			}
		});
		
		if(mListener != null) {
			mListener.onSlideCompleted(true);
		}
	}
	
	private Animation.AnimationListener mOpenListener = new Animation.AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			completeOpening();
		}
	};
	
	private void completeClosing() {
		mOffset = 0;
		requestLayout();
		
		post(new Runnable() {
			
			@Override
			public void run() {
				mMode = MODE_READY;
				mMenuView.setVisibility(View.GONE);
			}
		});
		
		if(mListener != null) {
			mListener.onSlideCompleted(false);
		}
	}
	
	private Animation.AnimationListener mCloseListener = new Animation.AnimationListener() {
		
		@Override
		public void onAnimationStart(Animation animation) {}
		
		@Override
		public void onAnimationRepeat(Animation animation) {}
		
		@Override
		public void onAnimationEnd(Animation animation) {
			completeClosing();
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
