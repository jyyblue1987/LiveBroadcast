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

package tv.inhand.capture.audio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Field;

import tv.inhand.rtmp.AACPacketizer;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

public class AACStream extends AudioStream {

	public final static String TAG = "AACStream";

	/** MPEG-4 Audio Object Types supported by ADTS. **/
	private static final String[] AUDIO_OBJECT_TYPES = {
		"NULL",							  // 0
		"AAC Main",						  // 1
		"AAC LC (Low Complexity)",		  // 2
		"AAC SSR (Scalable Sample Rate)", // 3
		"AAC LTP (Long Term Prediction)"  // 4	
	};

	/** There are 13 supported frequencies by ADTS. **/
	public static final int[] AUDIO_SAMPLING_RATES = {
		96000, // 0
		88200, // 1
		64000, // 2
		48000, // 3
		44100, // 4
		32000, // 5
		24000, // 6
		22050, // 7
		16000, // 8
		12000, // 9
		11025, // 10
		8000,  // 11
		7350,  // 12
		-1,   // 13
		-1,   // 14
		-1,   // 15
	};

	private int mActualSamplingRate;
	private int mProfile, mSamplingRateIndex, mChannel, mConfig;
	private SharedPreferences mSettings = null;

	public AACStream() throws IOException {
		super();
		
		if (!AACStreamingSupported()) {
			Log.e(TAG,"AAC not supported on this phone");
			throw new AACNotSupportedException();
		} else {
			Log.d(TAG,"AAC supported on this phone");
		}
		setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		mPacketizer = new AACPacketizer();
	}

	private static boolean AACStreamingSupported() {
		if (Integer.parseInt(android.os.Build.VERSION.SDK)<14) return false;
		try {
			MediaRecorder.OutputFormat.class.getField("AAC_ADTS");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Some data (the actual sampling rate used by the phone and the AAC profile)
	 * @param prefs The SharedPreferences that will be used to store the sampling rate
	 */
	public void setPreferences(SharedPreferences prefs) {
		mSettings = prefs;
	}

	public void start() throws IllegalStateException, IOException {
		super.start();
	}

	@Override
	protected void encodeWithMediaRecorder() throws IOException {
		testADTS();
		super.encodeWithMediaRecorder();
	}



	/** Stops the stream. */
	public synchronized void stop() {
		if (mStreaming) {
			super.stop();
		}
	}

	String savedADTSKey() {
		return "aac-"+mQuality.samplingRate;
	}
	/** 
	 * Records a short sample of AAC ADTS from the microphone to find out what the sampling rate really is
	 * On some phone indeed, no error will be reported if the sampling rate used differs from the 
	 * one selected with setAudioSamplingRate 
	 * @throws IOException 
	 * @throws IllegalStateException
	 */
	@SuppressLint("InlinedApi")
	private void testADTS() throws IllegalStateException, IOException {
		setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		try {
			Field name = MediaRecorder.OutputFormat.class.getField("AAC_ADTS");
			setOutputFormat(name.getInt(null));
		}
		catch (Exception ignore) {
			setOutputFormat(6);
		}
		
		// Checks if the user has supplied an exotic sampling rate
		int i=0;
		for (;i<AUDIO_SAMPLING_RATES.length;i++) {
			if (AUDIO_SAMPLING_RATES[i] == mQuality.samplingRate) {
				break;
			}
		}
		// If he did, we force a reasonable one: 16 kHz
		if (i>12) {
			Log.e(TAG,"Not a valid sampling rate: "+mQuality.samplingRate);
			mQuality.samplingRate = 16000;
		}
		
		if (mSettings!=null) {
			String savedKey = savedADTSKey();
			if (mSettings.contains(savedKey)) {
				String[] s = mSettings.getString(savedKey, "").split(",");
				mActualSamplingRate = Integer.valueOf(s[0]);
				mConfig = Integer.valueOf(s[1]);
				mChannel = Integer.valueOf(s[2]);
				Log.i(TAG, "Read ADTS config(" + savedKey + "):" + mActualSamplingRate + "," + mConfig + "," + mChannel);
				return;
			}
		}

		final String TESTFILE = Environment.getExternalStorageDirectory().getPath()+"/adtsstream-test.adts";

		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new IllegalStateException("No external storage or external storage not ready !");
		}

		// The structure of an ADTS packet is described here: http://wiki.multimedia.cx/index.php?title=ADTS

		// ADTS header is 7 or 9 bytes long
		byte[] buffer = new byte[9];

		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setAudioSource(mAudioSource);
		mMediaRecorder.setOutputFormat(mOutputFormat);
		mMediaRecorder.setAudioEncoder(mAudioEncoder);
		mMediaRecorder.setAudioChannels(2);
		mMediaRecorder.setAudioSamplingRate(mQuality.samplingRate);
		mMediaRecorder.setAudioEncodingBitRate(mQuality.bitRate);
		mMediaRecorder.setOutputFile(TESTFILE);
		mMediaRecorder.setMaxDuration(1000);

		mMediaRecorder.prepare();
		mMediaRecorder.start();

		// We record for 1 sec
		// TODO: use the MediaRecorder.OnInfoListener
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}


		try {
			mMediaRecorder.stop();
		} catch (Exception e) {}
		mMediaRecorder.reset();
		mMediaRecorder.release();
		mMediaRecorder = null;


		File file = new File(TESTFILE);
		RandomAccessFile raf = new RandomAccessFile(file, "r");

		// ADTS packets start with a sync word: 12bits set to 1
		while (true) {
			if ( (raf.readByte()&0xFF) == 0xFF ) {
				buffer[0] = raf.readByte();
				if ( (buffer[0]&0xF0) == 0xF0) break;
			}
		}

		raf.read(buffer,1,5);

		mSamplingRateIndex = (buffer[1]&0x3C)>>2 ;
		mProfile = ( (buffer[1]&0xC0) >> 6 ) + 1 ;
		mChannel = (buffer[1]&0x01) << 2 | (buffer[2]&0xC0) >> 6 ;
		mActualSamplingRate = AUDIO_SAMPLING_RATES[mSamplingRateIndex];

		// 5 bits for the object type / 4 bits for the sampling rate / 4 bits for the channel / padding
		mConfig = mProfile<<11 | mSamplingRateIndex<<7 | mChannel<<3;

		Log.i(TAG,"MPEG VERSION: " + ( (buffer[0]&0x08) >> 3 ) );
		Log.i(TAG,"PROTECTION: " + (buffer[0]&0x01) );
		Log.i(TAG,"PROFILE: " + AUDIO_OBJECT_TYPES[ mProfile ] );
		Log.i(TAG,"SAMPLING FREQUENCY: " + mActualSamplingRate );
		Log.i(TAG,"CHANNEL: " + mChannel );

		raf.close();

		if (mSettings!=null) {
			Editor editor = mSettings.edit();
			editor.putString(savedADTSKey(), mActualSamplingRate+","+mConfig+","+mChannel);
			editor.commit();
		}

		if (!file.delete()) {
			Log.e(TAG, "Temp file could not be erased");
		}
	}
}
