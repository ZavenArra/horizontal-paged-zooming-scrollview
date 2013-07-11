package net.winterroot.hzsv;

import net.winterroot.horizonalzoomingscrollview.R;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends Activity implements HorizontalZoomingCarouselListener {

	int[] resources = { R.drawable.a1, R.drawable.b1, R.drawable.c1, R.drawable.d1, R.drawable.e1 };

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
	
	}
	
	protected void onStart(){
		super.onStart();
		
		ImageView[] imageViews = new ImageView[resources.length];
		for(int i=0; i< resources.length; i++){
			ImageView iv = new ImageView(this);
			iv.setImageResource(resources[i]);
			imageViews[i] = iv;
		}
		
		HorizontalZoomingCarousel carousel = new HorizontalZoomingCarousel(this, imageViews);
		carousel.setListener(this);
		RelativeLayout mainLayout = (RelativeLayout) findViewById(R.id.activity_main);
		mainLayout.addView(carousel);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onItemSelected(int index) {
		Toast.makeText(this, String.valueOf(index), Toast.LENGTH_SHORT).show();
		
	}

}
