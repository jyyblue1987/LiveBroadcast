package com.streamcrop.livebroadcast;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import tv.inhand.capture.SessionBuilder;
import tv.inhand.capture.video.VideoQuality;
 
public class SettingActivity extends Activity {
	TextView 	m_txtResolution = null;
	TextView 	m_txtFrameRate = null;
	TextView 	m_txtBitRate = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        setContentView(R.layout.layout_setting);
        
        findViews();
        initData();
        initEvents();
    }
 
    private void findViews()
    {
    	m_txtResolution = (TextView) findViewById(R.id.txt_resolution);
    	m_txtFrameRate = (TextView) findViewById(R.id.txt_framerate);
    	m_txtBitRate = (TextView) findViewById(R.id.txt_bitrate);
    }
    
    private void initData()
    {
    	VideoQuality videoQuality = SessionBuilder.getInstance().getVideoQuality();
    	m_txtResolution.setText(videoQuality.resX + "x" + videoQuality.resY);
    	m_txtFrameRate.setText(videoQuality.framerate + "frame/s" );
    	m_txtBitRate.setText("" + (videoQuality.bitrate / 1000.0f));
    }
    
    private void initEvents()
    {
    	findViewById(R.id.lay_resolution).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickVideoResolutioin();
			}
		});
    	
    	findViewById(R.id.txt_framerate).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickFrameRate();
			}
		});
    	
       	findViewById(R.id.lay_bitrate).setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				onClickVideoBitRate();
			}
		});
    }
    
    private void onClickVideoResolutioin()
    {
    	final int [] resX = {320, 640, 960, 1280};
    	final int [] resY = {240, 480, 720, 960};
    	
		String [] items = new String[resX.length];
		for(int i = 0; i < items.length; i++)
		{
			items[i] = resX[i] + "x" + resY[i];
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int whichButton) {
				VideoQuality videoQuality = new VideoQuality();
				videoQuality.resX = resX[whichButton];
				videoQuality.resY = resY[whichButton];
				SessionBuilder.getInstance().setVideoQuality(videoQuality);
				
				m_txtResolution.setText(videoQuality.resX + "x" + videoQuality.resY);
				
				dialog.dismiss();
			}
		});
		
		dialog.create();
		AlertDialog alertDialog = dialog.show();
		
		alertDialog.setCanceledOnTouchOutside(true);
    }
    
    private void onClickFrameRate()
    {
    	final int [] framerate = {5, 7, 9, 11, 13, 15, 20, 25};
    	String [] items = new String[framerate.length];
		for(int i = 0; i < items.length; i++)
		{
			items[i] = framerate[i] + " frame/s";
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int whichButton) {
				VideoQuality videoQuality = new VideoQuality();
				videoQuality.framerate = framerate[whichButton];
				SessionBuilder.getInstance().setVideoQuality(videoQuality);
				
				m_txtFrameRate.setText(videoQuality.framerate + " frame/s");
				
				dialog.dismiss();
			}
		});
		
		dialog.create();
		AlertDialog alertDialog = dialog.show();
		
		alertDialog.setCanceledOnTouchOutside(true);
    }
    
    private void onClickVideoBitRate()
    {
    	final int [] bitrate = {1000, 5000, 10000, 20000, 50000, 100000, 200000, 500000, 1000000};
    	String [] items = new String[bitrate.length];
		for(int i = 0; i < items.length; i++)
		{
			items[i] = (bitrate[i] / 1000.0f) + "Kbps/s";
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int whichButton) {
				VideoQuality videoQuality = new VideoQuality();
				videoQuality.bitrate = bitrate[whichButton];
				SessionBuilder.getInstance().setVideoQuality(videoQuality);
				
				m_txtBitRate.setText((bitrate[whichButton] / 1000.0f) + "");
				
				dialog.dismiss();
			}
		});
		
		dialog.create();
		AlertDialog alertDialog = dialog.show();
		
		alertDialog.setCanceledOnTouchOutside(true);
    }
    
}