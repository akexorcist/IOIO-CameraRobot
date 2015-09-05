package app.akexorcist.ioiocamerarobot.ioio;

import java.lang.reflect.Method;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;


public class OrientationManager {
	public final static int PORTRAIT_NORMAL = 0;
	public final static int PORTRAIT_REVERSE = 1;
	public final static int LANDSCAPE_NORMAL = 2;
	public final static int LANDSCAPE_REVERSE = 3;
	
	Context context;
	Activity activity;
	
	String device_orientation = "";
	
	public OrientationManager(Activity activity) {
		this.activity = activity;
		this.context = activity.getApplicationContext();

        int xres = 0, yres = 0;
        Method mGetRawH;
        Display display = activity.getWindowManager().getDefaultDisplay(); 
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
        	try {
    			mGetRawH = Display.class.getMethod("getRawHeight");
    	        Method mGetRawW = Display.class.getMethod("getRawWidth");
    	        xres = (Integer) mGetRawW.invoke(display);
    	        yres = (Integer) mGetRawH.invoke(display);
    		} catch (Exception e) {
    			xres = display.getWidth();
    			yres = display.getHeight();
    		}
    	} else if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
    		DisplayMetrics outMetrics = new DisplayMetrics ();
    		display.getRealMetrics(outMetrics);
			xres = outMetrics.widthPixels;
			yres = outMetrics.heightPixels;
    	}
        
        int hdp = (int)(yres * (1f / dm.density));
        int wdp = (int)(xres * (1f / dm.density));
        int sw = (hdp < wdp) ? hdp : wdp;
        device_orientation = (sw >= 720) ? "landscape" : "portrait";
	}
	
	public int getOrientation() {		
		WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = wm.getDefaultDisplay().getRotation();

        if(device_orientation.equals("portrait")) {
        	if(rotation == Surface.ROTATION_0) {
				return PORTRAIT_NORMAL;
			} else if(rotation == Surface.ROTATION_90) {
				return LANDSCAPE_NORMAL;
			} else if(rotation == Surface.ROTATION_180) {
				return PORTRAIT_REVERSE;
			} else if(rotation == Surface.ROTATION_270) {
				return LANDSCAPE_REVERSE;
			}
        } else if(device_orientation.equals("landscape")) {
        	if(rotation == Surface.ROTATION_0) {
				return LANDSCAPE_NORMAL;
			} else if(rotation == Surface.ROTATION_90) {
				return PORTRAIT_REVERSE;
			} else if(rotation == Surface.ROTATION_180) {
				return LANDSCAPE_REVERSE;
			} else if(rotation == Surface.ROTATION_270) {
				return PORTRAIT_NORMAL;
			}
        }
		return -1;
	}
}
