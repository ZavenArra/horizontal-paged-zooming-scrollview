package net.winterroot.hzsv;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Point;
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
	
	
	ImageView[] imageViews;
	
	LinearLayout mScrollableArea;
	
	public HorizontalZoomingCarousel(Context context, ImageView[] instanceImageViews) {
		super(context);
		c = context;
		imageViews = instanceImageViews;
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screenWidth = size.x;
		edgeBufferWidth = (screenWidth - 108) / 2;
		
		mScrollableArea	 = new LinearLayout(context);	
		mScrollableArea.setOrientation(LinearLayout.HORIZONTAL);
		android.view.ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 122 * matrixMultiplier);
		mScrollableArea.setLayoutParams(params);
		
		LinearLayout.LayoutParams bufferLayoutParams =  new LinearLayout.LayoutParams(edgeBufferWidth, LayoutParams.MATCH_PARENT);
		LinearLayout edgeBuffer = new LinearLayout(context);
		edgeBuffer.setLayoutParams(bufferLayoutParams);
		mScrollableArea.addView(edgeBuffer);
		
		for(ImageView iv : imageViews){
			android.view.ViewGroup.LayoutParams lp = new android.view.ViewGroup.LayoutParams(216, LayoutParams.WRAP_CONTENT);
			iv.setLayoutParams(lp);
			mScrollableArea.addView(iv);
		}
		
		LinearLayout edgeBufferRight = new LinearLayout(context);
		edgeBufferRight.setLayoutParams(bufferLayoutParams);
		mScrollableArea.addView(edgeBufferRight);
		
		addView(mScrollableArea);
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
		
		int width = getWidth();
		//int[] views =  { R.id.imageView1, R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5 };
		for(ImageView iv : imageViews){
			int[] location = new int[2];
			iv.getLocationOnScreen(location);

			int imageViewCenter = location[0] + 108 / 2; // This can be calculated for each imageView that is on screen
			
			if(imageViewCenter < 0){
				imageViewCenter = 0;
			}
			if(imageViewCenter > width){
				imageViewCenter = width;
			}

			int scrollerCenter = (getWidth() - getPaddingLeft() - getPaddingRight()) / 2 + getPaddingLeft();
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
			int offsetInFrame = (int) (108 - 108 * scale) / 2;
			m.postTranslate(offsetInFrame, 0);
			
			iv.setScaleType(ScaleType.MATRIX);
			iv.setImageMatrix(m);
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
	        	
	        	int itemWidth = 216 * matrixMultiplier;
	        	
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

	
}
