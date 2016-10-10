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
import ev3Navigation.Navigation;

public class EvadeMode extends Thread{
	
	private EV3LargeRegulatedMotor sensorMotor, leftMotor, rightMotor;
	private static final int WHEEL_SPEED = 200;
	private static final int SCAN_SPEED = 100;
	private static final int ANALYSE_SPEED = 30;
	private static final int rightAngle = 45;
	private static final int leftAngle = -45;
	int bandCenter = 19;
	int bandWidth = 3;
	int distance;
	boolean detecting = true;
	boolean analysing = true;
	boolean evading = true;
	private SampleProvider us;
	private float[] usData;
	double wr,width;
	
	
	public EvadeMode (EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3LargeRegulatedMotor sensorMotor, SampleProvider us, float[] usData, double wheelRadius, double width){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.sensorMotor = sensorMotor;
		this.us = us;
		this.usData = usData;
		this.wr = wheelRadius;
		this.width = width;
		
	}

	public void run() {
		//for testing
		leftMotor.setSpeed(WHEEL_SPEED);
		rightMotor.setSpeed(WHEEL_SPEED);
		leftMotor.forward();
		rightMotor.forward();
		
		
		int distance;
		while (detecting) {
			// Scanning the surrounding, stop when the robot encounter an obstacle
			sensorMotor.setSpeed(SCAN_SPEED);
			if (sensorMotor.getTachoCount()>5){
				sensorMotor.rotateTo(leftAngle,true);
			} else {
			sensorMotor.rotateTo(rightAngle,true);
			}
			
			while (sensorMotor.isMoving()){
				us.fetchSample(usData,0);							// acquire data
				distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
				filter(distance);
			
				if(distance <= bandCenter){
					sensorMotor.stop();
					leftMotor.stop(true);
					rightMotor.stop(false);
					Sound.beep();
					detecting = false;
				}
		
				try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
			}		
		}
		
		// Analyse the position of the obstacle
		sensorMotor.rotateTo(-90);
		int closestAngle = 0;
		int closestPoint = bandCenter;
		sensorMotor.setSpeed(ANALYSE_SPEED);
		while (analysing){
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			filter(distance);
			sensorMotor.rotateTo(90,true);
			if (distance<=closestPoint){
				closestAngle = sensorMotor.getTachoCount();
				closestPoint = distance;
			}
			if (sensorMotor.getTachoCount() == 90){
				analysing = false;
			}
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
		sensorMotor.setSpeed(SCAN_SPEED);
		// Evading the obstacle
		
		if (closestAngle <= 0){
			sensorMotor.rotateTo(-90);
			leftMotor.rotate(convertAngle(wr, width, (double) Math.abs(closestAngle)), true);
			rightMotor.rotate(-convertAngle(wr, width, (double) Math.abs(closestAngle)), false);
		} else{
			sensorMotor.rotateTo(90);
			leftMotor.rotate(-convertAngle(wr, width, (double) Math.abs(closestAngle)), true);
			rightMotor.rotate(convertAngle(wr, width, (double) Math.abs(closestAngle)), false);
		}
			
		while (evading){
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			filter(distance);
			while (distance <= bandCenter+bandWidth){
				leftMotor.forward();
				rightMotor.forward();
			}
			leftMotor.rotate(convertDistance(wr, 2*width), true); 
			rightMotor.rotate(convertDistance(wr, 2*width), false);
			
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
				
		}
			
		
		
		
				
				
	
	
	public int readUSDistance() {
		return this.distance;
	}
		
	
	
	// Filtering out bad results
	public void filter(int distance){
		//int errorDistance = bandCenter - this.distance;
		
		int FILTER_OUT = 25;
		int filterControl = 0;
		
		// rudimentary filter - copied from TA code on myCourses
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
	
	private static int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}

	private static int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
	
}



