package ev3Navigation;

import lejos.hardware.Sound;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;


public class AdvancedNavigation extends Thread {
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor, sensorMotor;
	int bandCenter = 11;
	int bandWidth = 3;
	int distance;
	private SampleProvider us;
	private float[] usData;

	
	
	public AdvancedNavigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3LargeRegulatedMotor sensorMotor, SampleProvider us, float[] usData){
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.sensorMotor = sensorMotor;
		this.us = us;
		this.usData = usData;
	}
	
	//constants
	private static final int SCAN_SPEED = 175;
	private static final int rightAngle = 55;
	private static final int leftAngle = -55;
	private static final int criticalAngle = 10;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double WHEEL_RADIUS = NavLab.WHEEL_RADIUS;
	private static final double WHEEL_BASE = NavLab.WHEEL_BASE;
	private static final double PI = Math.PI;

	private static boolean navigating = true;

	@Override
	public void run() {
		travelTo(0,60);
		travelTo(60,0);
	}
	
	public void travelTo(double x, double y) {
		
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(3000);
		}
		
		
		double trajectoryX = x - odometer.getX();
		double trajectoryY = y - odometer.getY();
		double trajectoryAngle = Math.atan2(trajectoryX, trajectoryY);
		
		Sound.beepSequenceUp();
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		turnTo(trajectoryAngle);
		
		double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
		
		Sound.beepSequence();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistanceForMotor(WHEEL_RADIUS, trajectoryLine),true);
		rightMotor.rotate(convertDistanceForMotor(WHEEL_RADIUS, trajectoryLine),true);
		
		int distance;
		sensorMotor.resetTachoCount();
		sensorMotor.setSpeed(SCAN_SPEED);
		
			
		while (leftMotor.isMoving() || rightMotor.isMoving()) {
			while (!sensorMotor.isMoving()){
			if (sensorMotor.getTachoCount()>=criticalAngle){
				sensorMotor.rotateTo(leftAngle,true);
			} else {
				sensorMotor.rotateTo(rightAngle,true);
			}
			}
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			filter(distance);
			
			if(distance <= bandCenter){
				Sound.beep();
				leftMotor.stop(true);
				rightMotor.stop(false);
				navigating = false;
			}
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
		
		if (!this.isNavigating()){
			avoidObstacle();
			sensorMotor.rotateTo(0);
			navigating = true;
			travelTo(x,y);
			return;
		}
		sensorMotor.rotateTo(0);
		
	}
	
	public void turnTo(double theta) {
		
		double angle = theta-odometer.getTheta();
		
		if (angle > PI) {
			angle = 2*PI - angle;
		} else if (angle < -PI) {
			angle = angle + 2*PI;
		}
		
		leftMotor.rotate(convertAngleForMotor(WHEEL_RADIUS, WHEEL_BASE, angle),true);
		rightMotor.rotate(-convertAngleForMotor(WHEEL_RADIUS, WHEEL_BASE, angle),false);
	}
	
	public boolean isNavigating() {
		return navigating;
	}
	
	private int convertDistanceForMotor(double radius, double distance){
		return (int) (360*distance/(2*PI*radius));
	}
	
	private int convertAngleForMotor(double radius, double width, double angle){
		return convertDistanceForMotor(radius, width*angle/2);
	}
	
	public void avoidObstacle(){
		turnTo(odometer.getTheta()-Math.PI/2);
		sensorMotor.rotateTo(80);
		
		double endAngle = odometer.getTheta()+Math.PI*0.8;
		
		while (odometer.getTheta()<endAngle){
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			int errorDistance = bandCenter - distance;
			
			if (Math.abs(errorDistance)<= bandWidth){ //moving in straight line
				leftMotor.setSpeed(150);
				rightMotor.setSpeed(150);
				leftMotor.forward();
				rightMotor.forward();
			} else if (errorDistance > 0){ //too close to wall
				rightMotor.setSpeed(150); 
				leftMotor.setSpeed(60);// Setting the outer wheel to reverse
				rightMotor.forward();
				leftMotor.backward();
			} else if (errorDistance < 0){ // getting too far from the wall
				rightMotor.setSpeed(150);
				leftMotor.setSpeed(275);// Setting the outer wheel to move faster
				rightMotor.forward();
				leftMotor.forward();
			}
		}
		Sound.beep();
		leftMotor.stop();
		rightMotor.stop();
		
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

}
