package com.streamcrop.livebroadcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;

public class SplashActivity extends Activity {
	ImageView m_imgSplash = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_splash);
		
		findViews();
		initData();
	}
	
	protected void findViews()
	{
		m_imgSplash = (ImageView) findViewById(R.id.img_splash);
	}
	
	protected void initData()
	{
		startAlphaAnimation();
	}		
		
	private void startAlphaAnimation()
	{
		AlphaAnimation face_in_out_anim = new AlphaAnimation(0.1f, 1.0f);
		face_in_out_anim.setDuration(1000);     
		face_in_out_anim.setRepeatMode(Animation.REVERSE);
		
		if (m_imgSplash != null){
			m_imgSplash.setAnimation(face_in_out_anim);
		}
		face_in_out_anim.start(); 		
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				onFinishAnimation();
			}
		}, 1500);
	}
	
	private void onFinishAnimation()
	{
		gotoSettingPage();
	}
	
	private void gotoSettingPage()
	{
		Intent intent = new Intent(this, MainActivity.class);	       
        startActivity(intent);        
        
        finish();
	}

}
