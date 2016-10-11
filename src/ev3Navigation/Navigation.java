package ev3Navigation;

import lejos.hardware.Sound;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation extends Thread {
	
	private Odometer odometer;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	public Navigation(Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor){
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
	}
	
	//constants
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double WHEEL_RADIUS = NavLab.WHEEL_RADIUS;
	private static final double WHEEL_BASE = NavLab.WHEEL_BASE;
	private static final double PI = Math.PI;
	
	
	private static boolean navigating = false;

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
		
		navigating = true;
		
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
		rightMotor.rotate(convertDistanceForMotor(WHEEL_RADIUS, trajectoryLine),false);
	}
	
	public void turnTo(double theta) {
		
		double angle = theta-odometer.getTheta();
		
		leftMotor.rotate(convertAngleForMotor(WHEEL_RADIUS, WHEEL_BASE, angle),true);
		rightMotor.rotate(-convertAngleForMotor(WHEEL_RADIUS, WHEEL_BASE, angle),false);
	}
	
	public static boolean isNavigating() {
		return navigating;
	}
	
	private int convertDistanceForMotor(double radius, double distance){
		return (int) (360*distance/(2*PI*radius));
	}
	
	private int convertAngleForMotor(double radius, double width, double angle){
		return convertDistanceForMotor(radius, width*angle/2);
	}


}
