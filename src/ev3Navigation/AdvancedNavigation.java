package ev3Navigation;

import lejos.hardware.Sound;
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
	private static final int RIGHT_ANGLE = 55;
	private static final int LEFT_ANGLE = -55;
	private static final int CRITICAL_ANGLE = 10;
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final int OBSTACLE_SENSOR_ANGLE = 80;
	private static final int OBSTACLE_FWD_SPEED = 150;
	private static final int OBSTACLE_TURN_IN_SPEED = 275;
	private static final int OBSTACLE_TURN_OUT_SPEED = 60;
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
		leftMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
		rightMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
		
		int distance;
		sensorMotor.resetTachoCount();
		sensorMotor.setSpeed(SCAN_SPEED);
		
			
		while (leftMotor.isMoving() || rightMotor.isMoving()) {
			while (!sensorMotor.isMoving()){
			if (sensorMotor.getTachoCount()>=CRITICAL_ANGLE){
				sensorMotor.rotateTo(LEFT_ANGLE,true);
			} else {
				sensorMotor.rotateTo(RIGHT_ANGLE,true);
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
		
		double angle = getMinAngle(theta-odometer.getTheta());
		
		leftMotor.rotate(convertAngleForMotor(angle),true);
		rightMotor.rotate(-convertAngleForMotor(angle),false);
	}
	
	public double getMinAngle(double angle){
		if (angle > PI) {
			angle = 2*PI - angle;
		} else if (angle < -PI) {
			angle = angle + 2*PI;
		}
		
		return angle;
	}
	
	public boolean isNavigating() {
		return navigating;
	}
	
	private int convertDistanceForMotor(double distance){
		return (int) (360*distance/(2*PI*WHEEL_RADIUS));
	}
	
	private int convertAngleForMotor(double angle){
		return convertDistanceForMotor(WHEEL_BASE*angle/2);
	}
	
	public void avoidObstacle(){
		turnTo(odometer.getTheta()-PI/2);
		sensorMotor.rotateTo(OBSTACLE_SENSOR_ANGLE);
		
		double endAngle = odometer.getTheta()+PI*0.8;
		
		while (odometer.getTheta()<endAngle){
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			int errorDistance = bandCenter - distance;
			
			if (Math.abs(errorDistance)<= bandWidth){ //moving in straight line
				leftMotor.setSpeed(OBSTACLE_FWD_SPEED);
				rightMotor.setSpeed(OBSTACLE_FWD_SPEED);
				leftMotor.forward();
				rightMotor.forward();
			} else if (errorDistance > 0){ //too close to wall
				leftMotor.setSpeed(OBSTACLE_TURN_OUT_SPEED);// Setting the outer wheel to reverse
				rightMotor.setSpeed(OBSTACLE_FWD_SPEED); 
				leftMotor.backward();
				rightMotor.forward();
			} else if (errorDistance < 0){ // getting too far from the wall
				rightMotor.setSpeed(OBSTACLE_FWD_SPEED);
				leftMotor.setSpeed(OBSTACLE_TURN_IN_SPEED);// Setting the outer wheel to move faster
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
