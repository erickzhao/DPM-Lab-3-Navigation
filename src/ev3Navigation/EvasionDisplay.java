package ev3Navigation;

import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.TextLCD;
import ev3Navigation.UltrasonicController;

public class EvasionDisplay extends Thread {
//  In addition to the UltrasonicPoller, the printer thread also operates
//  in the background.  Since the thread sleeps for 200 mS each time through
//  the loop, screen updating is limited to 5 Hz.
//
	
	private UltrasonicController cont;
	
	public EvasionDisplay(UltrasonicController cont) {
		this.cont = cont;
	}
	
	public static TextLCD t = LocalEV3.get().getTextLCD();					// n.b. how the screen is accessed
	
	public void run() {
		while (true) {														// operates continuously
			t.clear();
			
			t.drawString("US Distance: " + cont.readUSDistance(), 0, 2 );	// print last US reading
						
			try {
				Thread.sleep(200);											// sleep for 200 mS
			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}
		}
	}
	
}
