package com.streamcrop.livebroadcast;

import java.io.IOException;

import android.app.Activity;
import android.hardware.Camera.CameraInfo;
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

public class PublishActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "JCameara";

    private ImageView m_imgCameraSwitch;
    private ImageView m_imgCameraSetting;
    
    private Button startStop;
    
    private Session session;
    private SurfaceView surfaceview;
    private SurfaceHolder surfaceHolder;
    private boolean recording;
    
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
    }
    
    protected void initEvents()
    {
    	 startStop.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Button btn = (Button)v;
                 if (!recording) {
                     try {
                         session = SessionBuilder.getInstance().build();
                         session.startPublisher(m_Channel);
                         session.start();
                         recording = true;
                         btn.setText("Stop");
                     } catch (Exception e) {
                         Log.e(TAG, "video session", e);
                     }
                 }
                 else {
                     if (session != null) {
                         session.stopPublisher();
                         session.stop();
                         recording = false;
                         btn.setText("Publish");
                     }
                 }
             }
         });     
    	 
    	 m_imgCameraSwitch.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onClickCameraSwitch();				
			}
		});
    }
    
    private void onClickCameraSwitch()
    {
    	try {
    		if( recording == true )
        	{
    			session.stopPublisher();
                session.stop();
                
                surfaceview.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						runOnUiThread(new Runnable() {
			                @Override
			                public void run() {
			                	int camera = SessionBuilder.getInstance().getCamera();
						    	if( camera == CameraInfo.CAMERA_FACING_BACK)
						    		SessionBuilder.getInstance().setCamera(CameraInfo.CAMERA_FACING_FRONT);
						    	else
						    		SessionBuilder.getInstance().setCamera(CameraInfo.CAMERA_FACING_BACK);
						    	
						    	recording = false;
						    	startStop.performClick();
			                }
			            });												
					}
				}, 3000);
                
        		
        	}	
    	} catch(Exception e){
    		e.printStackTrace();
    	}
    	
    }
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        surfaceHolder = holder;
        try {
            SessionBuilder.getInstance()                   
                    .setContext(getApplicationContext())
                    .setSurfaceHolder(surfaceHolder)
                    .setHost(m_Server)
                    .setAppName(m_AppName).build();
            ;
            startStop.performClick();
        } catch (Exception e) {
            Log.e(TAG, "Can't build session", e);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    	if( session != null && session.isStreaming() )
    	{
    		session.stopPublisher();
    		session.stop();
    	}
    }
}  