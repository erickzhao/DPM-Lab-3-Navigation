package ev3Navigation;

import lejos.hardware.Button;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.motor.EV3LargeRegulatedMotor;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.Port;

public class NavLab {
	
	//Initialize class variables
	private static final EV3LargeRegulatedMotor leftMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("A"));
	private static final EV3LargeRegulatedMotor rightMotor = new EV3LargeRegulatedMotor(LocalEV3.get().getPort("D"));
	

	
	//constants
	public static final double WHEEL_RADIUS = 2.13;
	public static final double WHEEL_BASE = 15.13;
	
	public static void main(String[] args) {
		
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
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}

}
