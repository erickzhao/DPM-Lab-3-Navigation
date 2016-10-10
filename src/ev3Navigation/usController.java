package ev3Navigation;
import lejos.hardware.motor.*;

public class usController implements UltrasonicController{
	private final int bandCenter, bandwidth;
	private final int motorStraight = 100, FILTER_OUT = 25;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	private int distance;
	private int filterControl;
	private int angle;
	private int correctionCompensation = 65;
	private int differentialCompensation = 150;
	
	
	public usController(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
					   int bandCenter, int bandwidth, int angle){
		this.bandCenter = bandCenter;
		this.bandwidth = bandwidth;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.angle = angle;
		leftMotor.setSpeed(motorStraight);					// Initalize motor rolling forward
		rightMotor.setSpeed(motorStraight);
		leftMotor.forward();
		rightMotor.forward();
		filterControl = 0;
		
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
	
		
		
		int errorDistance = bandCenter - this.distance;
		
		if(angle <=0 ){
			
			
			if (Math.abs(errorDistance) <= bandwidth){
				leftMotor.setSpeed(motorStraight);
				rightMotor.setSpeed(motorStraight);
				leftMotor.forward();
				rightMotor.forward();
			}
			
			else if (errorDistance < 0 ){
				int correction = calculateCorrection(errorDistance);
				leftMotor.setSpeed(motorStraight);
				rightMotor.setSpeed(motorStraight+correction);
				leftMotor.forward();
				rightMotor.forward();
			}
			
			else if (errorDistance > 0 ){
				int correction = calculateCorrection(errorDistance);
				leftMotor.setSpeed(motorStraight);
				rightMotor.setSpeed(Math.abs(motorStraight-correction-150));
				
				if (motorStraight-correction-150<0){
					rightMotor.backward();
				} else{
					rightMotor.forward();
				}
				leftMotor.forward();
			}
	
		}
		else if (angle > 0){
			if (Math.abs(errorDistance) <= bandwidth){
				rightMotor.setSpeed(motorStraight);
				leftMotor.setSpeed(motorStraight);
				leftMotor.forward();
				rightMotor.forward();
			}
			
			else if (errorDistance < 0 ){
				int correction = calculateCorrection(errorDistance);
				rightMotor.setSpeed(motorStraight);
				leftMotor.setSpeed(motorStraight+correction);
				leftMotor.forward();
				rightMotor.forward();
			}
			
			else if (errorDistance > 0 ){
				int correction = calculateCorrection(errorDistance);
				rightMotor.setSpeed(motorStraight);
				leftMotor.setSpeed(Math.abs(motorStraight-correction-differentialCompensation));
				
				if (motorStraight-correction-differentialCompensation<0){
					leftMotor.backward();
				} else{
					rightMotor.forward();
				}
				rightMotor.forward();
			}
		}

			
	
		
	}
	
	
	
	
	
	// Reposition the robot and the sensor until the robot is parallel to 
	// the obstacle while the sensor is facing directly the obstacle
	// We are assuming that the obstacle have a non-concave form	
	
	
		
	
	
	
	
	@Override
	public int readUSDistance() {
		return this.distance;
	}
	/* This method takes in an int diff that represents the distance error of the robot.
	 * It outputs a correction speed (in deg/s) that is directly proportional to the input variable.
	 * There is a max cap to how much correction there can be.
	 */
	
	private int calculateCorrection(int diff){
		
		int correction; 
		diff = Math.abs(diff);
		double proportionConstant = 3.4; // The proportion between distance and corrected speed
		int maxCorrection = 180;
		
		correction = (int)(proportionConstant * (double)diff);
		
		if (correction >= motorStraight-correctionCompensation){
			correction = maxCorrection;
		}
		
		return correction;
		
	}
	
	
}


