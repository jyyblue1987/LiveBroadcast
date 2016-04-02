package com.streamcrop.livebroadcast;

import java.util.ArrayList;
import java.util.List;

import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import tv.inhand.capture.SessionBuilder;
import tv.inhand.capture.video.VideoQuality;
 
public class SettingActivity extends Activity {
	TextView 	m_txtResolution = null;
	TextView 	m_txtVideoBitRate = null;
	TextView 	m_txtAudioBitRate = null;
	List<Camera.Size> camerasize = new ArrayList<Camera.Size>(); 
	
	private KsyRecordClient client;
    private KsyRecordClientConfig config;
    
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
    	m_txtResolution = (TextView) findViewById(R.id.fragment_videosize).findViewById(R.id.txt_content);
    	m_txtVideoBitRate = (TextView) findViewById(R.id.fragment_videobitrate).findViewById(R.id.txt_content);
    	m_txtAudioBitRate = (TextView) findViewById(R.id.fragment_audiobitrate).findViewById(R.id.txt_content);
    }
    
    private void initData()
    {
    	((TextView) findViewById(R.id.fragment_videosize).findViewById(R.id.txt_title)).setText("Camera Size");
    	((TextView) findViewById(R.id.fragment_videobitrate).findViewById(R.id.txt_title)).setText("Video Bitrate");
    	((TextView) findViewById(R.id.fragment_audiobitrate).findViewById(R.id.txt_title)).setText("Audio Bitrate");
    	
    	client = KsyRecordClient.getInstance(getApplicationContext());
    	config = KsyRecordClient.getConfig();
    	
    	m_txtResolution.setText(config.getVideoWidth() + "x" + config.getVideoHeight());
    	m_txtVideoBitRate.setText("" + (config.getVideoBitRate() / 1000) + "Kbps");
    	m_txtAudioBitRate.setText("" + (config.getAudioBitRate() / 1000) + "Kbps");
    	
//    	try {
//    		for (Camera.Size size : KsyRecordClient.mSupportedPreviewSizes) {
//        		camerasize.add(size);
//    	    }        	
//    	} catch(Exception e) {
//    		e.printStackTrace();
//    	}
    	
    }
    
    private void initEvents()
    {
    	findViewById(R.id.fragment_videosize).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickVideoResolutioin();
			}
		});
    	
    	findViewById(R.id.fragment_videobitrate).setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				onClickVideoBitRate();
			}
		});
    	
    	findViewById(R.id.fragment_audiobitrate).setOnClickListener(new View.OnClickListener() {
		
			@Override
			public void onClick(View v) {
				onClickAudioBitRate();
			}
		});
    }
    
    private void onClickVideoResolutioin()
    {
		String [] items = new String[camerasize.size()];
		for(int i = 0; i < items.length; i++)
		{
			items[i] = camerasize.get(i).width + "x" + camerasize.get(i).height;
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int whichButton) {
//				config.setmVideoWidth(camerasize.get(whichButton).width);
//				config.setmVideoHeigh(camerasize.get(whichButton).height);
				
				m_txtResolution.setText(camerasize.get(whichButton).width + "x" + camerasize.get(whichButton).height);
				
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
//				m_txt.setText(videoQuality.framerate + " frame/s");
				
				dialog.dismiss();
			}
		});
		
		dialog.create();
		AlertDialog alertDialog = dialog.show();
		
		alertDialog.setCanceledOnTouchOutside(true);
    }
    
    private void onClickVideoBitRate()
    {
    	final int [] bitrate = {200, 500, 1000, 1500, 2000, 2500, 3000, 3500, 4000};
    	String [] items = new String[bitrate.length];
		for(int i = 0; i < items.length; i++)
		{
			items[i] = bitrate[i] + "Kbps/s";
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int whichButton) {
				config.setmVideoBitRate(bitrate[whichButton] * 1000);
				m_txtVideoBitRate.setText(bitrate[whichButton] + "Kbps");				
				dialog.dismiss();
			}
		});
		
		dialog.create();
		AlertDialog alertDialog = dialog.show();
		
		alertDialog.setCanceledOnTouchOutside(true);
    }
    
    private void onClickAudioBitRate()
    {
    	final int [] bitrate = {2, 5, 10, 15, 20, 25, 30, 35, 40};
    	String [] items = new String[bitrate.length];
		for(int i = 0; i < items.length; i++)
		{
			items[i] = bitrate[i] + "Kbps";
		}
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		
		dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {			
			public void onClick(DialogInterface dialog, int whichButton) {
				config.setmAudioBitRate(bitrate[whichButton] * 1000);
				m_txtAudioBitRate.setText(bitrate[whichButton] + "Kbps");				
				dialog.dismiss();
			}
		});
		
		dialog.create();
		AlertDialog alertDialog = dialog.show();
		
		alertDialog.setCanceledOnTouchOutside(true);
    }
    
	
}
