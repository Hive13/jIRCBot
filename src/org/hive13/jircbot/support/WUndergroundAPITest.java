package org.hive13.jircbot.support;

import static org.junit.Assert.*;

import org.junit.Test;

public class WUndergroundAPITest {

	private void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000); 
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testDummyFloodTest() {
		int expectedFloodStop = WUndergroundAPI.MINUTE_FLOOD_LIMIT;
		int resultFloodStop = 0;
		while(WUndergroundAPI.dummyFloodTest()) resultFloodStop++;
		assertEquals("Expected " + expectedFloodStop + " but actually was " + resultFloodStop, expectedFloodStop, resultFloodStop);
		
		// sleep for 58 seconds, then try again, it should still fail.
		sleep(58);
		
		expectedFloodStop = 0;
		resultFloodStop = 0;
		while(WUndergroundAPI.dummyFloodTest()) resultFloodStop++;
		assertEquals("Expected " + expectedFloodStop + " but actually was " + resultFloodStop, expectedFloodStop, resultFloodStop);
		
		// sleep for 3 seconds, then try again, it should let you add all 8 events again.
		sleep(3);
		
		expectedFloodStop = WUndergroundAPI.MINUTE_FLOOD_LIMIT;
		resultFloodStop = 0;
		while(WUndergroundAPI.dummyFloodTest()) {
			resultFloodStop++;
			sleep(2); // This time sleep for 2 seconds between actions.
		}
		assertEquals("Expected " + expectedFloodStop + " but actually was " + resultFloodStop, expectedFloodStop, resultFloodStop);	
		
		// sleep for 50 seconds, then try again, it should let you add only 3 events.
		sleep(50);
		
		expectedFloodStop = 3;
		resultFloodStop = 0;
		while(WUndergroundAPI.dummyFloodTest()) {
			resultFloodStop++;
		}
		assertEquals("Expected " + expectedFloodStop + " but actually was " + resultFloodStop, expectedFloodStop, resultFloodStop);	
	}

	@Test
	public void testGetTemperature() {
		fail("Not yet implemented");
	}

}
