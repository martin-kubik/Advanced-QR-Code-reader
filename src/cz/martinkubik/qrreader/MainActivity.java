package cz.martinkubik.qrreader;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.graphics.Bitmap;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;


/*
 * implements CvViewFrameListener2 interface to get frames from camera and process it:
 */
public class MainActivity extends Activity implements CvCameraViewListener2{
	
	private CameraBridgeViewBase mOpenCvCameraView;
	private static final String    TAG = "Advanced QR Code reader";
	   
 	/**
	 OpenCV recommends dynamic initialization to use hardware optimized code,
	 but simultaneously it require installed OpenCV Manager app.
	 Static initialization removes this requirement.
	 */
	private static final boolean USE_STATIC_INITIALIZATION = true;

	static {
		if (USE_STATIC_INITIALIZATION) {
			if (!OpenCVLoader.initDebug()) {
				// Handle initialization error
			} else {
				System.loadLibrary("native_code");
			}
		}
	}
/*
 * View modes for switch-case structure at onCameraFrame method
 */
	private int                    mViewMode;
	private static final int       VIEW_MODE_QR_READER  = 0;
	private static final int       VIEW_MODE_RGBA = 1;
	private static final int       VIEW_MODE_GRAY = 2;
	private static final int       VIEW_MODE_ADAPT = 3;
	private static final int       VIEW_MODE_OTSU = 4;
	private static final int       VIEW_MODE_erode = 5;
	private static final int       VIEW_MODE_FIND_CONTOURS = 6;
	private static final int       VIEW_MODE_FIND_SQUARES = 7;
	private static final int       VIEW_MODE_PERSP_TRANS = 8;
    
/*  
 * items for menu:
 */
    private MenuItem               mItemPreviewQRreader;
    private MenuItem               mItemPreviewRGBA;
    private MenuItem               mItemPreviewGRAY;
    private MenuItem               mItemPreviewADAPT;
    private MenuItem               mItemPreviewOTSU;
    private MenuItem               mItemPreviewerode;
    private MenuItem               mItemPreviewFindContours;
    private MenuItem               mItemPreviewFindSquares;
    private MenuItem               mItemPreviewPerspTrans;
    
    private boolean doRgba = false;
    private boolean doGray = false;
    private boolean doThresh = false;
    private boolean doAdaptOnly = false;
    private boolean doOtsuOnly = false;
    private boolean doErode = false;
    private boolean doFindContours = false;
    private boolean doFindSquares = false;
    private boolean doPersTrans = false;
    private boolean doQRreader = false;
    
    private boolean endWithRgba = false;
    private boolean endWithGray = false;
    private boolean endWithAdaptOnly = false;
    private boolean endWithOtsuOnly = false;
    private boolean endWithErode = false;
    private boolean endWithContours = false;
    private boolean endWithSquares = false;
    private boolean endWithPersTrans = false;
    
/*  
 * create Mats:
 */
	private Mat mRgba;
	private Mat mGray;
	private Mat qr_code2;
	
/*
 * TextView and string for ZXing result
 */
	
	TextView qr_message2;
	private String m_QRcodeString2;	

	
	TextView avgBrightTextView;
	 private String avgBright;	
	 
	
/*
 * required method of CvViewFrameListener2 interface and set some erode elements
 */	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		  Log.i(TAG, "called onCreate");
	        super.onCreate(savedInstanceState);
	        
	        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

	        
	        setContentView(R.layout.activity_main);

	        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.activity_main_surface_view);
	        mOpenCvCameraView.setCvCameraViewListener(this);
	      //  mOpenCvCameraView.setMaxFrameSize(640, 480);

	        
	        if (USE_STATIC_INITIALIZATION) {
	        	 mOpenCvCameraView.enableView();
			}
	        
	        qr_message2 = (TextView) findViewById(R.id.qrcode_string2);
	       avgBrightTextView = (TextView) findViewById(R.id.avg_bright);
	      
	       }
	
	@Override
	public void onResume()
	{
		Log.i(TAG, "onResume");
		super.onResume();
		if (!OpenCVLoader.initDebug()) {
            // TODO: Handle initialization error
        } else {
            // manual call init code since we don't use Manager when call
            // OpenCVLoader.initDebug()
        	System.loadLibrary("native_code");
        }
	 	 mOpenCvCameraView.enableView();
	   
	}
	
	public void onStop() {
		super.onStop();
	}
	
/*
 * create menu
 */	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewQRreader = menu.add("QR reader");
        mItemPreviewRGBA= menu.add("RGBA");
        mItemPreviewGRAY= menu.add("Gray");
        mItemPreviewADAPT= menu.add("Adapt. th.");
        mItemPreviewOTSU=menu.add("Otsu");
        mItemPreviewerode= menu.add("Erode");
        mItemPreviewFindContours=menu.add("Contours");
        mItemPreviewFindSquares=menu.add("Squares");
        mItemPreviewPerspTrans=menu.add("Persp. trans.");
   
        return true;
    }
    
    
   /*
    * set mode for switch-case structure at onCameraFrame method
    */	
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mItemPreviewQRreader) {
            mViewMode = VIEW_MODE_QR_READER;
        }
        else if (item == mItemPreviewRGBA) {
            mViewMode = VIEW_MODE_RGBA;
        }
        else if (item == mItemPreviewGRAY) {
            mViewMode = VIEW_MODE_GRAY;
        }
        else if (item == mItemPreviewADAPT) {
            mViewMode = VIEW_MODE_ADAPT;
        }
        else if (item == mItemPreviewOTSU) {
            mViewMode = VIEW_MODE_OTSU;
        }
        else if (item == mItemPreviewerode) {
            mViewMode = VIEW_MODE_erode;
        } 
        else if (item == mItemPreviewFindContours) {
            mViewMode = VIEW_MODE_FIND_CONTOURS;
        }
        else if (item == mItemPreviewFindSquares) {
            mViewMode = VIEW_MODE_FIND_SQUARES;
        }
        else if (item == mItemPreviewPerspTrans) {
            mViewMode = VIEW_MODE_PERSP_TRANS;
        }

        return true;
    }
	
 /*
  * required method of CvViewFrameListener2 interface
  */	
	 @Override
	 public void onPause()
	 {
	     super.onPause();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	     super.onPause();
	 }
	 
/*
 * required method of CvViewFrameListener2 interface
 */	
	 public void onDestroy() {
	     super.onDestroy();
	     if (mOpenCvCameraView != null)
	         mOpenCvCameraView.disableView();
	 }

/*
 * required method of CvViewFrameListener2 interface, set main Mats
 */	
	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
		
		qr_code2 = new Mat (200, 200, CvType.CV_8UC1);
	
			}

/*
 * required method of CvViewFrameListener2 interface
 */	
	@Override
	public void onCameraViewStopped() {
		 mRgba.release();
		 mGray.release();
		
		qr_code2.release();
		
		

	}
	
/*	
 * The most important method
 * The input is frame from camera.
 * The output is mRgba frame after processing
 */
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		
		mRgba = inputFrame.rgba();
    	mGray = inputFrame.gray();
		
		 final int viewMode = mViewMode;
	        switch (viewMode) {
		
	        case VIEW_MODE_RGBA:
	        	
	            doRgba = true;
	            doGray = false;
	            doThresh = false;
	            doAdaptOnly = false;
	            doOtsuOnly = false;
	            doErode = false;
	            doFindContours = false;
	            doFindSquares = false;
	            doPersTrans = false;
	            doQRreader = false;
	            
	            endWithRgba = true;
	            endWithGray = false;
	            endWithAdaptOnly = false;
	            endWithOtsuOnly = false;
	            endWithErode = false;
	            endWithContours = false;
	            endWithSquares = false;

	        	break;
    	
	        	case VIEW_MODE_GRAY:
	
	            doRgba = true;
	            doGray = true;
	            doThresh = false;
	            doAdaptOnly = false;
	            doOtsuOnly = false;
	            doErode = false;
	            doFindContours = false;
	            doFindSquares = false;
	            doPersTrans = false;
	            doQRreader = false;
	        	
	            endWithRgba = false;
	            endWithGray = true;
	            endWithAdaptOnly = false;
	            endWithOtsuOnly = false;
	            endWithErode = false;
	            endWithContours = false;
	            endWithSquares = false;

		    	break;
		    	
		        case VIEW_MODE_ADAPT:
	    		
		        	doRgba = true;
		            doGray = false;
		            doThresh = true;
		            doAdaptOnly = true;
		            doOtsuOnly = false;
		            doErode = false;
		            doFindContours = false;
		            doFindSquares = false;
		            doPersTrans = false;
		            doQRreader = false;
		        	
		            endWithRgba = false;
		            endWithGray = false;
		            endWithAdaptOnly = true;
		            endWithOtsuOnly = false;
		            endWithErode = false;
		            endWithContours = false;
		            endWithSquares = false;
	    	
	    		break;
    		
		    	case VIEW_MODE_OTSU:
		    		

		        	doRgba = true;
		            doGray = false;
		            doThresh = true;
		            doAdaptOnly = false;
		            doOtsuOnly = true;
		            doErode = false;
		            doFindContours = false;
		            doFindSquares = false;
		            doPersTrans = false;
		            doQRreader = false;
		        	
		            endWithRgba = false;
		            endWithGray = false;
		            endWithAdaptOnly = false;
		            endWithOtsuOnly = true;
		            endWithErode = false;
		            endWithContours = false;
		            endWithSquares = false;
		    		
		    	break;
		    		
		    	case VIEW_MODE_erode:
		    		
			    		doRgba = true;
			            doGray = false;
			            doThresh = true;
			            doAdaptOnly = false;
			            doOtsuOnly = false;
			            doErode = true;
			            doFindContours = false;
			            doFindSquares = false;
			            doPersTrans = false;
			            doQRreader = false;
			        	
			            endWithRgba = false;
			            endWithGray = false;
			            endWithAdaptOnly = false;
			            endWithOtsuOnly = false;
			            endWithErode = true;
			            endWithContours = false;
			            endWithSquares = false;
	

    		    break;
    		
		    	case VIEW_MODE_FIND_CONTOURS:
    		
		    	doRgba = true;
	            doGray = false;
	            doThresh = true;
	            doAdaptOnly = false;
	            doOtsuOnly = false;
	            doErode = true;
	            doFindContours = true;
	            doFindSquares = false;
	            doPersTrans = false;
	            doQRreader = false;
	        	
	            endWithRgba = false;
	            endWithGray = false;
	            endWithAdaptOnly = false;
	            endWithOtsuOnly = false;
	            endWithErode = false;
	            endWithContours = true;
	            endWithSquares = false;
	        	
	        	break;
			
        
				case VIEW_MODE_FIND_SQUARES:
				
					doRgba = true;
		            doGray = false;
		            doThresh = true;
		            doAdaptOnly = false;
		            doOtsuOnly = false;
		            doErode = true;
		            doFindContours = true;
		            doFindSquares = true;
		            doPersTrans = false;
		            doQRreader = false;
		        	
		            endWithRgba = false;
		            endWithGray = false;
		            endWithAdaptOnly = false;
		            endWithOtsuOnly = false;
		            endWithErode = false;
		            endWithContours = false;
		            endWithSquares = true;
		            
		        	break;
		        	
				case VIEW_MODE_PERSP_TRANS:
					doRgba = true;
		            doGray = false;
		            doThresh = true;
		            doAdaptOnly = false;
		            doOtsuOnly = false;
		            doErode = true;
		            doFindContours = true;
		            doFindSquares = true;
		            doPersTrans = true;
		            doQRreader = false;
		        	
		            endWithRgba = false;
		            endWithGray = false;
		            endWithAdaptOnly = false;
		            endWithOtsuOnly = false;
		            endWithErode = false;
		            endWithContours = false;
		            endWithSquares = false;
		            endWithPersTrans = true;
					
		            break;
	
				case VIEW_MODE_QR_READER:
	        	
					doRgba = true;
		            doGray = false;
		            doThresh = true;
		            doAdaptOnly = false;
		            doOtsuOnly = false;
		            doErode = true;
		            doFindContours = true;
		            doFindSquares = true;
		            doPersTrans = false;
		            doQRreader = true;
		            
		            endWithRgba = false;
		            endWithGray = false;
		            endWithAdaptOnly = false;
		            endWithOtsuOnly = false;
		            endWithErode = false;
		            endWithContours = false;
		            endWithSquares = false;
		            
	            try {
					
					zxing();
				} catch (ChecksumException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FormatException e) {
					// TODO Auto-generated catch  block
					e.printStackTrace();
				}
	            break;
	            
    }
	        
	       	callNative(
        			doRgba,
        			doGray,
        			doThresh,
        			doAdaptOnly,
        			doOtsuOnly,
        			doErode,
        			doFindContours,
        			doFindSquares,
        			doPersTrans,
        			doQRreader,

        			endWithRgba,
        		    endWithGray,
        		    endWithAdaptOnly,
        			endWithOtsuOnly,
        		    endWithErode,
        		    endWithContours,
            		endWithSquares,
        			
        			mRgba.getNativeObjAddr(),
           		    mGray.getNativeObjAddr(),
         
           		    qr_code2.getNativeObjAddr()
        			);
	        
	    	runOnUiThread(new Runnable() {

        		public void run() {
        			
        			 if( mViewMode == VIEW_MODE_QR_READER)
						    
        				 qr_message2.setVisibility(View.VISIBLE);
        			 	
        			 else
							  
        				 qr_message2.setVisibility(View.GONE);
						  
        			
        			 if(  endWithAdaptOnly == true || endWithOtsuOnly == true){
        				 
        				 int msgadapt = getMessageFromNative();
        				 avgBrightTextView.setText(String.valueOf(msgadapt));
        				 avgBrightTextView.setVisibility(View.VISIBLE);
        				 
        			 }
        			 
        			 else
							  
        				 avgBrightTextView.setVisibility(View.GONE);
		
        			}
        	});

	        
        return mRgba;
    }
	
    public native void callNative(
    		
    		boolean doRgba,
    		boolean doGray,
    		boolean doThresh,
    		boolean doAdaptOnly,
    		boolean doOtsuOnly,
    		boolean doErode,
    		boolean doFindContous,
    		boolean doFindSquares,
    		boolean doPersTrans,
    		boolean doQRreader,
    		
    		
    		boolean endWithRgba,
    		boolean endWithGray,
    		boolean endAdaptOnly,
    		boolean endOtsuOnly,
    		boolean endWithErode,
    		boolean endWithContours,
    		boolean endWithSquares,
    		
    		long matAddrRgba,
    		long matAddrGr,
    	
    		long matAddrQr_gray2
    		);

    public native int getMessageFromNative();

	public void zxing() throws ChecksumException, FormatException{

	Bitmap bMap2 = Bitmap.createBitmap(qr_code2.width(),qr_code2.height(), Bitmap.Config.ARGB_8888);
	Utils.matToBitmap(qr_code2, bMap2);
	int[] intArray2 = new int[bMap2.getWidth()*bMap2.getHeight()];  
	bMap2.getPixels(intArray2, 0, bMap2.getWidth(), 0, 0, bMap2.getWidth(), bMap2.getHeight());  
	LuminanceSource source2 = new RGBLuminanceSource(bMap2.getWidth(), bMap2.getHeight(),intArray2);
	
	BinaryBitmap bitmap2 = new BinaryBitmap(new HybridBinarizer(source2));
	
	Reader reader2 = new QRCodeReader();  
	
	try {
		  
		   final Result result2 = reader2.decode(bitmap2);   
	       Log.d(TAG,"Found something 2: "+result2.getText());
	   
	       	runOnUiThread(new Runnable() {

	           public void run() {
	        	   
	         	 m_QRcodeString2 = (""+result2.getText());
	           	 qr_message2.setText(m_QRcodeString2);
	           	 
	           	 ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
	           	 ClipData clip = ClipData.newPlainText("QR code content", qr_message2.getText());
	           	 clipboard.setPrimaryClip(clip);
	           	final Toast message = Toast.makeText(getApplicationContext(), "QR code content was copied to Clipboard", Toast. LENGTH_SHORT);
	         
	           	message.show();
	           	
	            Handler handler2 = new Handler();
	            handler2.postDelayed(new Runnable() {
	               @Override
	               public void run() {
	            	   message.cancel(); 
	               }
	        }, 2000);
	           }
	       	}); 
		   
	       }
	
 catch (NotFoundException e) {
         Log.d(TAG, "Code Not Found!");
         e.printStackTrace();
 }


}


}