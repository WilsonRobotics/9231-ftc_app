package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

/**
 * Created by Robotics on 3/18/2016.
 */
public class SeansTest extends OpMode {

    public void init(){

    }

    public void loop(){

        telemetry.addData("runtime",getRuntime());
    }
}
