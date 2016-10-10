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

public class RotatingSensor extends Thread {
	
	private EV3LargeRegulatedMotor sensorMotor, leftMotor, rightMotor;
	private static final int SCAN_SPEED = 100;
	private static final int rightAngle = 70;
	private static final int leftAngle = -70;
	int bandCenter = 15;
	int bandWidth = 5;
	private SampleProvider usDistance;
	private float[] usData;
	// for testing only
	int Speed = 200;
	int distance;
	
	
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
		
		scanSurrounding(bandCenter+bandWidth);

		//usController evade = new usController(this.leftMotor, this.rightMotor, this.bandCenter, 
		//		this.bandWidth, sensorMotor.getTachoCount());
		//UltrasonicPoller usPoller = new UltrasonicPoller(usDistance, usData, evade);
		//usPoller.start();
	}
	
		
	// Scanning the surrounding until detecting an obstacle
	public void scanSurrounding(int errorDistance){
		sensorMotor.setSpeed(SCAN_SPEED);
		scan:
		while (true){
			usDistance.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);
			
			sensorMotor.rotateTo(rightAngle,true);
			while (sensorMotor.isMoving()){
				if(distance >= bandCenter){
					sensorMotor.stop(true);
					leftMotor.stop(true);
					rightMotor.stop(false);
					Sound.beep();
					break scan;
				}
			}
			sensorMotor.rotateTo(leftAngle, true);
			while (sensorMotor.isMoving()){
				if(distance >= bandCenter){
					sensorMotor.stop(true);
					leftMotor.stop(true);
					rightMotor.stop(false);
					Sound.beep();
					break scan;
				}				
			}
			try { Thread.sleep(50); } catch(Exception e){}	
		}
		
	}
	

	
	

	
	
	
}
