package com.alec.heif.braille;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.alec.heif.braille.util.SystemUiHider;

import com.alec.heif.braille.R;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import android.util.Log;

import android.speech.tts.TextToSpeech;

public class FullscreenActivity extends Activity implements
		CvCameraViewListener, TextToSpeech.OnInitListener {

	private static final int REQUEST_IMAGE_CAPTURE = 100;

	// Text to Speech stuff
	private TextToSpeech tts;
	
//	final View controlsView = findViewById(R.id.fullscreen_content_controls);
//	final View contentView = findViewById(R.id.fullscreen_content);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		dispatchTakePictureIntent(); 
		
		tts = new TextToSpeech(this, this);
	}

	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	    }
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		System.out.println("X\n\n" + Integer.toString(resultCode) + "\n\nX");
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
		    Bitmap photo = (Bitmap) data.getExtras().get("data"); 
			if (resultCode == RESULT_OK) {
				ImageView img = new ImageView(this);
				img.setImageBitmap(photo);
				setContentView(img);
				Toast.makeText(this, "Image recorded", Toast.LENGTH_LONG).show();
 
			} else if (resultCode == RESULT_CANCELED) {
				System.out.println("WOO FIGURED OUT WHAT CANCELED DOES");
			}
		}
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: 
				Log.i("OpenCV", "OpenCV loaded successfully");
				break;
			default: 
				super.onManagerConnected(status);
				break;
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		//   This method is invoked when camera preview has started.
	}

	@Override
	public void onCameraViewStopped() {
		// This method is invoked when camera preview has been stopped for some reason.
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return inputFrame.rgba();
	}

	@Override
	public Mat onCameraFrame(Mat inputFrame) {
		return inputFrame;
	}

	/**
	 * This is for Text to Speech
	 */
	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = tts.setLanguage(Locale.US);
			tts.setSpeechRate(0.8f);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				speak();
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
		}
	}

	public void speak() {
		// String text = BrailleUtils.parseBraille(array);
		// This should be done somewhere else
	//	String text = "testing";
	//	tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

}
