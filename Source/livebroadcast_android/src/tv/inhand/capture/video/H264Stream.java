/*
 * Copyright (C) 2011-2013 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package tv.inhand.capture.video;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import tv.inhand.capture.mp4.MP4Config;
import tv.inhand.rtmp.H264Packetizer;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Camera.CameraInfo;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class H264Stream extends VideoStream {

	private SharedPreferences mSettings = null;

	private Semaphore mLock = new Semaphore(0);

	/**
	 * Constructs the H.264 stream.
	 * Uses CAMERA_FACING_BACK by default.
	 * @throws IOException
	 */
	public H264Stream() throws IOException {
		this(CameraInfo.CAMERA_FACING_BACK);
	}	

	/**
	 * Constructs the H.264 stream.
	 * @param cameraId Can be either CameraInfo.CAMERA_FACING_BACK or CameraInfo.CAMERA_FACING_FRONT
	 * @throws IOException
	 */
	public H264Stream(int cameraId) throws IOException {
		super(cameraId);
		setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mPacketizer = new H264Packetizer();
	}

	/**
	 * Some data (SPS and PPS params) needs to be stored
	 * @param prefs The SharedPreferences that will be used to save SPS and PPS parameters
	 */
	public void setPreferences(SharedPreferences prefs) {
		mSettings = prefs;
	}

	/**
	 * Starts the stream.
	 * This will also open the camera and dispay the preview 
	 * if {@link #startPreview()} has not aready been called.
	 */
	public synchronized void start() throws IllegalStateException, IOException {
		MP4Config config = testH264();
		byte[] pps = Base64.decode(config.getB64PPS(), Base64.NO_WRAP);
		byte[] sps = Base64.decode(config.getB64SPS(), Base64.NO_WRAP);
		((H264Packetizer)mPacketizer).setStreamParameters(pps, sps);
		super.start();
	}

	private String savedH264Key() {
		return "h264"+mQuality.framerate+","+mQuality.resX+","+mQuality.resY+","+mQuality.orientation;
	}

	// Should not be called by the UI thread
	private MP4Config testH264() throws IllegalStateException, IOException {

		if (mSettings != null) {
			String saveKey = savedH264Key();

			if (mSettings.contains(saveKey)) {
				String savedValue = mSettings.getString(saveKey, "");
				Log.i(TAG, "Read MP4Config(" + saveKey + "):" + savedValue);
				String[] values = savedValue.split(",");
				return new MP4Config(values[0],values[1],values[2]);
			}
		}

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new IllegalStateException("No external storage or external storage not ready !");
		}

		final String TESTFILE = Environment.getExternalStorageDirectory().getPath()+"/h264stream-test.mp4";

		Log.i(TAG,"Testing H264 support... Test file saved at: "+TESTFILE);

		// Save flash state & set it to false so that led remains off while testing h264
		boolean savedFlashState = mFlashState;
		mFlashState = false;

		createCamera();
		
		// Stops the preview if needed
		if (mPreviewStarted) {
			lockCamera();
			try {
				mCamera.stopPreview();
			} catch (Exception e) {}
			mPreviewStarted = false;
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		unlockCamera();

		mMediaRecorder = new MediaRecorder();
		initRecorderParameters();
//		mMediaRecorder.setCamera(mCamera);
//		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setMaxDuration(1000);
//		mMediaRecorder.setVideoEncoder(mVideoEncoder);
//		mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
//		mMediaRecorder.setVideoSize(mQuality.resX,mQuality.resY);
//		mMediaRecorder.setVideoFrameRate(mQuality.framerate);
//		mMediaRecorder.setVideoEncodingBitRate(mQuality.bitrate);

		mMediaRecorder.setOutputFile(TESTFILE);

		// We wait a little and stop recording
		mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
			public void onInfo(MediaRecorder mr, int what, int extra) {
				Log.i(TAG,"MediaRecorder callback called !");
				if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
					Log.i(TAG,"MediaRecorder: MAX_DURATION_REACHED");
				} else if (what==MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
					Log.i(TAG,"MediaRecorder: MAX_FILESIZE_REACHED");
				} else if (what==MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
					Log.i(TAG,"MediaRecorder: INFO_UNKNOWN");
				} else {
					Log.i(TAG,"WTF ?");
				}
				mLock.release();
			}
		});
		// Start recording
		mMediaRecorder.prepare();
		mMediaRecorder.start();

		try {
			if (mLock.tryAcquire(6,TimeUnit.SECONDS)) {
				Log.i(TAG,"MediaRecorder callback was called :)");
				Thread.sleep(400);
			} else {
				Log.i(TAG,"MediaRecorder callback was not called after 6 seconds... :(");
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				mMediaRecorder.stop();
			} catch (Exception e) {}
			mMediaRecorder.reset();
			mMediaRecorder.release();
			mMediaRecorder = null;
			lockCamera();
		}

		// Retrieve SPS & PPS & ProfileId with MP4Config
		MP4Config config = new MP4Config(TESTFILE);

		// Delete dummy video
		File file = new File(TESTFILE);
		if (!file.delete())
			Log.e(TAG,"Temp file could not be erased");

		// Restore flash state
		mFlashState = savedFlashState;

		Log.i(TAG,"H264 Test succeeded...");

		// Save test result
		if (mSettings != null) {
			Editor editor = mSettings.edit();
			String saveKey = savedH264Key();
			String saveValue = config.getProfileLevel()+","+config.getB64SPS()+","+config.getB64PPS();
			editor.putString(saveKey, saveValue);
			editor.commit();
			Log.i(TAG, "Save configure:" + saveKey + ", " + saveValue);
		}
		
		return config;
	}
}
