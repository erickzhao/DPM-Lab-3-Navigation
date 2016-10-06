package ev3Navigation;

import lejos.hardware.motor.EV3LargeRegulatedMotor;

public class Odometer {
	
	//class variable declaration
	private double x, y, theta;
	private int leftMotorTachoCount, rightMotorTachoCount;
	private EV3LargeRegulatedMotor leftMotor, rightMotor;
	
	private Object lock;
	
	//constants
	private static final long ODOMETER_PERIOD_MS = 25;
	private static final double WHEEL_RADIUS = NavLab.WHEEL_RADIUS;
	private static final double WHEEL_BASE = NavLab.WHEEL_BASE;
	private static final double PI = Math.PI;
	
	//constructor
	public Odometer(EV3LargeRegulatedMotor leftMotor, EV3LargeRegulatedMotor rightMotor){
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.x = 0;
		this.y = 0;
		this.theta = 0;
		this.leftMotorTachoCount = 0;
		this.rightMotorTachoCount = 0;
		lock = new Object();
	}
	
	//run method
	public void run(){
		long updateStart, updateEnd;
		int currentTachoL, currentTachoR, lastTachoL, lastTachoR;
		double distL, distR, dDistance, dTheta, dX, dY;
		
		//set initial tachometer count to 0
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		lastTachoL = leftMotor.getTachoCount();
		lastTachoR = rightMotor.getTachoCount();
		
		while (true) {
			updateStart = System.currentTimeMillis();
			
			currentTachoL = leftMotor.getTachoCount();
			currentTachoR = rightMotor.getTachoCount();
			
			int tachoDiffL = currentTachoL - lastTachoL;
			int tachoDiffR = currentTachoR - lastTachoR;
			
			distL = 2*PI*WHEEL_RADIUS*tachoDiffL/360;
			distR = 2*PI*WHEEL_RADIUS*tachoDiffR/360;
			
			lastTachoL = currentTachoL;
			lastTachoR = currentTachoR;
			
			dDistance = (distL - distR)/2;
			dTheta = (distL - distR)/WHEEL_BASE;
			dX = dDistance*Math.sin(theta);
			dY = dDistance*Math.cos(theta);
			
			synchronized(lock){
				theta += dTheta;
				x += dX;
				y += dY;
			}
			
			updateEnd = System.currentTimeMillis();
			long updateTime = updateEnd - updateStart;
			
			if (updateTime < ODOMETER_PERIOD_MS) {
				try {
					Thread.sleep(ODOMETER_PERIOD_MS - updateTime);
				} catch (InterruptedException e) {
					//TODO: catch exception if odometer is interrupted by other thread
				}
			}
		}
	}
	//getters
	public void getPosition(double[] position, boolean[] update){
		synchronized(lock){
			if (update[0]){
				position[0] = x;
			}
			if (update[1]){
				position[1] = y;
			}
			if (update[2]){
				position[2] = theta;
			}
		}
	}
	
	public double getX(){
		double result;

		synchronized (lock){
			result = x;
		}

		return result;
	}

	public double getY(){
		double result;

		synchronized (lock){
			result = y;
		}

		return result;
	}

	public double getTheta(){
		double result;

		synchronized (lock){
			result = theta;
		}

		return result;
	}
	
//setters
	public void setPosition(double[] position, boolean[] update) {
		// ensure that the values don't change while the odometer is running
		synchronized (lock) {
			if (update[0])
				x = position[0];
			if (update[1])
				y = position[1];
			if (update[2])
				theta = position[2];
		}
	}

	public void setX(double x) {
		synchronized (lock) {
			this.x = x;
		}
	}

	public void setY(double y) {
		synchronized (lock) {
			this.y = y;
		}
	}

	public void setTheta(double theta) {
		synchronized (lock) {
			this.theta = theta;
		}
	}

	/**
	 * @return the leftMotorTachoCount
	 */
	public int getLeftMotorTachoCount() {
		return leftMotorTachoCount;
	}

	/**
	 * @param leftMotorTachoCount the leftMotorTachoCount to set
	 */
	public void setLeftMotorTachoCount(int leftMotorTachoCount) {
		synchronized (lock) {
			this.leftMotorTachoCount = leftMotorTachoCount;	
		}
	}

	/**
	 * @return the rightMotorTachoCount
	 */
	public int getRightMotorTachoCount() {
		return rightMotorTachoCount;
	}

	/**
	 * @param rightMotorTachoCount the rightMotorTachoCount to set
	 */
	public void setRightMotorTachoCount(int rightMotorTachoCount) {
		synchronized (lock) {
			this.rightMotorTachoCount = rightMotorTachoCount;	
		}
	}


}
