package com.streamcrop.livebroadcast;

import java.io.IOException;

import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;
import com.ksy.recordlib.service.exception.KsyRecordException;
import com.ksy.recordlib.service.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import tv.inhand.capture.Session;
import tv.inhand.capture.SessionBuilder;

public class PublishActivityRtmp extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "JCameara";

    private ImageView m_imgCameraSwitch;
    private ImageView m_imgCameraSetting;
    
    private Button startStop;
    
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private boolean recording;
    
    private KsyRecordClient client;
    private KsyRecordClientConfig config;
    
    String m_Server = "178.62.32.245";
    String m_AppName = "hls";
    String m_Channel = "jyy1";
    
    public void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.layout_publish);
       
        findViews();
        initData();
        initEvents();
    }

    protected void findViews()
    {
    	 surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
         surfaceHolder = surfaceview.getHolder();
         startStop = (Button) this.findViewById(R.id.start);
         
         m_imgCameraSwitch = (ImageView) findViewById(R.id.img_camera_switch);
         m_imgCameraSetting = (ImageView) findViewById(R.id.img_camera_setting);
    }
    
    protected void initData()
    {
    	Bundle bundle = getIntent().getExtras();
		
		if( bundle != null )
		{
			String server = bundle.getString("server", "178.62.32.245/hls"); 
			
			int index = server.indexOf("/");
			if( index >= 0 )
			{
				m_Server = server.substring(0, index);
				m_AppName = server.substring(index + 1);
			}
			else
			{
				m_Server = server + "";
				m_AppName = "hls";
			}
			m_Channel = bundle.getString("channel", "jyy1");
		}
        
        surfaceHolder.addCallback(this);
        // We still need this line for backward compatibility reasons with android 2
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        setUpEnvironment();
        setupRecord();
    }
    
    private void setUpEnvironment() {
        // Keep screen on
        KsyRecordClientConfig.Builder builder = new KsyRecordClientConfig.Builder();
        builder.setVideoProfile(CamcorderProfile.QUALITY_720P).setUrl("rtmp://178.62.32.245:1935/hls/1234567");
        
        config = builder.build();
        int videorate = config.getVideoBitRate();        
        config.setmVideoBitRate(1000000);
    }
    
    private void setupRecord() {
        client = KsyRecordClient.getInstance(getApplicationContext());
        client.setConfig(config);
        client.setDisplayPreview(surfaceview);   
//        client
        
    }
    
    private void startRecord() {
        try {
            client.startRecord();
            recording = true;
        } catch (KsyRecordException e) {
            e.printStackTrace();
            Log.d(Constants.LOG_TAG, "Client Error, reason = " + e.getMessage());
        }
    }
    
    private void stopRecord() {
        client.stopRecord();
        recording = false;
        Log.d(Constants.LOG_TAG, "stop and release");
    }
    
    protected void initEvents()
    {
    	ResourceUtils.addClickEffect(startStop);
    	 startStop.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Button btn = (Button)v;
                 if (!recording) {
                	 startRecord();
                     btn.setText("Stop");                     
                 }
                 else {
                	 stopRecord();
                     btn.setText("Publish");                     
                 }
             }
         });     
    	 
    	 m_imgCameraSwitch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCameraSwitch();				
			}
		});
    	 
    	 m_imgCameraSetting.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCameraSetting();
			}
		});
    }
    
    private void onClickCameraSwitch()
    {
    	try {
    		if( recording == true )
        	{
    			client.stopRecord();
                
                surfaceview.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
			                @Override
			                public void run() {
			                	
			                	int camera = config.getCameraType();
						    	if( camera == CameraInfo.CAMERA_FACING_BACK)
						    		config.setmCameraType(CameraInfo.CAMERA_FACING_FRONT);						    		
						    	else
						    		config.setmCameraType(CameraInfo.CAMERA_FACING_BACK);
						    	
						    	recording = false;
						    	startStop.performClick();
			                }
			            });												
					}
				}, 1000);
                
        		
        	}	
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    private void onClickCameraSetting()
    {
    	Intent intent = new Intent(this, SettingActivity.class);
       
        startActivity(intent);    
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
      
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    
    }
}  