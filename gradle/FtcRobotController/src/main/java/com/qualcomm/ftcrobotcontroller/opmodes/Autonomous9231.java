package com.qualcomm.ftcrobotcontroller.opmodes;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import static java.lang.Thread.sleep;

/**
 * Created by Robotics on 11/4/2015.
 */
public class Autonomous9231 extends OpMode{
    //Robot limb declaration
    DcMotor motorFrontRight;
    DcMotor motorFrontLeft;
    DcMotor motorBackRight;
    DcMotor motorBackLeft;
    DcMotor motorArm;
    Servo knockRight;
    Servo knockLeft;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    public Autonomous9231(){

    }
    @Override
    public void init() {
        	/*
		 * For the team 9231 bot we assume the following,
		 *   There are four motors
		 *   "fl" and "bl" are front and back left wheels
		 *   "fr" and "br" are front and back right wheels
		 *   "arm" is the lifting arm
		 *   "kr" and "kl" are the arms that are used
		 */
        motorFrontRight = hardwareMap.dcMotor.get("fr");
        motorFrontLeft = hardwareMap.dcMotor.get("fl");
        motorBackRight = hardwareMap.dcMotor.get("br");
        motorBackLeft = hardwareMap.dcMotor.get("bl");
        motorArm = hardwareMap.dcMotor.get("arm");
        knockRight = hardwareMap.servo.get("kr");
        knockLeft = hardwareMap.servo.get("kl");
        motorFrontLeft.setDirection(DcMotor.Direction.REVERSE);
        motorBackRight.setDirection(DcMotor.Direction.REVERSE);
        /*
        **==WHAT WE NEED TO DO==**
        * Climb up the hill
        *
        */


    }

    @Override
    public void loop() {
        DcMotor motors[] = {motorFrontLeft, motorFrontRight, motorBackLeft, motorBackRight};
        //Initialize motors
        motorFrontRight.setPower(1);
        motorBackRight.setPower(1);
        //Turn left
        try {
            sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Go forward
        for(int aa = 0; aa < motors.length; aa++){
            motors[aa].setPower(1);
        }
        try {
            sleep(5000);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Stop moving
        for(int aa = 0; aa < motors.length; aa++){
            motors[aa].setPower(0);
        }
        motorFrontLeft.setPower(1);
        motorBackLeft.setPower(1);
        //Turn right
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        motorFrontLeft.setPower(0);
        motorBackLeft.setPower(0);
        //Stop moving
        for(int aa = 0; aa < motors.length; aa++){
            motors[aa].setPower(1);
        }
        //Go forward
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(int aa = 0; aa < motors.length; aa++){
            motors[aa].setPower(0);
        }
        //Stop moving
    }

}
