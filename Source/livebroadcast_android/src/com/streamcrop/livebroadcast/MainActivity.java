package com.streamcrop.livebroadcast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
 
public class MainActivity extends Activity {
    EditText	m_editChannel = null;
    EditText	m_editAppName = null;
    EditText	m_EditServer = null;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    
        setContentView(R.layout.layout_main);
        
        findViews();
        initEvents();
    }
 
    private void findViews()
    {
    	m_EditServer = (EditText) findViewById(R.id.edit_address);
    	m_editAppName = (EditText) findViewById(R.id.edit_appname);
    	m_editChannel = (EditText) findViewById(R.id.edit_channel);
    }
    
    private void initEvents()
    {
    	findViewById(R.id.btn_publish).setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				publishCameraStream();
			}
		});
    }
    
    private void publishCameraStream()
    {
    	String server = m_EditServer.getText().toString();
    	String appname = m_editAppName.getText().toString();
    	String channel = m_editChannel.getText().toString();
    	
    	Intent intent = new Intent(this, PublishActivity.class);
       
    	Bundle bundle = new Bundle();    	
    	bundle.putString("server", server);
    	bundle.putString("appname", appname);
    	bundle.putString("channel", channel);
    	
    	
    	intent.putExtras(bundle);
    	
        startActivity(intent);        
    }
}