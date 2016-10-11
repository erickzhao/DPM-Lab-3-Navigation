package ev3Navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.robotics.SampleProvider;
import lejos.hardware.Sound;

public class EvadeMode extends Thread{
	
	private EV3LargeRegulatedMotor sensorMotor, leftMotor, rightMotor;
	int bandCenter = 11;
	int bandWidth = 3;
	int distance;
	private SampleProvider us;
	private float[] usData;
	double wr,width;
	private Odometer odometer;
	
	
	public EvadeMode (Odometer odometer, EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor,
			EV3LargeRegulatedMotor sensorMotor, SampleProvider us, float[] usData, double wheelRadius, double width){
		this.odometer = odometer;
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.sensorMotor = sensorMotor;
		this.us = us;
		this.usData = usData;
		this.wr = wheelRadius;
		this.width = width;
		
	}
	
	Navigation nav = new Navigation(odometer, leftMotor, rightMotor);

	public void run() {
		
		leftMotor.setSpeed(150);
		rightMotor.setSpeed(150);
		leftMotor.forward();
		rightMotor.forward();
		
		int distance;
		while (true) {
			us.fetchSample(usData,0);							// acquire data
			distance=(int)(usData[0]*100.0);					// extract from buffer, cast to int
			filter(distance);
			
			if(distance <= bandCenter){
				Sound.beep();
				break;
			}
			try { Thread.sleep(50); } catch(Exception e){}		// Poor man's timed sampling
		}
		leftMotor.stop(true);
		rightMotor.stop(false);
		avoidObstacle();
		}
	
	public void avoidObstacle(){
		turnTo(odometer.getTheta()-Math.PI/2);
		sensorMotor.rotate(80);
		
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
	
	private int convertDistanceForMotor(double radius, double distance){
		return (int) (360*distance/(2*Math.PI*radius));
	}

	private int convertAngleForMotor(double radius, double width, double angle){
		return convertDistanceForMotor(radius, width*angle/2);
	}
	
public void turnTo(double theta) {
		
		double angle = theta-odometer.getTheta();
		
		leftMotor.rotate(convertAngleForMotor(wr, width, angle),true);
		rightMotor.rotate(-convertAngleForMotor(wr, width, angle),false);
	}
}



