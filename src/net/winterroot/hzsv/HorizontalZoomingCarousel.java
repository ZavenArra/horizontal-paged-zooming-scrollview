package net.winterroot.hzsv;

import net.winterroot.horizonalzoomingscrollview.R;
import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;



public class HorizontalZoomingCarousel extends HorizontalScrollView {

	HorizontalZoomingCarouselListener listener = null;
	Context c;
	
	int edgeBufferWidth = 220;
	
	
	int[] resources = { R.drawable.a1, R.drawable.b1, R.drawable.c1, R.drawable.d1, R.drawable.e1 };
	ImageView[] imageViews;
	
	LinearLayout mScrollableArea;
	
	public HorizontalZoomingCarousel(Context context, ImageView[] instanceImageViews) {
		super(context);
		c = context;
		imageViews = instanceImageViews;
		
		mScrollableArea	 = new LinearLayout(context);	
		mScrollableArea.setOrientation(LinearLayout.HORIZONTAL);
		android.view.ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 122);
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
			
			// Compute the matrix
			Matrix m = new Matrix();
			m.reset();

			// Middle of the image should be the scale pivot
			m.postScale(scale, scale);
			
			//center in frame.
			//m.postTranslate()
			
			iv.setScaleType(ScaleType.MATRIX);
			iv.setImageMatrix(m);
		}
	}

	    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
	    boolean result = super.onTouchEvent(event);

	    switch(event.getAction()) {
	        case(MotionEvent.ACTION_DOWN):
	            break;
	        case(MotionEvent.ACTION_UP):
	        	
	        	int itemWidth = 216;
	        	
	        	int scrollX = getScrollX();
	        	int page = (scrollX + itemWidth / 2) / itemWidth;
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
