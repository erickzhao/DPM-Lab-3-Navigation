package ev3Navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Navigation implements Runnable {
	
	//constants
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	private static final double WHEEL_RADIUS = NavLab.WHEEL_RADIUS;
	private static final double WHEEL_BASE = NavLab.WHEEL_BASE;
	private static final double PI = Math.PI;
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public static void travelTo(double x, double y) {
		
	}
	
	public static void turnTo(double theta) {
		
	}
	
	public static boolean isNavigating() {
		return true;
	}
	
	private static int convertDistanceForMotor(double radius, double distance){
		return (int) (360*distance/(2*PI*radius));
	}
	
	private static int convertDegreesToRads(double radius, double width, double angle){
		return convertDistanceForMotor(radius, PI*width*angle/360);
	}


}
