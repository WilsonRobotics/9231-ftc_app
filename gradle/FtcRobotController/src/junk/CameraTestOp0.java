/**
 * CameraTestOp - a simple operating mode that tries to acquire an image from the
 * phone camera and get some data from the image
 * Created by phanau on 12/9/15.
 */

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.nio.IntBuffer;
import java.util.List;

// a simple class describing an RGB pixel
class Pixel {
    int mR, mG, mB;
    public Pixel(int r, int g, int b) {
        mR = r; mG = g; mB = b;
    }
    public int red() { return mR; }
    public int green() { return mG; }
    public int blue() { return mB; }
    public String toString() { return "pixel("+red()+","+green()+","+blue()+")"; }
}

// a simple wrapper around the data returned by the camera callback
// assumes the data is in RGB_565 format (for now)
class CameraImage {
    Camera.Size mSize;
    int mBpp;       // bytes per pixel
    byte[] mData;
    Bitmap mBitmap; // converted to RGB?

    public CameraImage(final byte[] imageData, Camera c) {
        mData = imageData;      // reference to (readonly) image data
        Camera.Parameters camParms = c.getParameters();
        int picFormat = camParms.getPictureFormat();
        assert(picFormat == PixelFormat.RGB_565);
        mBpp = 2; // assuming picFormat really is RGB_565
        mSize = camParms.getPictureSize();
        mBitmap = YUVtoRGB.convert(mData, mSize.width, mSize.height);
    }

    public Camera.Size cameraSize() {
        return mSize;
    }

    public int dataSize() { return mData.length; }

    public String dataToString(int count) {
        String s = "";
        for (int i=0; i<count; i++) {
            int b = mData[i];
            if (b < 0)      // treat data as unsigned
                b += 256;
            s += String.valueOf(b)+",";
        }
        return s;
    }

    // return the pixel at {x,y)
    public int getPixel(int x, int y) {
        return mBitmap.getPixel(x,y);
    }

}


public class CameraTestOp extends OpMode {

    boolean mDataReady;
    int mLoopCount;

    private Camera mCamera;
    SurfaceTexture mDummyTexture;

    CameraImage mPreviewImage;

    String mErrMsg;

    Camera.PreviewCallback mPreviewCallback = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] imageData, Camera camera) {
            // process the frame and save results in member variables
            // ...
            mPreviewImage = new CameraImage(imageData, camera);

            // tell loop() that there is valid data available
            mDataReady = true;
        }
    };



    // Constructor
    public CameraTestOp() {
    }

    @Override
    public void init() {
        mDataReady = false;
        mLoopCount = 0;
        mErrMsg = "okay";

        try {
            mCamera = Camera.open();
            Camera.Parameters parameters = mCamera.getParameters();
            List<Integer> previewFormats = parameters.getSupportedPreviewFormats();
            parameters.setPreviewFormat(ImageFormat.NV21);       // choices are NV21(17) and YV12(842094169)
            List<Camera.Size> previewSizes = parameters.getSupportedPictureSizes();
            int iPreviewSize = 17;  // use (17)160x120 or (15)320x240 entry
            parameters.setPreviewSize(previewSizes.get(iPreviewSize).width, previewSizes.get(iPreviewSize).height);
            mCamera.setParameters(parameters);
        }
        catch (Exception e) {
            telemetry.addData("error: ", "cannot connect to camera");
            return;
        }

        mDummyTexture = new SurfaceTexture(1); // pick a random argument for texture id
        try {
            mCamera.setPreviewTexture(mDummyTexture);
        }
        catch (Exception e) {}

    }


    public void loop() {
        // post some debug data
        telemetry.addData("loop count:", mLoopCount);

        if (mCamera == null)
            return;

        if (mLoopCount == 0)
            try {
                mCamera.setPreviewCallback(mPreviewCallback);
                mCamera.startPreview();
            }
            catch (Exception e) {
                mErrMsg = "cannot take first picture";
            }

        mLoopCount++;

        // debug info ...
        if (mPreviewImage != null) {
            Camera.Size camSize = mPreviewImage.cameraSize();
            telemetry.addData("preview camera size: ", String.valueOf(camSize.width) + "x" + String.valueOf(camSize.height));
            telemetry.addData("preview data size:", mPreviewImage.dataSize());
            telemetry.addData("preview data: ", mPreviewImage.dataToString(8));
            telemetry.addData("preview rgb(0,0):", String.format("%08X", mPreviewImage.getPixel(0, 0)));
            telemetry.addData("preview exp comp:", String.valueOf(mCamera.getParameters().getExposureCompensation()) +
                                "  step:" + mCamera.getParameters().getExposureCompensationStep());

            // process the current frame
            // ... "move toward the light..."

            // start another frame
            mDataReady = false;
            mErrMsg = "okay";
            try {
                mCamera.setPreviewCallback(mPreviewCallback);
                mCamera.startPreview();
            }
            catch (Exception e) {
                mErrMsg = "cannot take picture #"+String.valueOf(mLoopCount);
            }
        }
    }

    public void stop() {
        mCamera.stopPreview();
        mCamera.release();
    }

}

class YUVtoRGB {

    static Bitmap convert(byte[] data, int imageWidth, int imageHeight) {

        // the bitmap we want to fill with the image
        Bitmap bitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888);
        int numPixels = imageWidth * imageHeight;

        // the buffer we fill up which we then fill the bitmap with
        IntBuffer intBuffer = IntBuffer.allocate(imageWidth * imageHeight);
        // If you're reusing a buffer, next line imperative to refill from the start,
        // if not good practice
        intBuffer.position(0);

        // Set the alpha for the image: 0 is transparent, 255 fully opaque
        final byte alpha = (byte) 255;

        // Get each pixel, one at a time
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                // Get the Y value, stored in the first block of data
                // The logical "AND 0xff" is needed to deal with the signed issue
                int Y = data[y * imageWidth + x] & 0xff;

                // Get U and V values, stored after Y values, one per 2x2 block
                // of pixels, interleaved. Prepare them as floats with correct range
                // ready for calculation later.
                int xby2 = x / 2;
                int yby2 = y / 2;

                // make this V for NV12/420SP
                float U = (float) (data[numPixels + 2 * xby2 + yby2 * imageWidth] & 0xff) - 128.0f;

                // make this U for NV12/420SP
                float V = (float) (data[numPixels + 2 * xby2 + 1 + yby2 * imageWidth] & 0xff) - 128.0f;

                // Do the YUV -> RGB conversion
                float Yf = 1.164f * ((float) Y) - 16.0f;
                int R = (int) (Yf + 1.596f * V);
                int G = (int) (Yf - 0.813f * V - 0.391f * U);
                int B = (int) (Yf + 2.018f * U);

                // Clip rgb values to 0-255
                R = R < 0 ? 0 : R > 255 ? 255 : R;
                G = G < 0 ? 0 : G > 255 ? 255 : G;
                B = B < 0 ? 0 : B > 255 ? 255 : B;

                // Put that pixel in the buffer
                intBuffer.put(alpha * 16777216 + R * 65536 + G * 256 + B);
            }
        }

        // Get buffer ready to be read
        intBuffer.flip();

        // Push the pixel information from the buffer onto the bitmap.
        bitmap.copyPixelsFromBuffer(intBuffer);

        return bitmap;
    }
}
