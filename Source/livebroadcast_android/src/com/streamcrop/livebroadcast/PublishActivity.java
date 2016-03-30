package com.streamcrop.livebroadcast;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import tv.inhand.capture.SessionBuilder;
import tv.inhand.capture.Session;

public class PublishActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "JCameara";

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
        surfaceview = (SurfaceView) this.findViewById(R.id.surfaceview);
        surfaceHolder = surfaceview.getHolder();
        
        
		Bundle bundle = getIntent().getExtras();
		
		if( bundle != null )
		{
			m_Server = bundle.getString("server", "178.62.32.245");
			m_AppName = bundle.getString("appname", "hls");
			m_Channel = bundle.getString("channel", "jyy1");
		}
        
        surfaceHolder.addCallback(this);
        // We still need this line for backward compatibility reasons with android 2
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        startStop = (Button) this.findViewById(R.id.start);
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
                        btn.setText("Stop");;
                    } catch (Exception e) {
                        Log.e(TAG, "video session", e);
                    }
                }
                else {
                    if (session != null) {
                        session.stopPublisher();
                        session.stop();
                        recording = false;
                        btn.setText("Start");
                    }
                }
            }
        });
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