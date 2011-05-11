/**
 * 
 */
package org.hive13.jircbot.support;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author vincentp
 *
 */
public class jIRCToolsTest {

	public final String TEST_STRING = "REMOVETHIS This is a test\n\tOf a string   removethis   \n With multiple issues\t\t\n\t that will" +
									  " need to be cleaned up: Behold, the LOD. RemoveThis";
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
		testStr = jIRCTools.replaceAll(testStr, "(ReMoVeThIs|\\n|  )", "");
		assertTrue("Regex: Found newline in testStr at:" + testStr.indexOf("\n"), testStr.indexOf("\n") == -1);
		assertTrue("Regex: Found multiple spaces at:" + testStr.indexOf("  "), testStr.indexOf("  ") == -1);
		assertTrue("Regex: Found occurance of RemoveThis at: " + testStr.indexOf("RemoveThis"), testStr.indexOf("RemoveThis") == -1);
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#generateMD5(java.lang.String)}.
	 */
	@Test
	public void testGenerateMD5() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#generateCRC32(java.lang.String)}.
	 */
	@Test
	public void testGenerateCRC32() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#generateShortURL(java.lang.String)}.
	 */
	@Test
	public void testGenerateShortURLString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#generateShortURL(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGenerateShortURLStringStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getShortURLTitle(java.lang.String)}.
	 */
	@Test
	public void testGetShortURLTitleString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getShortURLTitle(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetShortURLTitleStringStringString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getURLTitle(org.hive13.jircbot.support.WebFile)}.
	 */
	@Test
	public void testGetURLTitle() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getStmtForConn(java.lang.String)}.
	 */
	@Test
	public void testGetStmtForConn() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#isValidColumn(java.sql.ResultSet, java.lang.String)}.
	 */
	@Test
	public void testIsValidColumn() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#insertMessage(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.hive13.jircbot.support.jIRCTools.eMsgTypes)}.
	 */
	@Test
	public void testInsertMessageStringStringStringStringEMsgTypes() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#insertMessage(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.hive13.jircbot.support.jIRCTools.eMsgTypes, java.lang.String)}.
	 */
	@Test
	public void testInsertMessageStringStringStringStringEMsgTypesString() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getChannelID(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetChannelID() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#insertChannel(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testInsertChannel() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getMessagesByUser(java.lang.String)}.
	 */
	@Test
	public void testGetMessagesByUser() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#getRandomUsernames(java.lang.String, int)}.
	 */
	@Test
	public void testGetRandomUsernames() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#updateAllTargetsUsernames(java.lang.String, java.util.ArrayList, java.util.ArrayList)}.
	 */
	@Test
	public void testUpdateAllTargetsUsernames() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#searchMessagesForKeyword(java.lang.String)}.
	 */
	@Test
	public void testSearchMessagesForKeyword() {
		fail("Not yet implemented"); // TODO
	}

	/**
	 * Test method for {@link org.hive13.jircbot.support.jIRCTools#updateAllTargetsMessages(java.lang.String, java.util.ArrayList)}.
	 */
	@Test
	public void testUpdateAllTargetsMessages() {
		fail("Not yet implemented"); // TODO
	}

}
