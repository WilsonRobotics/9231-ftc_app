/**
 * CameraTestOp - a simple operating mode that tries to acquire an image from the
 * phone camera and get some data from the image
 * Created by phanau on 12/9/15.
 */

package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import java.nio.IntBuffer;
import java.util.List;

import com.qualcomm.ftcrobotcontroller.opmodes.CameraLib;


public class CameraTestOp extends OpMode {

    int mLoopCount;
    CameraLib.CameraAcquireFrames mCamAcqFr;


    // Constructor
    public CameraTestOp() {
        mCamAcqFr = new CameraLib.CameraAcquireFrames();
    }

    @Override
    public void init() {
        mLoopCount = 0;

        if (mCamAcqFr.init(2) == false)     // init camera at 2nd smallest size
            telemetry.addData("error: ", "cannot initialize camera");

    }

    public void loop() {
        // post some debug data
        telemetry.addData("loop count:", mLoopCount++);
        telemetry.addData("version: ", "1.3");

        // get most recent frame from camera (may be same as last time or null)
        CameraLib.CameraImage frame = mCamAcqFr.loop();

        // log debug info ...
        if (frame != null) {

            // process the current frame
            // ... "move toward the light..."

            // log data about the most current image to driver station every loop so it stays up long enough to read
            Camera.Size camSize = frame.cameraSize();
            telemetry.addData("preview camera size: ", String.valueOf(camSize.width) + "x" + String.valueOf(camSize.height));
            telemetry.addData("preview data size:", frame.dataSize());
            telemetry.addData("preview rgb(center):", String.format("%08X", frame.getPixel(camSize.width / 2, camSize.height / 2)));
            telemetry.addData("frame number: ", mCamAcqFr.frameCount());

            // log text representations of several significant scanlines
            final int bandSize = 10;
            telemetry.addData("hue a(1/3): ", frame.scanlineHue(camSize.height / 3, bandSize));
            telemetry.addData("hue b(1/2): ", frame.scanlineHue(camSize.height / 2, bandSize));
            telemetry.addData("hue c(2/3): ", frame.scanlineHue(2*camSize.height / 3, bandSize));
            telemetry.addData("dom a(1/3): ", frame.scanlineDomColor(camSize.height / 3, bandSize));
            telemetry.addData("dom b(1/2): ", frame.scanlineDomColor(camSize.height / 2, bandSize));
            telemetry.addData("dom c(2/3): ", frame.scanlineDomColor(2*camSize.height / 3, bandSize));

        }
    }

    public void stop() {
        mCamAcqFr.stop();
    }

}

