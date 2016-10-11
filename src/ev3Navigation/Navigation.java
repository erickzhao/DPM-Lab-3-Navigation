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
		travelTo(60,30);
		travelTo(30,30);
		travelTo(30,60);
		travelTo(60,0);
	}
	
	/* parameters: double x and y that represent the waypoint coordinates
	 * action: travels to waypoint
	 */
	public void travelTo(double x, double y) {
		
		//reset motors
		for (EV3LargeRegulatedMotor motor : new EV3LargeRegulatedMotor[] {leftMotor, rightMotor}) {
			motor.stop();
			motor.setAcceleration(3000);
		}
		
		navigating = true;
		
		//calculate trajectory path and angle
		double trajectoryX = x - odometer.getX();
		double trajectoryY = y - odometer.getY();
		double trajectoryAngle = Math.atan2(trajectoryX, trajectoryY);
		
		//rotate to correct angle
		Sound.beepSequenceUp();
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		turnTo(trajectoryAngle);
		
		double trajectoryLine = Math.hypot(trajectoryX, trajectoryY);
		
		//move forward correct distance
		Sound.beepSequence();
		leftMotor.setSpeed(FORWARD_SPEED);
		rightMotor.setSpeed(FORWARD_SPEED);
		leftMotor.rotate(convertDistanceForMotor(trajectoryLine),true);
		rightMotor.rotate(convertDistanceForMotor(trajectoryLine),false);
	}
	
	/* parameters: double theta that represents an angle in radians
	 * action: changes heading from current angle to theta
	 */
	public void turnTo(double theta) {
		
		double angle = theta-odometer.getTheta();
		
		leftMotor.rotate(convertAngleForMotor(angle),true);
		rightMotor.rotate(-convertAngleForMotor(angle),false);
	}
	
	/* returns: whether or not the vehicle is currently navigating
	 */
	public boolean isNavigating() {
		return navigating;
	}
	/* parameter: double distance representing the length of the line the vehicle has to run
	 * returns: amount of degrees the motors have to turn to traverse this distance
	 */
	private int convertDistanceForMotor(double distance){
		return (int) (360*distance/(2*PI*WHEEL_RADIUS));
	}
	/* parameter: double angle representing the angle heading change in radians
	 * returns: amount of degrees the motors have to turn to change this heading
	 */
	private int convertAngleForMotor(double angle){
		return convertDistanceForMotor(WHEEL_BASE*angle/2);
	}


}
