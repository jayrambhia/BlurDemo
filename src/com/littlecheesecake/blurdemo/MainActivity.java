package com.littlecheesecake.blurdemo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {
	private ImageView mImage;
	private TextView mText;
	
	private OnPreDrawListener mPreDrawListener = 
			new OnPreDrawListener(){

				@Override
				public boolean onPreDraw() {
					ViewTreeObserver observer = mText.getViewTreeObserver();
					if(observer != null){
						observer.removeOnPreDrawListener(this);
					}
					Drawable drawable = mImage.getDrawable();
					
					if(drawable != null &&
							drawable instanceof BitmapDrawable){
						Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
						if(bitmap != null){
							blur(bitmap, mText, 25);
						}
					}
					return true;
				}
		
	};
	
	private OnGlobalLayoutListener mLayoutListener =
			new OnGlobalLayoutListener(){
		@Override
		public void onGlobalLayout(){
			ViewTreeObserver observer =
					mText.getViewTreeObserver();
			if(observer != null){
				observer.addOnPreDrawListener(mPreDrawListener);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mImage = (ImageView)findViewById(R.id.background_view);
		mText = (TextView)findViewById(R.id.overlay_view);
		
		if(mImage != null && mText != null){
			ViewTreeObserver observer =
					mText.getViewTreeObserver();
			if(observer != null){
				observer.addOnGlobalLayoutListener(mLayoutListener);
			}
		}
	}
	
	private void blur(Bitmap bkg, View view, float radius){
		//create a new Bitmap object
		Bitmap overlay = Bitmap.createBitmap(
				view.getMeasuredWidth(),
				view.getMeasuredHeight(),
				Bitmap.Config.ARGB_8888);
		
		//wrap the *blank* bitmap within a Canvas that we can draw upon
		Canvas canvas = new Canvas(overlay);
		
		//cut the section of the image we want and draw on the canvas
		canvas.drawBitmap(bkg, -view.getLeft(), -view.getTop(), null);
		
		//construct RenderScript
		RenderScript rs = RenderScript.create(this);
		
		//creating the allocation from the bitmap we construct, copy to the 
		//memory to be used in RenderScript native environment
		Allocation overlayAlloc = Allocation.createFromBitmap(
				rs, overlay);
		
		//do the bluring
		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(
				rs, overlayAlloc.getElement());
		
		blur.setInput(overlayAlloc);
		blur.setRadius(radius);
		blur.forEach(overlayAlloc);
		
		//after bluring, copy it back to java memory address
		overlayAlloc.copyTo(overlay);
		
		//set the blured image as the background of the view
		view.setBackground(new BitmapDrawable(
				getResources(), overlay));
		
		//free the memory
		rs.destroy();
		
	}

}
