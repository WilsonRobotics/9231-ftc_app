package com.qualcomm.ftcrobotcontroller.opmodes;

/**
 * OpMode to test AutoLib driving of real motors by time or encoder counts
 * Created by phanau on 12/14/15.
 */

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;


/**
 * A test example of autonomous opmode programming using AutoLib classes.
 * Created by phanau on 12/14/15.
 */


// a dummy DcMotor that just logs commands we send to it --
// useful for testing Motor code when you don't have real hardware handy
class TestMotor extends DcMotor {
    OpMode mOpMode;     // needed for logging data
    String mName;       // string id of this motor

    public TestMotor(String name, OpMode opMode) {
        super(null, 0);     // init base class (real DcMotor) with dummy data
        mOpMode = opMode;
        mName = name;
    }

    @Override       // this function overrides the setPower() function of the real DcMotor class
    public void setPower(double power) {
        mOpMode.telemetry.addData(mName, " power:"+String.valueOf(power));
    }
}


public class AutoMotorTest1 extends OpMode {

    AutoLib.Sequence mSequence;     // the root of the sequence tree
    boolean bDone;                  // true when the programmed sequence is done
    boolean bFirst;                 // true first time loop() is called

    DcMotor mFr, mBr, mFl, mBl;     // four drive motors (front right, back right, front left, back left)
    DcMotor mIo, mUd;               // two arm motors (in-out, up-down)

    boolean debug;                  // run in test/debug mode with dummy motors and data logging

    public AutoMotorTest1() {
        debug = true;
    }

    public void init() {

        if (debug) {
            // make some dummy motors that just log data
            mFr = new TestMotor("fr", this);
            mFl = new TestMotor("fl", this);
            mBr = new TestMotor("br", this);
            mBl = new TestMotor("bl", this);
            mIo = new TestMotor("io", this);
            mUd = new TestMotor("ud", this);
        }
        else {
            // get the real motors we'll be using
            mFr = hardwareMap.dcMotor.get("fr");
            mFl = hardwareMap.dcMotor.get("fl");
            mBr = hardwareMap.dcMotor.get("br");
            mBl = hardwareMap.dcMotor.get("bl");
            mFl.setDirection(DcMotor.Direction.REVERSE);
            mBl.setDirection(DcMotor.Direction.REVERSE);
            mIo = hardwareMap.dcMotor.get("io");
            mUd = hardwareMap.dcMotor.get("ud");
        }

        // create the root Sequence for this autonomous OpMode
        mSequence = new AutoLib.LinearSequence();

        // add a Step (actually, a ConcurrentSequence under the covers) that
        // drives all four motors forward at half power for 2 seconds
        mSequence.add(new AutoLib.MoveByTime(mFr, mBr, mFl, mBl, 0.5, 2.0, false));

        // create a second sequence that drives motors at different speeds
        // to turn left for 3 seconds, then stop all motors
        mSequence.add(new AutoLib.TurnByTime(mFr, mBr, mFl, mBl, 0.5, 0.2, 3.0, true));

        // raise the arm using encoders while also extending it for 1 second
        AutoLib.ConcurrentSequence cs1 = new AutoLib.ConcurrentSequence();
        if (debug)
            cs1.add(new AutoLib.TimedMotorStep(mUd, 0.75, 1.0, true)); // we don't support encoders yet in debug mode
        else
            cs1.add(new AutoLib.EncoderMotorStep(new EncoderMotor(mUd), 0.75, 1000, true));
        cs1.add(new AutoLib.TimedMotorStep(mIo, 0.5, 1.0, true));
        mSequence.add(cs1);

        // start out not-done, first time
        bDone = false;
        bFirst = true;
    }

    public void loop() {
        // reset the timer when we start looping
        if (bFirst) {
            this.resetStartTime();      // OpMode provides a timer
            bFirst = false;
        }

        // until we're done with the root Sequence, perform the current Step(s) each time through the loop
        if (!bDone) {
            bDone = mSequence.loop();       // returns true when we're done

            if (debug)
                telemetry.addData("elapsed time", this.getRuntime());
        }
    }

    public void stop() {
        telemetry.addData("stop() called", "");
    }
}
