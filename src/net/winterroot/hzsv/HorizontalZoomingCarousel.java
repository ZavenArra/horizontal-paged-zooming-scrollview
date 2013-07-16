package net.winterroot.hzsv;

import com.amci.nissan360.R;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;



public class HorizontalZoomingCarousel extends HorizontalScrollView {

	HorizontalZoomingCarouselListener listener = null;
	Context c;

	int edgeBufferWidth = 150;
	public int matrixMultiplier = 1;

	int itemWidth = 0;
	int itemHeight = 0;
	int viewWidth = 656; // Hard coded for target device for now

	ImageView[] imageViews;

	LinearLayout mScrollableArea = null;

	CarouselAdapter mAdapter;

	public HorizontalZoomingCarousel(Context context, int setItemWidth, int setItemHeight) {
		super(context);
		c = context;
		itemWidth = setItemWidth;
		itemHeight = setItemHeight;

	}

	public void layoutImageViews(){
		/*WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		 */

		//int screenWidth = size.x;
		int thisViewWidth = getWidth();

		edgeBufferWidth = (viewWidth - itemWidth) / 2;

		mScrollableArea	 = new LinearLayout(c);	
		mScrollableArea.setOrientation(LinearLayout.HORIZONTAL);
		android.view.ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, itemHeight * matrixMultiplier);
		mScrollableArea.setLayoutParams(params);

		LinearLayout.LayoutParams bufferLayoutParams =  new LinearLayout.LayoutParams(edgeBufferWidth, LayoutParams.MATCH_PARENT);
		LinearLayout edgeBuffer = new LinearLayout(c);
		edgeBuffer.setLayoutParams(bufferLayoutParams);
		mScrollableArea.addView(edgeBuffer);

		for(ImageView iv : imageViews){
			//android.view.ViewGroup.LayoutParams lp = new android.view.ViewGroup.LayoutParams(216, LayoutParams.WRAP_CONTENT);
			//iv.setLayoutParams(lp);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(itemWidth, itemHeight);
			iv.setLayoutParams(layoutParams);
			mScrollableArea.addView(iv);
		}

		LinearLayout edgeBufferRight = new LinearLayout(c);
		edgeBufferRight.setLayoutParams(bufferLayoutParams);
		mScrollableArea.addView(edgeBufferRight);

		addView(mScrollableArea);

		adjustImageSizes();

	}

	public void setListener(HorizontalZoomingCarouselListener theListener){
		listener = theListener;
	}

	/*  Not going to use these ones
	public HorizonalZoomingCarousel(Context context, AttributeSet attrs) {
		super(context, attrs);
		c= context;
	}

	public HorizonalZoomingCarousel(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		c= context;
	}
	 */

	@Override
	public void fling (int velocityY)
	{
		/*Scroll view is no longer gonna handle flings
		 * super.fling(velocityY);
		 */
	}



	@Override
	protected void onLayout (boolean changed, int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		adjustImageSizes();
	}


	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		//Log.v("SCROLL", String.valueOf(l));

		adjustImageSizes();

	}

	private void adjustImageSizes(){

		WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;

		int width = getWidth();
		width = viewWidth; // To do this correctly we need to know the calculated width of the scrollview
		// which is tricky because it's a calculated value
		int paddingLeft = (screenWidth - width) / 2;
		if(imageViews != null){
			for(ImageView iv : imageViews){
				int[] location = new int[2];
				iv.getLocationOnScreen(location);

				int imageViewCenter = (location[0] + itemWidth / 2) - paddingLeft; // This can be calculated for each imageView that is on screen

				if(imageViewCenter < 0){
					imageViewCenter = 0;
				}
				if(imageViewCenter > width){
					imageViewCenter = width;
				}

				int scrollerCenter =  getWidth() / 2; //(getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
				float offset = Math.abs(scrollerCenter - imageViewCenter);

				float ratio = ( offset / ( width / 2)  );

				// Scale the size.
				float scale = .5f + (1.0f - ratio) / 2;
				if (scale <= 0) {
					scale = 0.1f;
				}
				scale = scale * matrixMultiplier;

				// Compute the matrix
				Matrix m = new Matrix();
				m.reset();

				// Middle of the image should be the scale pivot
				m.postScale(scale, scale);

				//center in frame.
				int offsetInFrame = (int) (itemWidth - itemWidth * scale) / 2;
				m.postTranslate(offsetInFrame, 0);

				iv.setScaleType(ScaleType.MATRIX);
				iv.setImageMatrix(m);
			}
		}
	}


	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean result = super.onTouchEvent(event);

		switch(event.getAction()) {
		case(MotionEvent.ACTION_DOWN):
			if(listener != null){
				listener.onStartedMove();
			}
		break;
		case(MotionEvent.ACTION_UP):

			int itemWidth = this.itemWidth * matrixMultiplier;

		int scrollX = getScrollX();
		int page = (scrollX + itemWidth / 2) / itemWidth;
		if(page >= imageViews.length){
			page = imageViews.length-1;
		}
		int offset = page * itemWidth;
		this.smoothScrollTo(offset, 0);
		if(listener != null){
			listener.onItemSelected(page);
		}

		break;
		}

		return result;
	}

	public void setAdapter(CarouselAdapter brandAdapter) {
		mAdapter = brandAdapter;
	}

	public void cursorUpdate(){
		// As a hack, we'll just read from the adapter here and do the layout
		// Ideally this should be doing some kind of observing
		if(mScrollableArea != null){
			removeView(mScrollableArea);
		}
		mScrollableArea = null;
		imageViews = null;
		System.gc(); // They say to garbage collect here..
		// http://stackoverflow.com/questions/3117429/garbage-collector-in-android

		imageViews = new ImageView[mAdapter.getCount()];
		for(int i=0; i< mAdapter.getCount(); i++){
			ImageView iv = new ImageView(c);  // Have to wonder about this and the memory leaks
			iv.setImageResource(R.drawable.t1);

			if(i % 2 == 0 ) {
				iv.setBackgroundColor(0xFF00FF00);
			} else {
				iv.setBackgroundColor(0xFFFF0000);
			}
			imageViews[i] = iv;
		}

		layoutImageViews();



	}


}
