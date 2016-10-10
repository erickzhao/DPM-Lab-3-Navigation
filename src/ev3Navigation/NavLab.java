package ev3Navigation;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;
import lejos.hardware.sensor.EV3UltrasonicSensor;
import lejos.hardware.sensor.SensorModes;
import lejos.robotics.SampleProvider;

public class NavLab {
	
	//Initialize class variables
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	private static final EV3LargeRegulatedMotor sensorMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("C"));
	private static final Port usPort = LocalEV3.get().getPort("S4");
	
	//constants
	public static final double WHEEL_RADIUS = 2.13;
	public static final double WHEEL_BASE = 15.13;
	
	public static void main(String[] args) {
		
		// Setting up the sensor
		@SuppressWarnings("resource")							    // Because we don't bother to close this resource
		SensorModes usSensor = new EV3UltrasonicSensor(usPort);		// usSensor is the instance
		SampleProvider usDistance = usSensor.getMode("Distance");	// usDistance provides samples from this instance
		float[] usData = new float[usDistance.sampleSize()];		// usData is the buffer in which data are returned
		EvadeMode em = new EvadeMode(leftMotor, rightMotor, sensorMotor, usDistance, usData, WHEEL_RADIUS, WHEEL_BASE);
		
		
		//display
		int buttonChoice;
		
		final TextLCD t = LocalEV3.get().getTextLCD();
		Odometer odometer = new Odometer(leftMotor, rightMotor);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer, t);
		Navigation nav = new Navigation(odometer, leftMotor, rightMotor);
		
		do {
			t.clear();
			
			t.drawString("< Left | Right >", 0, 0);
			t.drawString("       |        ", 0, 1);
			t.drawString(" do    | nav    ", 0, 2);
			t.drawString(" simple| around ", 0, 3);
			t.drawString(" nav   | object ", 0, 4);
			
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT && buttonChoice != Button.ID_RIGHT);
		
		if (buttonChoice == Button.ID_LEFT){
			odometer.start();
			odometryDisplay.start();
			nav.start();
		} else {
			
			//TODO: ADD CODE TO NAV AROUND OBJECTS
			em.start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}

