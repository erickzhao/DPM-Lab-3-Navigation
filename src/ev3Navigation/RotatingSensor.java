package ev3Navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;
import ev3Navigation.UltrasonicController;
import ev3Navigation.UltrasonicPoller;
import lejos.hardware.Sound;

public class RotatingSensor extends Thread implements UltrasonicController {
	
	private EV3LargeRegulatedMotor sensorMotor, leftMotor, rightMotor;
	private static final int SCAN_SPEED = 0;
	private static final int rightAngle = 70;
	private static final int leftAngle = -70;
	int bandCenter = 25;
	int bandWidth = 5;
	private SampleProvider usDistance;
	private float[] usData;
	// for testing only
	int Speed = 100;
	int distance;
	int FILTER_OUT = 15;
	int filterControl = 0;
	
	
	public RotatingSensor(EV3LargeRegulatedMotor sensorMotor, EV3LargeRegulatedMotor leftMotor, 
			EV3LargeRegulatedMotor rightMotor, SampleProvider usDistance, float[] usData){
		this.sensorMotor = sensorMotor;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.usDistance = usDistance;
		this.usData = usData;
		
		
		
	}
	
	// required for Thread
	public void run(){
		sensorMotor.resetTachoCount();
		// for testing only
		leftMotor.setSpeed(Speed);
		rightMotor.setSpeed(Speed);
		leftMotor.forward();
		rightMotor.forward();
		
		scanSurrounding(bandCenter);

		usController evade = new usController(this.leftMotor, this.rightMotor, this.bandCenter, 
				this.bandWidth, sensorMotor.getTachoCount());
		UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, evade);
		usPoller.start();
	}
	
		
	// Scanning the surrounding until detecting an obstacle
	public void scanSurrounding(int threshold){
		
		scan:
		while (true){
			sensorMotor.setSpeed(SCAN_SPEED);
			usDistance.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);
			
			sensorMotor.rotateTo(rightAngle,true);
			while (sensorMotor.isMoving()){
				if(distance <= threshold){
					sensorMotor.stop();
					leftMotor.stop(true);
					rightMotor.stop(false);
					Sound.beep();
					break scan;
				}
			}
			sensorMotor.rotateTo(leftAngle, true);
			while (sensorMotor.isMoving()){
				if(distance <= threshold){
					sensorMotor.stop();
					leftMotor.stop(true);
					rightMotor.stop(false);
					Sound.beep();
					break scan;
				}				
			}
			try { Thread.sleep(50); } catch(Exception e){}	
		}
		
	}
	

	
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
	@Override
	public void processUSData(int distance) {

		// rudimentary filter - toss out invalid samples corresponding to null
		// signal. taken from myCourses.
		if (distance >= 255 && filterControl < FILTER_OUT) {
			// bad value, do not set the distance var, however do increment the
			// filter value
			filterControl++;
		} else if (distance >= 255) {
			// We have repeated large values, so there must actually be nothing
			// there: leave the distance alone
			this.distance = distance;
		} else {
			// distance went below 255: reset filter and leave
			// distance alone.
			filterControl = 0;
			this.distance = distance;
		}
	}
	
	
	
}
