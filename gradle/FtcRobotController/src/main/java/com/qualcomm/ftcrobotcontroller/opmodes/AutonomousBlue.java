package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.ftcrobotcontroller.opmodes.AutoLib;

import static java.lang.Thread.sleep;

/**
 * Created by Robotics on 11/17/2015.
 */
public class AutonomousBlue extends OpMode {
    DcMotor motorFrontRight;
    DcMotor motorFrontLeft;
    DcMotor motorBackRight;
    DcMotor motorBackLeft;
    DcMotor motorArm;
    DcMotor motorGuy;
    //Servo knockRight;
    //Servo knockLeft;
    Servo servoGuy;
    AutoLib.Sequence mSequence;     // the root of the sequence tree
    boolean bDone;                  // true when the programmed sequence is done
    @Override
    public void init() {
        motorFrontRight = hardwareMap.dcMotor.get("fr");
        motorFrontLeft = hardwareMap.dcMotor.get("fl");
        motorBackRight = hardwareMap.dcMotor.get("br");
        motorBackLeft = hardwareMap.dcMotor.get("bl");
        motorArm = hardwareMap.dcMotor.get("arm");
        motorGuy = hardwareMap.dcMotor.get("guy");
        //knockLeft = hardwareMap.servo.get("kl");
        motorFrontLeft.setDirection(DcMotor.Direction.REVERSE);
        motorBackRight.setDirection(DcMotor.Direction.REVERSE);
        // create the root Sequence for this autonomous OpMode
        mSequence = new AutoLib.LinearSequence();

        // add a first simple Step to the root Sequence
        mSequence.add(new AutoLib.MoveByTime(motorFrontRight, motorBackRight, motorFrontLeft, motorBackLeft, 0, 10, false));
        mSequence.add(new AutoLib.TurnByTime(motorFrontRight, motorBackRight, motorFrontLeft, motorBackLeft, 1, 0.1, 3.1, false));
        mSequence.add(new AutoLib.MoveByTime(motorFrontRight, motorBackRight, motorFrontLeft, motorBackLeft, 1, 2.3, false));
        mSequence.add(new AutoLib.TurnByTime(motorFrontRight, motorBackRight, motorFrontLeft, motorBackLeft, 1, -1, 1.7, false));
        mSequence.add(new AutoLib.MoveByTime(motorFrontRight, motorBackRight, motorFrontLeft, motorBackLeft, -0.3, 1, true));
        mSequence.add(new AutoLib.TimedMotorStep(motorGuy, 0.5, 1, true));
        // start out not-done
        bDone = false;
    }

    @Override
    public void loop() {
        if (!bDone){
            bDone = mSequence.loop();       // returns true when we're done
        }
    }
}
