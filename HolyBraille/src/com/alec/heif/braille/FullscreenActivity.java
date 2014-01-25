package com.alec.heif.braille;

import java.io.ByteArrayOutputStream;
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
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

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
			if (resultCode == RESULT_OK) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				ImageView img = new ImageView(this);
				photo = thresholdImage(photo);
				img.setImageBitmap(photo);
				setContentView(img);
				Toast.makeText(this, "Image recorded", Toast.LENGTH_LONG).show();
 
			} else if (resultCode == RESULT_CANCELED) {
				System.out.println("WOO FIGURED OUT WHAT CANCELED DOES");
			}
		}
	}
	
	private Bitmap thresholdImage(Bitmap bmp) {
	    Mat mat = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
	    Mat dst = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
	    Utils.bitmapToMat(bmp, mat);
	    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
		Log.i("THRESHOLD", "Started Doing This");
		Imgproc.adaptiveThreshold(mat, dst, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY , 3, 0);
		
		
	    //byte buff[] = new byte[(int) (mat.total() * mat.channels())];
    	//mat.get(0, 0, buff);

    	/*for (int i = 0; i < buff.length; i++) {
    		if( ((int) buff[i]) < 0) {
    			buff[i] = (byte)-128;
    		}
    		else {
    			buff[i] = (byte)128;
    		}	
    	}*/
    	//Mat m = new Mat(bmp.getWidth(), bmp.getHeight(), );
    	//Bitmap b = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ALPHA_8);
    	//mat.put(0, 0, buff);
        Utils.matToBitmap(dst, bmp);
        return bmp;
	}

	    /*function otsu(histogram, total) {
	        var sum = 0;
	        for (var i = 1; i < 256; ++i)
	            sum += i * histogram[i];
	        var sumB = 0;
	        var wB = 0;
	        var wF = 0;
	        var mB;
	        var mF;
	        var max = 0.0;
	        var between = 0.0;
	        var threshold1 = 0.0;
	        var threshold2 = 0.0;
	        for (var i = 0; i < 256; ++i) {
	            wB += histogram[i];
	            if (wB == 0)
	                continue;
	            wF = total - wB;
	            if (wF == 0)
	                break;
	            sumB += i * histogram[i];
	            mB = sumB / wB;
	            mF = (sum - sumB) / wF;
	            between = wB * wF * Math.pow(mB - mF, 2);
	            if ( between >= max ) {
	                threshold1 = i;
	                if ( between > max ) {
	                    threshold2 = i;
	                }
	                max = between;            
	            }
	        }
	        return ( threshold1 + threshold2 ) / 2.0;
	    }	*/    

// Maybe useful stuff for circle detection drawing?
//	private Bitmap test(Bitmap bmp) {
//		Mat imgSource = new Mat(), imgCirclesOut = new Mat();
//		Utils.bitmapToMat(bmp , imgSource);
//		Imgproc.cvtColor(imgSource, imgSource, Imgproc.COLOR_BGR2GRAY);
//
//		Imgproc.GaussianBlur( imgSource, imgSource, new Size(9, 9), 2, 2 );
//		Imgproc.HoughCircles( imgSource, imgCirclesOut, Imgproc.CV_HOUGH_GRADIENT, 1, imgSource.rows()/8, 200, 100, 0, 0 );
//		float circle[] = new float[3];
//		for (int i = 0; i < imgCirclesOut.cols(); i++) {
//		    imgCirclesOut.get(0, i, circle);
//		    org.opencv.core.Point center = new org.opencv.core.Point();
//		    center.x = circle[0];
//		    center.y = circle[1];
//		    Core.circle(imgSource, center, (int) circle[2], new Scalar(255,0,0,255), 4);
//		}
//		Bitmap bmp2 = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
//		Utils.matToBitmap(imgSource, bmp2);
//		return bmp2;
//	}

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
