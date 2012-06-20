/**
 * 
 */
package org.hive13.jircbot.support;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author vincentp
 *
 */
public class jIRCToolsTest {

	public final String TEST_STRING = "REMOVETHIS This is a test\n\n\n\tOf a string   removethis   \n With multiple issues\t\t\n\t that will" +
									  " need to be cleaned up: Behold, the LOD. RemoveThis";
	public final String TEST_URL 	= "http://www.google.com/index.html";
	public final String TEST_URL2	= "http://www.theorionbelt.com/index.html";
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

   /**
    * Test method for Wunderground.
    */
   @Test
   public void testWundergroundTemperature() {
      String stationID = "KLUK";
      double result = -255;
      result = jIRCTools.getWUndergroundTemperature(stationID);
      assertTrue("Result of temperature was:" + result, result != -255);
   }

   /**
    * Test method for {@link org.hive13.jircbot.support.jIRCTools#replaceAll(java.lang.String, java.lang.String, java.lang.String)}.
    */
   @Test
   public void testReplaceAll() {
      String testStr = TEST_STRING;
      testStr = jIRCTools.replaceAll(testStr, "\\n", ""); // Remove newlines from the string
      assertTrue("Found newline in testStr at:" + testStr.indexOf("\n"), testStr.indexOf("\n") == -1);

      testStr = TEST_STRING; // Reset the test
      testStr = jIRCTools.replaceAll(testStr, "  ", ""); // Remove duplicate spaces
      assertTrue("Found multiple spaces at:" + testStr.indexOf("  "), testStr.indexOf("  ") == -1);

      testStr = TEST_STRING; // Reset the test
      testStr = jIRCTools.replaceAll(testStr, "RemOveThIs", ""); // Case insensitive text remove
      assertTrue("Found occurance of RemoveThis at: " + testStr.indexOf("RemoveThis"), testStr.indexOf("RemoveThis") == -1);
      
      testStr = TEST_STRING; // Reset the test
      testStr = jIRCTools.replaceAll(testStr, "(ReMoVeThIs|\\n)", "");
      testStr = jIRCTools.replaceAll(testStr, "  ", "");
      assertTrue("Regex: Found newline in testStr at:" + testStr.indexOf("\n"), testStr.indexOf("\n") == -1);
      assertTrue("Regex: Found occurance of RemoveThis at: " + testStr.indexOf("RemoveThis"), testStr.indexOf("RemoveThis") == -1);
      assertTrue("Regex: Found multiple spaces at:" + testStr.indexOf("  "), testStr.indexOf("  ") == -1);
   }

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#generateShortURL(java.lang.String)}.
	 */
	@Test
	public void testGenerateShortURLString() {
		String expectedShort = "http://bit.ly/"; // This value could change, but as long as this test is run regularly it should work.
		String shortURL = jIRCTools.generateShortURL(TEST_URL);
		assertEquals(expectedShort, shortURL.substring(0, expectedShort.length()));		
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#findURLTitle(java.lang.String)}.
	 */
	@Test
	public void testfindURLTitleString() {
		String urlTitle_Expected = "Google";

		String urlTitle = jIRCTools.findURLTitle(TEST_URL); // I would imagine that this would be cached by bit.ly already.
		assertEquals(urlTitle_Expected, urlTitle);
		
		urlTitle = jIRCTools.findURLTitle(TEST_URL + "#" + 
				Calendar.getInstance().getTimeInMillis()); // This however, should force a cache miss on bit.ly
		assertEquals(urlTitle_Expected, urlTitle);
		
		long startTime = Calendar.getInstance().getTimeInMillis();
		urlTitle = jIRCTools.findURLTitle(TEST_URL2 + "#" +
				Calendar.getInstance().getTimeInMillis()); // Page w/ no title and a forced bit.ly cache miss.
		long stopTime = Calendar.getInstance().getTimeInMillis();
		assertEquals("www.theorionbelt.com", urlTitle);
		assertTrue("findURLTitle returned in less than 5s.  It should have timed out.", stopTime-startTime > 5000);
		
		// TODO: Need to test URL's with spaces, unicode, UTF, new lines, etc..
	}
	
	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#findURLTitle(java.lang.String)}.
	 * 
	 * This test case is specially related to fixing GitHub Issue #1
	 */
	@Test
	public void testfindURLTitle_Issue1() {
		String cacheMiss = "#" + Calendar.getInstance().getTimeInMillis();
		ArrayList<String> testUrls = new ArrayList<String>();
		testUrls.add("http://www.fullspectrumengineering.com/forums/viewtopic.php?t=259"); // Had newline issues
		testUrls.add("http://www.reddit.com/r/Minecraft/comments/gwzgh/im_so_obsessed_with_minecraft_so_i_got_some_wood/"); // Had issues?
		testUrls.add("https://chrome.google.com/webstore/detail/giibodhbpkhjnbnfmbpmnkmgjflabkop"); // Returned "bit.ly"
		
		Iterator<String> it = testUrls.iterator();
		while(it.hasNext()) {
			String testURL = it.next();
			for(int i = 1; i <= 2; i++) { // Each URL twice, once with a cache miss on bit.ly
				if(i % 2 == 0)
					testURL += cacheMiss;
				
				String urlTitle = jIRCTools.findURLTitle(testURL);
				// Make sure there are no duplicate spaces
				//fail(urlTitle);
				assertTrue("Found duplicate spaces at (" + urlTitle.indexOf("  ") + ") in: " + urlTitle + " for URL: " + testURL, urlTitle.indexOf("  ") == -1);
				// Make sure there are no new lines
				assertTrue("Found multiple lines in: " + urlTitle + " for URL: " + testURL, urlTitle.indexOf("\n") == -1);
				// Make sure there are no non-ASCII characters
				assertTrue("Found non-ASCII in: " + urlTitle + " for URL: " + testURL, urlTitle.matches("\\A\\p{ASCII}*\\z"));	
				// Make sure the title is not bit.ly
				assertTrue("Found title to be bit.ly for URL: " + testURL, !urlTitle.equals("bit.ly"));
				// Make sure we actually found a title...
				assertTrue("Failed to find title for URL: " + testURL, !urlTitle.isEmpty());
			}
		}
	}
}
