package app.akexorcist.ioiocamerarobot.ioio;

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Akexorcist on 9/5/15 AD.
 */
public class CameraManager implements Camera.PictureCallback, Camera.PreviewCallback {

    private CameraManagerListener listener;
    private Camera mCamera;
    private Camera.Parameters params;
    private Camera.Size pictureSize;
    private Camera.Size previewSize;

    private int selectedPreviewSize;
    int w, h;
    int[] rgbs;
    boolean initialed = false;

    public CameraManager(int selectedPreviewSize) {
        this.selectedPreviewSize = selectedPreviewSize;
    }

    public void setCameraManagerListener(CameraManagerListener listener) {
        this.listener = listener;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        int imageNum = 0;
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "DCIM/IOIOCameraRobot");
        imagesFolder.mkdirs();

        SimpleDateFormat sd = new SimpleDateFormat("yyyyMMdd-hhmmss");
        String date = sd.format(new Date());

        String fileName = "IMG_" + date + ".jpg";
        File output = new File(imagesFolder, fileName);
        while (output.exists()) {
            imageNum++;
            fileName = "IMG_" + date + "_" + String.valueOf(imageNum) + ".jpg";
            output = new File(imagesFolder, fileName);
        }

        try {
            FileOutputStream fos = new FileOutputStream(output);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.stopPreview();
        mCamera.setPreviewCallback(this);
        mCamera.startPreview();
        if (listener != null)
            listener.onPictureTaken(fileName, imagesFolder.getAbsolutePath());
    }

    public void setCameraOrientation(int orientation) {
        switch (orientation) {
            case OrientationManager.LANDSCAPE_NORMAL:
                mCamera.setDisplayOrientation(0);
                break;
            case OrientationManager.PORTRAIT_NORMAL:
                mCamera.setDisplayOrientation(90);
                break;
            case OrientationManager.LANDSCAPE_REVERSE:
                mCamera.setDisplayOrientation(180);
                break;
            case OrientationManager.PORTRAIT_REVERSE:
                mCamera.setDisplayOrientation(270);
                break;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (!initialed) {
            w = mCamera.getParameters().getPreviewSize().width;
            h = mCamera.getParameters().getPreviewSize().height;
            rgbs = new int[w * h];
            initialed = true;
        }

        if (data != null && listener != null) {
            try {
                decodeYUV420(rgbs, data, w, h);
                listener.onPreviewTaken(Bitmap.createBitmap(rgbs, w, h, Bitmap.Config.ARGB_8888));
            } catch (OutOfMemoryError e) {
                listener.onPreviewOutOfMemory(e);
            }
        }
    }

    public void startCameraPreview(SurfaceView surfaceView) {
        try {
            mCamera.setPreviewDisplay(surfaceView.getHolder());
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopCameraPreview() {
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initCameraParameter() {
        params = mCamera.getParameters();
        pictureSize = getMaxPictureSize(params);
        previewSize = params.getSupportedPreviewSizes().get(selectedPreviewSize);

        params.setPictureSize(pictureSize.width, pictureSize.height);
        params.setPreviewSize(previewSize.width, previewSize.height);
        params.setPreviewFrameRate(getMaxPreviewFps(params));

        params.setJpegQuality(100);
        mCamera.setParameters(params);
        mCamera.setPreviewCallback(this);
    }

    public Camera.Size getPreviewSize() {
        return previewSize;
    }

    public Camera.Size getPictureSize() {
        return pictureSize;
    }

    public Camera.Size getMaxPictureSize(Camera.Parameters params) {
        List<Camera.Size> pictureSize = params.getSupportedPictureSizes();
        int firstPictureWidth, lastPictureWidth;
        try {
            firstPictureWidth = pictureSize.get(0).width;
            lastPictureWidth = pictureSize.get(pictureSize.size() - 1).width;
            if (firstPictureWidth > lastPictureWidth)
                return pictureSize.get(0);
            else
                return pictureSize.get(pictureSize.size() - 1);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return pictureSize.get(0);
        }
    }

    public int getMaxPreviewFps(Camera.Parameters params) {
        List<Integer> previewFps = params.getSupportedPreviewFrameRates();
        int fps = 0;
        for (int i = 0; i < previewFps.size(); i++) {
            if (previewFps.get(i) > fps)
                fps = previewFps.get(i);
        }
        return fps;
    }

    public void createCameraInstance(SurfaceHolder holder) {
        try {
            mCamera = Camera.open(0);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void destroyCameraInstance() {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public interface CameraManagerListener {
        public void onPictureTaken(String filename, String path);
        public void onPreviewTaken(Bitmap bitmap);
        public void onPreviewOutOfMemory(OutOfMemoryError e);
    }

    public void requestAutoFocus() {
        if (mCamera != null)
            mCamera.autoFocus(null);
    }

    public boolean requestTakePicture() {
        if (mCamera != null) {
            mCamera.takePicture(null, null, null, this);
            return true;
        }
        return false;
    }

    public boolean isFlashAvailable() {
        return params.getSupportedFlashModes() != null;
    }

    public boolean requestFlashOn() {
        if (params.getSupportedFlashModes() != null) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
            return true;
        }
        return false;
    }

    public boolean requestFlashOff() {
        if (params.getSupportedFlashModes() != null) {
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            return true;
        }
        return false;
    }

    private void decodeYUV420(int[] rgb, byte[] yuv420, int width, int height) {
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420[yp])) - 16;
                if (y < 0) y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420[uvp++]) - 128;
                    u = (0xff & yuv420[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0) r = 0;
                else if (r > 262143) r = 262143;
                if (g < 0) g = 0;
                else if (g > 262143) g = 262143;
                if (b < 0) b = 0;
                else if (b > 262143) b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
}
