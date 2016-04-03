package com.streamcrop.livebroadcast;

import com.ksy.recordlib.service.core.KsyRecordClient;
import com.ksy.recordlib.service.core.KsyRecordClientConfig;
import com.ksy.recordlib.service.core.KsyRecordSender;
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

public class PublishActivityRtmp extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "JCameara";

    private Button m_btnCameraSwitch;
    private Button m_btnCameraSetting;
    
    private Button startStop;
    
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private boolean recording;
    
    private KsyRecordClient client;
    private KsyRecordClientConfig config;
    
    String m_Server = "rtmp://178.62.32.245:1935/hls";
    String m_AppName = "hls";
    String m_Channel = "jyy1";
    
    private long	m_lastStop = 0;
    private static final long GAP = 3000;
    
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
         
         m_btnCameraSwitch = (Button) findViewById(R.id.btn_camera_switch);
         m_btnCameraSetting = (Button) findViewById(R.id.btn_camera_setting);
    }
    
    protected void initData()
    {
    	Bundle bundle = getIntent().getExtras();
		
		if( bundle != null )
		{
			m_Server = bundle.getString("server", "rtmp://178.62.32.245:1935/hls"); 
			
			m_Channel = bundle.getString("channel", "jyy1");
		}
        
        surfaceHolder.addCallback(this);
        // We still need this line for backward compatibility reasons with android 2
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        
        setPreviewState(false);
        
        setUpEnvironment();
        setupRecord();
    }
    
    private void setUpEnvironment() {
        // Keep screen on
        KsyRecordClientConfig.Builder builder = new KsyRecordClientConfig.Builder();
        builder.setVideoProfile(CamcorderProfile.QUALITY_720P).setUrl(m_Server + "/" + m_Channel);
        
        config = builder.build();
    }
    
    private void setupRecord() {
        client = KsyRecordClient.getInstance(getApplicationContext());
        client.setConfig(config);
        client.setDisplayPreview(surfaceview);        
    }
    
    private void setPreviewState(boolean flag)
    {
    	m_btnCameraSwitch.setEnabled(flag);
    }
    
    private void restartRecord()
    {
    	m_lastStop = System.currentTimeMillis();
    	setPreviewState(false);
    	client.stopPreview();
    	surfaceview.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					try {
						client.startPreview();
						m_lastStop = System.currentTimeMillis();
						setPreviewState(true);
						
						if( recording == false )
							client.stopRecord();						
					} catch (KsyRecordException e) {						
						e.printStackTrace();
					}
					
				}
			}, 1000);
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
    	 
    	 ResourceUtils.addClickEffect(m_btnCameraSwitch);
    	 m_btnCameraSwitch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCameraSwitch();				
			}
		});
    	
    	ResourceUtils.addClickEffect(m_btnCameraSetting);
    	m_btnCameraSetting.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCameraSetting();
			}
		});
    }
    
    private void onClickCameraSwitch()
    {
    	try {
    		client.switchCamera();   		
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    
    private void onClickCameraSetting()
    {
    	if( System.currentTimeMillis() - m_lastStop < 500 )
    		return;
    	
    	Intent intent = new Intent(this, SettingActivity.class);
       
        startActivity(intent); 
        
        KsyRecordSender sender = KsyRecordSender.getRecordInstance();
        sender.getAVBitrate();
    }
    
	@Override
	public void onDestroy( ) {
		client.stopRecord();
		client.release();
				
		super.onDestroy();
	}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
        
        restartRecord(); 
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	int i = 0;
    	i = 1;
    }
}  