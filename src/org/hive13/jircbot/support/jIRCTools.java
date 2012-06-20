package org.hive13.jircbot.support;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.info;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.awt.Image;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.directory.InvalidAttributesException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.mbenning.weather.wunderground.api.domain.DataSet;
import de.mbenning.weather.wunderground.api.domain.WeatherStation;
import de.mbenning.weather.wunderground.api.domain.WeatherStations;
import de.mbenning.weather.wunderground.impl.services.HttpDataReaderService;

public class jIRCTools {
    
    /**
     * This must be checked by each JDBC method to ensure that the JDBC
     * integration is configured.
     */
    public static boolean jdbcEnabled = false;

    /**
     * The different types of messages saved in the database.
     */
    public enum eMsgTypes {
        // NOTE: If adding any additional message types, append them to the end
        // of the list.
        /** User messages in a chat channel. */
        publicMsg,
        /** User message directly to the bot. */
        privateMsg,
        /** Ex. /me */
        actionMsg,
        /** User joins a channel the bot is in. */
        joinMsg,
        /** User leaves a channel the bot is in. */
        partMsg,
        /** Users changes their nickname. */
        nickChange,
        /** User quits the server the bot is on. */
        quitMsg,
        /** Bot messages w/ HTML formatting. */
        htmlMsg,
        /** Do Not Log Message. */
        LogFreeMsg
    }

   // TODO: Implement getWUndergroundTemperature
   public static double getWUndergroundTemperature(String StationID) {
      double dResult = -1;
      // http://www.roseindia.net/tutorials/xPath/java-xpath.shtml
      /*// The following method did not work at all.
      // create a instance of a wunderground data reader
      HttpDataReaderService dataReader = new HttpDataReaderService();
      
      // select a wunderground weather station (ID "INORDRHE72" = Dortmund-Mengede)
      WeatherStation weatherStation = WeatherStations.ALL.get("INORDRHE72");
      
      // set selected weather station to data reader
      dataReader.setWeatherStation(weatherStation);
      
      // get current (last) weather data set from selected station
      DataSet current = dataReader.getCurrentData();
      
      // print selected weather station ID
      // System.out.println(weatherStation.getStationId());
      
      // print city, state and country of weather station
      //System.out.println(weatherStation.getCity() + " " + weatherStation.getState() + " " + weatherStation.getCountry());
      
      // print datetime of measure and temperature ...
      //System.out.println(current.getDateTime() + " " + current.getTemperature());
      result = current.getTemperature();
      
      return result;//*/
      WebFile website;
      try {
         website = new WebFile("http://api.wunderground.com/api/ad6ac91937e9a86c/conditions/q/45225.xml");
         String urlTitle = jIRCTools.getURLTitle(website);
         Object content = website.getContent();
         
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
         domFactory.setNamespaceAware(true);
         DocumentBuilder builder = domFactory.newDocumentBuilder();
         Document doc = builder.parse("persons.xml");
         
         XPath xpath = XPathFactory.newInstance().newXPath();
         
         XPathExpression expr = xpath.compile("/response/current_observation/temp_f");
         
         Object result = expr.evaluate(doc, XPathConstants.NUMBER);
         
         NodeList nodes = (NodeList) result;
         for(int i = 0; i < nodes.getLength(); i++) {
            dResult = Double.parseDouble(nodes.item(i).getNodeValue());
         }
      } catch (MalformedURLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (ParserConfigurationException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (SAXException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (XPathExpressionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return dResult;
      
   }
    
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@   ---- Generic Utility Functions ---- @@@@@@@@@@@@@@@@@@@@@@
    
    /**
     * Log message to logFilename.  This creates a new log file
     * and overwrites any previous log file with the same name.
     * 
     * @param message       What to put in logFilename.
     * @param logFilename   The path (including filename) to log to.  If
     *                      the file does not exist, it will be created.
     */
    public static void logToFile(String message, String logFilename) throws IOException {
        logToFile(message, logFilename, false);
        
    }
    
    /**
     * Log message to logFilename.
     * 
     * @param message       What to put in logFilename.
     * @param logFilename   The path (including filename) to log to.  If
     *                      the file does not exist, it will be created.
     * @param append        True to append message to the file,
     *                      False to overwrite the file.
     * @throws IOException
     */
    public static void logToFile(String message, String logFilename, boolean append) throws IOException {
        File logFile = new File(jIRCProperties.getInstance().getCacheDirectory().getPath()
                + "/" + logFilename);
        if(!logFile.exists()) {
            logFile.createNewFile();
        }
        if(logFile.exists()) {
            Writer writer = new FileWriter(logFile, append);
            writer.write(message);
            writer.close();
        }
        
    }

    /**
     * Implementation of a case insensitive string replace all function.
     * 
     * @param string        Source string.
     * @param regex         What to search the string for.
     * @param replaceWith   What to replace items found with the regex with.
     * @return              The source strings with all instances found
     *                      by the regex replaced with replaceWith.
     */
    public static String replaceAll(String string, String regex, String replaceWith){
        Pattern myPattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        /*for space,new line, tab checks*/
        //Pattern myPattern = Pattern.compile(regex+"[ /n/t/r]", Pattern.CASE_INSENSITIVE);
        string = myPattern.matcher(string).replaceAll(replaceWith);
        return string;
    }
    
    public static String generateMD5(String input) {
        String hashword = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD2");
            md5.update(input.getBytes());
            BigInteger hash = new BigInteger(1, md5.digest());
            hashword = hash.toString(Character.MAX_RADIX);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return hashword;
    }
    
    public static String generateCRC32(String input) {
        java.util.zip.CRC32 x = new java.util.zip.CRC32();
        x.update(input.getBytes());
        return Long.toHexString(x.getValue());
        
    }
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@   ---- URL Related Utility Functions ---- @@@@@@@@@@@@@@@@@@
    /**
     * Bit.ly API integration to generate a shortened URL. Make sure that the
     * bitlyName and bitlyAPIKey are specified before calling this method.
     * 
     * @param longURL
     *            Long URL to shorten.
     * @return Returns a bit.ly shortened URL
     */
    public static String generateShortURL(String longURL) {
        return generateShortURL(longURL, jIRCProperties.getInstance()
                .getBitlyName(), jIRCProperties.getInstance().getBitlyAPIKey());
    }

    /**
     * Uses the Bit.ly API to generate a short URL.
     * 
     * @param longURL
     *            The log URL to shorten.
     * @param username
     *            The Bit.ly username to use.
     * @param apikey
     *            Bit.ly APIKey associated w/ the username.
     * @return Returns a shortened bit.ly URL.
     */
    public static String generateShortURL(String longURL, String username,
            String apikey) {
        if(longURL.isEmpty())
            return "";
        
        String result = "Username or API key are not initialized";
        if (jIRCProperties.getInstance().getBitlyName().length() > 0
                && jIRCProperties.getInstance().getBitlyAPIKey().length() > 0)
            result = as(username, apikey).call(shorten(longURL)).getShortUrl();
        return result;
    }

    /**
     * This method aggressively attempts to find some string description of
     * the passed in URL. This function has multiple stages as it tries 
     * various methods for finding the passed in URL's title.
     * 1. See if bit.ly has cached a title for the page.
     * 2. Determine if the page is text or binary.
     * 2.1. If it is binary, determine the MIME type and return that.
     * 2.2. If it is text, download a copy of the page's text and parse it for <title> tags.
     * 3. Did we fail to find the title via #2? Try to wait for bit.ly to cache the page title. (5 seconds, then time out)
     *
     * @param url	The http URL address to find a string description for.
     * @return		A string description of the passed in URL.
     */
    public static String findURLTitle(String url) {
    	return findURLTitle(url, generateShortURL(url),  null);
    }
    
    /**
     * This method aggressively attempts to find some string description of
     * the passed in URL. This function has multiple stages as it tries 
     * various methods for finding the passed in URL's title.
     * 1. See if bit.ly has cached a title for the page.
     * 2. Determine if the page is text or binary.
     * 2.1. If it is binary, determine the MIME type and return that.
     * 2.2. If it is text, download a copy of the page's text and parse it for <title> tags.
     * 3. Did we fail to find the title via #2? Try to wait for bit.ly to cache the page title. (5 seconds, then time out)
     *
     * @param url		The http URL address to find a string description for.
     * @param shortURL	The bit.ly short version of the URL passed in.
     * @param bot	An optional parameter for status updates for the log.
     * @return		A string description of the passed in URL.
     */
    public static String findURLTitle(String url, String shortURL, jIRCBot bot) {
    	final boolean USE_BITLY_TITLE = true;		// Should we give Bit.ly a 2nd chance?
    	final boolean WAIT_FOR_TITLE = true;		// Given a 2nd chance, should we keep giving it chances until Timeout?
    	final int WAIT_FOR_TITLE_TIMEOUT = 5000;	// Given multiple chances, how long before we give up (in ms)
    	
    	String urlTitle = "";
    	boolean withBot = (bot != null);
    	
    	urlTitle = jIRCTools.getShortURLTitle(shortURL);
    	if(urlTitle.isEmpty()) {
            if(withBot) bot.log("findURLTitle - initial bit.ly failed, trying jIRCTools.getURLTitle", eLogLevel.info);
            try {
                WebFile website = new WebFile(url);
                urlTitle = jIRCTools.getURLTitle(website);
                Object content = website.getContent();
                if(content instanceof Image) {
                    // We want to resize this image.
                    // Image img = (Image)content;
                    
                    // Then write this image to a directory.
                    // Then write a 'formatedMsg' to the log.
                }
            } catch (MalformedURLException e) {
            	if(withBot) bot.log("findURLTitle - WTF! Bit.ly gave us an invalid URL...", eLogLevel.error);
                e.printStackTrace();
            } catch (IOException e) {
            	if(withBot) bot.log("findURLTitle - failed (badly) to getURLTitle()", eLogLevel.error);
                e.printStackTrace();
            }
        }
        
        if(urlTitle.isEmpty() && USE_BITLY_TITLE) { // Are we allowing ourselves to fall back to Bit.ly?
        	if(withBot) bot.log("findURLTitle - jIRCTools.getURLTitle failed, waiting for bit.ly to cache title.", eLogLevel.info);
            // The title is not retrieved by bit.ly immediately, we can optionally
            // move on, likely without the title, or we can repeatedly try
            // until we get a response or timeout.
            Date start = new Date();
            long duration = 0;
            while((urlTitle = jIRCTools.getShortURLTitle(shortURL)).isEmpty()
                    && WAIT_FOR_TITLE && duration < WAIT_FOR_TITLE_TIMEOUT) {
                try {
                    Thread.sleep(200);
                    duration = (new Date()).getTime() - start.getTime();
                    if(withBot) bot.log("Waiting for URL title [ " + duration + " ms ]");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        if(urlTitle.isEmpty()) { // Is the title STILL empty?  Lets fall back to the TLD
        	if(withBot) bot.log("findURLTitle - Second bit.ly failed, let's just get the Domain...");
        	urlTitle = getURLDomain(url);
        	if(urlTitle.isEmpty() && withBot) {
        		bot.log("findURLTitle - getURLDomain returned a blank result.");
        	}
        }
        // Remove ASCII characters, new lines, and excessive spaces.
        urlTitle = urlTitle.replaceAll("[^\\p{ASCII}]", "");
        urlTitle = urlTitle.replaceAll("\\n", "");
        urlTitle = urlTitle.replaceAll("  ", "");
    	return urlTitle;
    }
    
    
    /**
     * Calls on the Bit.ly API to find the title of an already shortened URL.
     * Make sure that the bitlyName and bitlyAPIKey are specified before calling
     * this method.
     * 
     * @param shortURL
     *            An already shortened Bit.ly URL.
     * @return Returns the title of the page the Bit.ly URL links too.
     */
    public static String getShortURLTitle(String shortURL) {
        return getShortURLTitle(shortURL, jIRCProperties.getInstance()
                .getBitlyName(), jIRCProperties.getInstance().getBitlyAPIKey());
    }

    /**
     * Calls on the Bit.ly API to find the title of an already shortened URL.
     * 
     * @param shortURL
     *            An already shortened Bit.ly URL.
     * @param username
     *            The Bit.ly username to use.
     * @param apikey
     *            Bit.ly APIKey associated w/ the username.
     * @return Returns the title of the page the Bit.ly URL links too.
     */
    public static String getShortURLTitle(String shortURL, String username,
            String apikey) {
        String result = "Username or API key are not initialized";
        if (jIRCProperties.getInstance().getBitlyName().length() > 0
                && jIRCProperties.getInstance().getBitlyAPIKey().length() > 0)
            result = as(username, apikey).call(info(shortURL)).getTitle();
        return result;
    }

    /**
     * When passed the URL for a webpage this function attempts to determine the
     * title of the webpage if it is an HTML page, if it is not an HTML page it
     * determines the MIME datatype and returns that.
     * 
     * @return Returns the title of the webpage.
     */
    public static String getURLTitle(WebFile website) {
        String result = "";
        String type = website.getMIMEType();

        String rgxIsHTML = "(text/x?html|application/xhtml+xml)";
        Pattern p = Pattern.compile(rgxIsHTML);
        Matcher m = p.matcher(type);
        if (!m.find()) {
            result = type; // It is not a webpage.
        } else {
            Object content = website.getContent();
            if (content instanceof String) {
                String sContent = ((String) content).replaceAll("[\\n\\r]", "");
                // Case insensitive search for the <title> tags.
                String rgxFindTitle = "<[tT][iI][tT][lL][eE][^>]*>(.*?)</[tT][iI][tT][lL][eE]>";
                p = Pattern.compile(rgxFindTitle);
                m = p.matcher(sContent);
                if (m.find()) {
                    result = m.group();
                    result = result.substring(result.indexOf('>') + 1,
                            result.lastIndexOf('<')).trim();
                }
            }

        }
        return result;
    }
    
    public static Object getUrlContent(String url) throws MalformedURLException, IOException {
    	WebFile website = new WebFile(url);
		return website.getContent();
    }
    
    // TODO: Implement the code to get text.
    public static String getURLText(WebFile website) {
       String result = "";
       String type = website.getMIMEType();

       String rgxIsHTML = "(text/x?html|application/xhtml+xml)";
       Pattern p = Pattern.compile(rgxIsHTML);
       Matcher m = p.matcher(type);
       if (!m.find()) {
           result = type; // It is not a webpage.
       } else {
           Object content = website.getContent();
           if (content instanceof String) {
               String sContent = ((String) content).replaceAll("[\\n\\r]", "");
               // Case insensitive search for the <title> tags.
               String rgxFindTitle = "<[tT][iI][tT][lL][eE][^>]*>(.*?)</[tT][iI][tT][lL][eE]>";
               p = Pattern.compile(rgxFindTitle);
               m = p.matcher(sContent);
               if (m.find()) {
                   result = m.group();
                   result = result.substring(result.indexOf('>') + 1,
                           result.lastIndexOf('<')).trim();
               }
           }

       }
       return result;
    }

    public static String getURLDomain(String sURL) {
    	String result = "";
    	try {
			URL url = new URL(sURL);
			result = url.getHost();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return result;
    }
    
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@   ---- MySQL Utility Functions ---- @@@@@@@@@@@@@@@@@@@@@@@@
    
    /**
     * This function creates a connection to a MySQL database using the settings
     * defined in the *.properties file.  If these settings are not defined it
     * throws an "InvalidAttributesException".
     * 
     * @param statement SQL Query to use to initialize the prepared statement.
     * @return          A prepared statement that is ready to be initialized
     *                  with the proper parameters and then executed.
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws InvalidAttributesException
     */
    public static PreparedStatement getStmtForConn(String statement) throws SQLException, ClassNotFoundException, InvalidAttributesException {
    	if (!jIRCTools.jdbcEnabled)
            throw new InvalidAttributesException("jdbcEnabled is false, refusing to create connection that can not exist.");

    	Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(jIRCProperties
                .getInstance().getJDBCUrl(), jIRCProperties.getInstance()
                .getJDBCUser(), jIRCProperties.getInstance().getJDBCPass());
        return conn.prepareStatement(statement);
    }
 
    /**
     * Simple function to determine if a ResultSet has
     * the specified column.
     * 	
     * @param rs            The ResultSet that we are checking.
     * @param columnName    The name of the column to check for.
     * @return              True if the ResultSet contains the column.
     */
    public static boolean isValidColumn(ResultSet rs, String columnName) {
    	boolean result = false;
    	try {
			rs.findColumn(columnName);
			result = true;
		} catch (SQLException e) {
			result = false;
		}
    	return result;
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    // @@@@@@@@@@   ---- MySQL Query Functions ---- @@@@@@@@@@@@@@@@@@@@@@@@@@
    /**
     * Inserts a new message into the "messages" table of the attached MySQL
     * database.
     * 
     * @param channel
     *            Name of the channel the message relates to.
     * @param server
     *            The server the bot is connected too.
     * @param username
     *            The username of related to this message.
     * @param msg
     *            The actual contents of the message.
     * @param msgType
     *            The type of message (Public, private, join // part, etc..)
     */
    public static void insertMessage(String channel, String server,
            String username, String msg, eMsgTypes msgType) {
        if (!jIRCTools.jdbcEnabled || eMsgTypes.LogFreeMsg == msgType)
            return;

        int chanID = getChannelID(channel, server);
        if (chanID == -1) { // Channel does not exist... try to create it.
            if ((chanID = insertChannel(channel, server)) == -1) { // Did the
                                                                   // create
                                                                   // work?
                Logger.getLogger(jIRCBot.class.getName()).log(
                        Level.SEVERE,
                        "Failed to insert new channel: " + channel + "@"
                                + server + "\n");
            }
        }

        String insertStatement = "INSERT INTO messages "
                + "( fk_ChannelID, vcMsgType, vcUsername, vcMessage )"
                + "VALUES" + "( ?, ?, ?, ? )";
        try {
            PreparedStatement stmt = getStmtForConn(insertStatement);
            stmt.setInt(1, chanID);
            stmt.setString(2, msgType.toString());
            stmt.setString(3, username);
            stmt.setString(4, msg);
            stmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidAttributesException e) {
			// Do Nothing, the mysql conn is just not set up.
		}
    }

    /**
     * Inserts a new message into the "messages" table of the attached MySQL
     * database.
     * 
     * @param channel
     *            Name of the channel the message relates to.
     * @param server
     *            The server the bot is connected too.
     * @param username
     *            The username of related to this message.
     * @param msg
     *            The actual contents of the message.
     * @param msgType
     *            The type of message (Public, private, join // part, etc..)
     */
    public static void insertMessage(String channel, String server,
            String username, String msg, eMsgTypes msgType, String tsMsgDate) {
        if (!jIRCTools.jdbcEnabled || eMsgTypes.LogFreeMsg == msgType)
            return;

        int chanID = getChannelID(channel, server);
        if (chanID == -1) { // Channel does not exist... try to create it.
            if ((chanID = insertChannel(channel, server)) == -1) { // Did the
                                                                   // create
                                                                   // work?
                Logger.getLogger(jIRCBot.class.getName()).log(
                        Level.SEVERE,
                        "Failed to insert new channel: " + channel + "@"
                                + server + "\n");
            }
        }

        String insertStatement = "INSERT INTO messages "
                + "( fk_ChannelID, vcMsgType, vcUsername, vcMessage, tsMsgTime )"
                + "VALUES" + "( ?, ?, ?, ?, ? )";
        try {
            PreparedStatement stmt = getStmtForConn(insertStatement);
            stmt.setInt(1, chanID);
            stmt.setString(2, msgType.toString());
            stmt.setString(3, username);
            stmt.setString(4, msg);
            stmt.setString(5, tsMsgDate);
            stmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidAttributesException e) {
			// Do Nothing, the mysql conn is just not set up.
		}
    }

    /**
     * Finds the database ID for a channel && server
     * 
     * @param channel
     *            The name of the channel.
     * @param server
     *            The server the bot is connected too.
     * @return The ID # for the channel or -1 if it is unable to find the
     *         channel.
     */
    public static int getChannelID(String channel, String server) {
        if (!jIRCTools.jdbcEnabled)
            return -1;

        String stmtGetChannelID = "SELECT pk_ChannelID FROM channel WHERE vcServer=? AND vcChannel=?";

        try {
            PreparedStatement stmt = getStmtForConn(stmtGetChannelID);
            stmt.setString(1, server);
            stmt.setString(2, channel);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                return rs.getInt("pk_ChannelID");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidAttributesException e) {
			// Do Nothing, the mysql conn is just not set up.
		}
        return -1; // Failed to find a channelID
    }

    /**
     * Inserts a new channel, it does not check to make sure the channel //
     * server pair does not already exist.
     * 
     * @param channel
     *            Name of the channel
     * @param server
     *            Name of the server
     * @return The ID # for the new channel or -1 if it fails to add the
     *         channel.
     */
    public static int insertChannel(String channel, String server) {
        String stmtInsertChannel = "INSERT INTO channel "
                + "( vcChannel, vcServer )" + "VALUES" + "( ?, ?)";

        try {
            PreparedStatement stmt = getStmtForConn(stmtInsertChannel);
            stmt.setString(1, channel);
            stmt.setString(2, server);
            stmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (InvalidAttributesException e) {
			// Do Nothing, the mysql conn is just not set up.
		}

        return getChannelID(channel, server);
    }

    /**
     * Returns an ArrayList of MessageRows that contains all the logged
     * message information for the passed in username.
     * 
     * @param username  Username to search for messages for.
     * @return          Any arraylist of MessageRows populated with
     *                  data from the database.
     */
    public static ArrayList<MessageRow> getMessagesByUser(String username) {
    	ArrayList<MessageRow> result = new ArrayList<MessageRow>();
    	
    	String stmtGetMessages = 
    		"SELECT pk_MessageID, fk_ChannelID, vcUsername, vcMessage, vcMsgType, tsMsgTime " +
			"FROM messages " +
			"WHERE vcUsername = ?";
    	
    	try {
			PreparedStatement stmt = getStmtForConn(stmtGetMessages);
			stmt.setString(1, username);
			
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
            	result.add(new MessageRow(rs));
            }
			
		} catch (InvalidAttributesException e) {
            // Do Nothing, the mysql conn is just not set up.
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
            e.printStackTrace();
		}
		
    	return result;
    }
    
    /**
     * Polls the database for 'n' number of random usernames
     * 
     * @param omitName  A username to ommit from the list of random usernames.
     * @param number    The number of random usernames to return.
     * @return          Returns an ArrayList of MessageRow's, however only the
     *                  vcUsername is set in each row.
     */
    public static ArrayList<MessageRow> getRandomUsernames(String omitName, int number) {
        ArrayList<MessageRow> result = new ArrayList<MessageRow>();
        
        String stmtNameQuery;
        
        //TODO: Make the 'excludes' more generic.
        if(number > 1) {
            stmtNameQuery = "SELECT vcUsername " +
                            "  FROM messages " +
                            " WHERE vcUsername != ? " +
                            "    AND vcUsername != 'Hive13Bot' " +  // No bot names
                            "    AND vcUsername != 'Phergie' " +    // No bot names
                            "    AND (vcMsgType='publicMsg' OR vcMsgType='actionMsg') " +
                            "ORDER BY RAND() LIMIT ?";
        } else {
            stmtNameQuery = "SELECT vcUsername " +
                            "  FROM messages M  " +
                            "    JOIN ( " +
                            "      SELECT FLOOR(MAX((pk_messageid)-1)*RAND()) AS ID " +
                            "      FROM messages ) AS X " +
                            "    ON M.pk_messageID >= X.ID " +
                            " WHERE (vcMsgType='publicMsg' OR vcMsgType='actionMsg') " +
                            "      AND NOT vcUsername=? " +
                            " LIMIT 1";
        }
        try {
            PreparedStatement stmt = getStmtForConn(stmtNameQuery);
            stmt.setString(1, omitName);
            if(number > 1)
                stmt.setInt(2, number);
            
            ResultSet rs = stmt.executeQuery();
            
            while(rs.next()) {
                result.add(new MessageRow(rs, false));
            }
        } catch (InvalidAttributesException e) {
            // We do not have a MySQL connection set up.
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        
        return result;
    }

    /**
     * This database query is fairly specific to jIBCObfuscate.  It might make
     * more sense if you look at the code in there.
     */
    public static void updateAllTargetsUsernames(String vcUsername, 
            ArrayList<MessageRow> msgIds, ArrayList<MessageRow> replacementNames) {
        if(msgIds.size() != replacementNames.size())
            throw new InvalidParameterException("The msgIDs size is not " +
            		"the same as the replacement names size.");
        
        // Initialize the statement strings.
        String stmtUpdateUsernamesStart =
            "UPDATE messages " +
            "   SET vcUsername = CASE pk_MessageID ";
        String stmtUpdateUsernamesMeat = ""; // This will be 'WHEN # THEN ? \n'
        String stmtUpdateUsernamesEnd =
            "       ELSE vcUsername " +
            "   END " +
            "WHERE pk_MessageID in (";
        
        // Build the array of statements for the PreparedStatement.
        Iterator<MessageRow> it = msgIds.iterator();
        while(it.hasNext()) {
            int msgID = it.next().pk_MessageID;
            // Note: I deliberately decided not to make the
            //       msgID a ? parameter for a couple reasons.
            //       1. It is data straight from the database that users have never touched.
            //       2. It would be a pain in the royal arse to not do it this way.
            stmtUpdateUsernamesMeat += 
                "       WHEN " + msgID +" THEN ? \n";
            
            if(it.hasNext())
                stmtUpdateUsernamesEnd += msgID + ", ";
            else
                stmtUpdateUsernamesEnd += msgID + ") ";
        }
        
        // Combine the generated statement strings.
        String stmtCombined = stmtUpdateUsernamesStart + " " +
                              stmtUpdateUsernamesMeat + " " + 
                              stmtUpdateUsernamesEnd;
        
        try {
            // Initialize the PreparedStatement parameters.
            PreparedStatement stmt = getStmtForConn(stmtCombined);
            for(int i = 1; i <= replacementNames.size(); i++) {
                stmt.setString(i, replacementNames.get(i-1).vcUsername);
            }
            
            // Execute the query.
            stmt.executeUpdate();
          
        } catch (InvalidAttributesException e) {
            // There was no MySQL connection setup.
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
    }

    /**
     * Searches the database for all messages that contain the passed in keyword.
     * The keyword can be a part of another word, but it does not work 100% of the
     * time if this is the case.
     * @param keyword	Keyword to search the database for.
     * @return			Returns an ArrayList of MessageRows that contains all of
     * 					the information about the rows that contain the keyword.
     */
    public static ArrayList<MessageRow> searchMessagesForKeyword(String keyword) {
        ArrayList<MessageRow> result = new ArrayList<MessageRow>();
        
        String stmtSearchQuery = "SELECT * " +
        		                  "FROM messages " +
        		                  "WHERE MATCH(vcMessage)" +
        		                  "      AGAINST (? IN BOOLEAN MODE)";
        try {
            PreparedStatement stmt = getStmtForConn(stmtSearchQuery);
            stmt.setString(1, keyword);
            
            ResultSet rs = stmt.executeQuery();
            while(rs.next())
                result.add(new MessageRow(rs));
            
        } catch (InvalidAttributesException e) {
            // MySQL conn information not filled in.
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * This database query is fairly specific to jIBCObfuscate.  It might make
     * more sense if you look at the code in there.
     */
    public static void updateAllTargetsMessages(String vcUsername, 
            ArrayList<MessageRow> msgIds) {
        
        // Initialize the statement strings.
        String stmtUpdateUsernamesStart =
            "UPDATE messages " +
            "   SET vcMessage = CASE pk_MessageID ";
        String stmtUpdateUsernamesMeat = ""; // This will be 'WHEN # THEN ? \n'
        String stmtUpdateUsernamesEnd =
            "       ELSE vcMessage " +
            "   END " +
            "WHERE pk_MessageID in (";
        
        // Build the array of statements for the PreparedStatement.
        Iterator<MessageRow> it = msgIds.iterator();
        while(it.hasNext()) {
            int msgID = it.next().pk_MessageID;
            // Note: I deliberately decided not to make the
            //       msgID a ? parameter for a couple reasons.
            //       1. It is data straight from the database that users have never touched.
            //       2. It would be a pain in the royal arse to not do it this way.
            stmtUpdateUsernamesMeat += 
                "       WHEN " + msgID +" THEN ? \n";
            
            if(it.hasNext())
                stmtUpdateUsernamesEnd += msgID + ", ";
            else
                stmtUpdateUsernamesEnd += msgID + ") ";
        }
        
        // Combine the generated statement strings.
        String stmtCombined = stmtUpdateUsernamesStart + " " +
                              stmtUpdateUsernamesMeat + " " + 
                              stmtUpdateUsernamesEnd;
       
        try {
            
            // Initialize the PreparedStatement parameters.
            PreparedStatement stmt = getStmtForConn(stmtCombined);
            for(int i = 1; i <= msgIds.size(); i++) {
                stmt.setString(i, msgIds.get(i-1).vcMessage);
            }
            
            // Execute the query.
            stmt.executeUpdate();
          
        } catch (InvalidAttributesException e) {
            // There was no MySQL connection setup.
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } 
    }
}
