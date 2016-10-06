package ev3Navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation implements Runnable {
	
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final Odometer odometer = new Odometer(leftMotor, rightMotor);
	
	//constants
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double WHEEL_RADIUS = NavLab.WHEEL_RADIUS;
	private static final double WHEEL_BASE = NavLab.WHEEL_BASE;
	private static final double PI = Math.PI;
	
	private static boolean navigating = false;

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public static void travelTo(double x, double y) {
		
		navigating = true;
		
		double trajectoryX = x - odometer.getX();
		double trajectoryY = y - odometer.getY();
		
		double trajectoryAngle = Math.atan2(trajectoryY, trajectoryX);
		turnTo(trajectoryAngle);
		
		double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
		
		leftMotor.rotate(convertDistanceForMotor(WHEEL_RADIUS, trajectoryLine),true);
		rightMotor.rotate(convertDistanceForMotor(WHEEL_RADIUS, trajectoryLine),false);
		
		
	}
	
	public static void turnTo(double theta) {
		
		leftMotor.rotate(convertAngleForMotor(WHEEL_RADIUS, WHEEL_BASE, theta),true);
		rightMotor.rotate(convertAngleForMotor(WHEEL_RADIUS, WHEEL_BASE, theta),false);
	}
	
	public static boolean isNavigating() {
		return navigating;
	}
	
	private static int convertDistanceForMotor(double radius, double distance){
		return (int) (360*distance/(2*PI*radius));
	}
	
	private static int convertAngleForMotor(double radius, double width, double angle){
		return convertDistanceForMotor(radius, PI*width*angle/360);
	}


}
