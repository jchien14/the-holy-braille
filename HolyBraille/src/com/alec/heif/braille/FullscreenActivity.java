package com.alec.heif.braille;

import java.io.File;
import java.io.PrintWriter;
import java.util.Locale;

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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import android.util.Log;

import android.speech.tts.TextToSpeech;

public class FullscreenActivity extends Activity implements
		CvCameraViewListener, TextToSpeech.OnInitListener {

	private static final int REQUEST_IMAGE_CAPTURE = 100;

	// Text to Speech stuff
	private TextToSpeech tts;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_fullscreen);
		dispatchTakePictureIntent(); 
		
		tts = new TextToSpeech(this, this);
	}

	private void dispatchTakePictureIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    
	    //Save picture taken - disabled for now.
	    //if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        //startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
	    //}
	}
	
	/**
	 * When user takes photograph, if succeeded then parse image, get dots, and display filtered image.	
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			if (resultCode == RESULT_OK) {
				Bitmap photo = (Bitmap) data.getExtras().get("data");
				ImageView img = new ImageView(this);
				//Parse image, receive it and show the user. 
				photo = thresholdImage(photo);
				img.setImageBitmap(photo);
				setContentView(img);
				Toast.makeText(this, "Image recorded", Toast.LENGTH_LONG).show();
 
			}
		}
	}
	
	/**
	 * DEPRECATED: Code for circle detection - scrapped due to being too unreliable and hard to debug
	 **/
	/*private Bitmap advancedThresholdImage(Bitmap bmp) {
	    Mat mat = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
	    Mat dst = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_16UC3);
	    Utils.bitmapToMat(bmp, mat);
	    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
	    Mat circles = new Mat(1000, 1000, CvType.CV_16UC3);
        Imgproc.HoughCircles(mat, circles, Imgproc.CV_HOUGH_GRADIENT, 1, 8, 1, 8, 2, 6);
        for(int col = 0; col < circles.cols(); col++) {
        	//Log.i("X-CENTER", Double.toString(circles.get(0, col)[0]));
        	//Log.i("Y-CENTER", Double.toString(circles.get(0, col)[1]));
        	//Log.i("RADIUS", Double.toString(circles.get(0, col)[2]));
        }
        Log.e("TOTAL CIRCLES" , Integer.toString(circles.cols()));
        int[][] mapData = getData(mat, 255);
        Mat binMat = new Mat(mapData.length, mapData[0].length, CvType.CV_8UC1);
        for(int i = 0; i < mapData.length; i++) {
        	for(int j = 0; j < mapData[0].length; j++) {
        		byte[] md = {(byte)(mapData[i][j])};
        		binMat.put(i, j, md);
        	}
        }
		Bitmap bmpMono = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(binMat, bmpMono);
        Log.i("COMPLETE", "DICKS");
        return bmpMono;
	}*/
	
	/**
	 * All the OpenCV stuff.
	 */
	private Bitmap thresholdImage(Bitmap bmp) {
		/**
		 * Parse image into matrix, then grayscale it.
		 */
	    Mat mat = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
	    Utils.bitmapToMat(bmp, mat);
	    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2GRAY);
	    
	    /**
	     * DEPRECATED: Original filters, makes the cool sketch effect. Scrapped because too inaccurate.
	     */
		//Imgproc.adaptiveThreshold(mat, dst, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY , 7, 0);
		//Imgproc.bilateralFilter(dst, mat, 10, 50, 50);
		//Bitmap bmpMono = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(mat, bmpMono);
	    
	    /**
	     * DEPRECATED: Parse map data, also no longer needed since we use OpenCV to handle thresholding.
	     */
        /*for(int i = 0; i < mapData.length; i++) {
        	for(int j = 0; j < mapData[0].length; j++) {
        		byte[] md = {(byte)(mapData[i][j])};
        		binMat.put(i, j, md);
        	}
        }*/
	    
	    /**
	     * Make grayscale into binary using Otsu's algorithm.
	     */
        Mat binMat = new Mat(mat.rows(), mat.cols(), CvType.CV_8UC1);
        Imgproc.threshold(mat, binMat, 127.0, 255, Imgproc.THRESH_OTSU);
        
        /**
         * Set map data for use in braille translation, string builder stuff only used for developer mode.
         */
		//StringBuilder sb = new StringBuilder("[ ");
		int[][] mapData = new int[mat.rows()][mat.cols()];
		for(int row = 0; row < mat.rows(); row++) {
			//sb.append("[");
			for(int col = 0; col < mat.cols(); col++) {
				double val = mat.get(row, col)[0];
				if(val >= 127) {
					mapData[row][col] = 1;
					//sb.append("1,");
				}
				else {
					mapData[row][col] = 0;
					//sb.append("0,");
				}
			}
			//sb.deleteCharAt(sb.length()-1);
			//sb.append("],");
		}
		//sb.deleteCharAt(sb.length()-1);
		//sb.append("]");
		
		/**
		 * DEVELOPER MODE: Write the array to file to use for improving circle mapping algorithm.
		 */
		/*try {
			File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString(), "output.txt");
			PrintWriter fOut = new PrintWriter(file);
			fOut.print(sb.toString());
			fOut.flush();
			fOut.close();
		}
		catch(Exception e) {
			Log.i("FAIL", "fail");
			e.printStackTrace();
		}*/
		
		/**
		 * DEPRECATED: using contours to find all shapes then filling them and getting data. Kept for posterity but never went anywhere.
		 */
        //List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        //Imgproc.findContours(binMat, contours, new Mat(mapData.length, mapData[0].length, CvType.CV_8UC4), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

		/**
		 * DEPRECATED: Configure grayscale bitmap, used in sketch filter 
		 */
    	//Mat m = new Mat(bmp.getWidth(), bmp.getHeight(), );
    	//Bitmap b = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ALPHA_8);
    	//mat.put(0, 0, buff);
        //return bmpMono;
		
		/**
		 * Turn binary mat into a bitmap, export it.
		 */
		Bitmap bmpMono = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(binMat, bmpMono);
        return bmpMono;

	}
		/**
		 * DEPRECATED: Source code for Otsu's algorithm, in Javascript.
		 */
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

	/**
	 * DEPRECATED: First attempt to use circles, didn't go far.
	 */
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

	/**
	 * Callback for when OpenCV is turned on. Not really used for anything besides logging.
	 */
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: 
				break;
			default: 
				super.onManagerConnected(status);
				break;
			}
		}
	};

	/**
	 * Invoked when user resumes app after sleeping, reopens OpenCV.
	 */
	@Override
	public void onResume() {
		super.onResume();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this,
				mLoaderCallback);
	}

	/**
	 * When user exits kill app.
	 */
	public void onDestroy() {
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
		super.onDestroy();
	}

	/**
	 * Invoked when camera intent has started camera view.
	 */
	@Override
	public void onCameraViewStarted(int width, int height) {
		//   This method is invoked when camera preview has started.
	}

	/**
	 * Invoked when camera view stops.
	 */
	@Override
	public void onCameraViewStopped() {
		// This method is invoked when camera preview has been stopped for some reason.
	}

	/**
	 * Invoked on camera photograph, returns image data.
	 * @return
	 */
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		return inputFrame.rgba();
	}

	/**
	 * Not sure this does anything but required for interface purposes.
	 */
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
