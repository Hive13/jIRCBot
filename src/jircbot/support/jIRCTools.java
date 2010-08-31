package jircbot.support;
// TODO: Document this class.
import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.info;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jircbot.jIRCBot;

public class jIRCTools {
	public static final String UserAgentString = "Googlebot/2.1 (+http://www.googlebot.com/bot.html)";
	
    /** Username to use for the bit.ly API */
	public static String bitlyName = "";
	/** API key to use for the bit.ly API */
	public static String bitlyAPIKey = "";

	/** 
	 * This must be checked by each JDBC method to ensure
	 * that the JDBC integration is configured.
	 */
    public static boolean jdbcEnabled = false;
    /** JDBC URL to use to connect to the database */
	public static String jdbcURL = "";
    /** Username for the MySQL database to connect too */
	public static String jdbcUser = "";
    /** Password for the MySQL database user. */
	public static String jdbcPass = "";

    /** 
     * The different types of messages saved in the 
     * database.
     */
	public enum eMsgTypes {
	    publicMsg,
	    privateMsg,
	    actionMsg,
	    joinMsg,
	    partMsg, 
	    nickChange, 
	    quitMsg
	}
	
	/**
	 * Bit.ly API integration to generate a shortened URL.
	 * Make sure that the bitlyName and bitlyAPIKey are specified
	 * before calling this method.
	 * 
	 * @param longURL  Long URL to shorten.
	 * @return         Returns a bit.ly shortened URL
	 */
	public static String generateShortURL(String longURL) {
		return generateShortURL(longURL, bitlyName, bitlyAPIKey);
	}
	
	/**
	 * Uses the Bit.ly API to generate a short URL.
	 * 
	 * @param longURL  The log URL to shorten.
	 * @param username The Bit.ly username to use.
	 * @param apikey   Bit.ly APIKey associated w/ the username.
	 * @return         Returns a shortened bit.ly URL.
	 */
	public static String generateShortURL(String longURL, String username, String apikey) {
		String result = "Username or API key are not initialized";
		if(bitlyName.length() > 0 && bitlyAPIKey.length() > 0)
			result = as(username, apikey).call(shorten(longURL)).getShortUrl();
		return result;
	}
	
	/**
	 * Calls on the Bit.ly API to find the title of an already shortened
	 * URL. Make sure that the bitlyName and bitlyAPIKey are specified
     * before calling this method.
	 * 
	 * @param shortURL An already shortened Bit.ly URL.
	 * @return         Returns the title of the page the Bit.ly URL links too.
	 */
	public static String getShortURLTitle(String shortURL) {
	    return getShortURLTitle(shortURL, bitlyName, bitlyAPIKey);
	}
	
	/**
	 * Calls on the Bit.ly API to find the title of an already shortened
     * URL.
     * 
	 * @param shortURL An already shortened Bit.ly URL.
	 * @param username The Bit.ly username to use.
	 * @param apikey   Bit.ly APIKey associated w/ the username.
	 * @return         Returns the title of the page the Bit.ly URL links too.
	 */
	public static String getShortURLTitle(String shortURL, String username, String apikey) {
	    String result = "Username or API key are not initialized";
        if(bitlyName.length() > 0 && bitlyAPIKey.length() > 0)
            result = as(username, apikey).call(info(shortURL)).getTitle();
	    return result;
	}
	
	public static String getURLTitle(String myURL) {
		String result = "";
		try {
			URL url = new URL(myURL);
			URLConnection uconn = url.openConnection();
			if(uconn instanceof HttpURLConnection) {
				// Set up the request.
				HttpURLConnection conn = (HttpURLConnection)uconn;
				conn.setConnectTimeout(10000); 	// 10 seconds
				conn.setReadTimeout(10000); 	// 10 seconds
				conn.setRequestProperty("User-agent", UserAgentString);
				
				// Send the request.
				conn.connect();
				
				// Get the response.
				//Map<String, List<String>> responseHeader = conn.getHeaderFields();
				//int responseCode = conn.getResponseCode();
				String type = conn.getContentType();
				String rgxIsHTML = "(text/x?html|application/xhtml+xml)";
				Pattern p = Pattern.compile(rgxIsHTML);
				Matcher m = p.matcher(type);
				if(!m.find()) {
					result = type; // It is not a webpage.
				} else {
					// Now we need to get the text page.
					Object content = conn.getContent();
					if(content instanceof String) {
						String sContent = (String)content;
						String rgxFindTitle = "<title[^>]*>(.*?)</title>";
						p = Pattern.compile(rgxFindTitle);
						m = p.matcher(sContent);
						if(m.find()) {
							result = m.group();
						}
					}
				}
			} else {
				// This would be FTP I assume.
				// just fall out w/ a blank title.
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	/**
     * Inserts a new message into the "messages" table of the
     * attached MySQL database.
     * 
     * @param channel  Name of the channel the message relates to.
     * @param server   The server the bot is connected too.
     * @param username The username of related to this message.
     * @param msg      The actual contents of the message.
     * @param msgType  The type of message (Public, private, join // part, etc..)
     */
    public static void insertMessage(String channel, String server, String username, String msg, eMsgTypes msgType) {
        if(!jIRCTools.jdbcEnabled)
            return;
        
        int chanID = getChannelID(channel, server);
        if(chanID == -1) { // Channel does not exist... try to create it.
            if((chanID = insertChannel(channel, server)) == -1) { // Did the create work?
                Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, "Failed to insert new channel: " + channel + "@" + server + "\n");
            }
        }
        
        String insertStatement = "INSERT INTO messages " +
                "( fk_ChannelID, vcMsgType, vcUsername, vcMessage )" +
                "VALUES" +
                "( ?, ?, ?, ? )";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPass);
            PreparedStatement stmt = conn.prepareStatement(insertStatement);
            stmt.setInt(1, chanID);
            stmt.setString(2, msgType.toString());
            stmt.setString(3, username);
            stmt.setString(4, msg);
            stmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Inserts a new message into the "messages" table of the
     * attached MySQL database.
     * 
     * @param channel  Name of the channel the message relates to.
     * @param server   The server the bot is connected too.
     * @param username The username of related to this message.
     * @param msg      The actual contents of the message.
     * @param msgType  The type of message (Public, private, join // part, etc..)
     */
    public static void insertMessage(String channel, String server, String username, String msg, eMsgTypes msgType, String tsMsgDate) {
        if(!jIRCTools.jdbcEnabled)
            return;
        
        int chanID = getChannelID(channel, server);
        if(chanID == -1) { // Channel does not exist... try to create it.
            if((chanID = insertChannel(channel, server)) == -1) { // Did the create work?
                Logger.getLogger(jIRCBot.class.getName()).log(Level.SEVERE, "Failed to insert new channel: " + channel + "@" + server + "\n");
            }
        }
        
        String insertStatement = "INSERT INTO messages " +
                "( fk_ChannelID, vcMsgType, vcUsername, vcMessage, tsMsgTime )" +
                "VALUES" +
                "( ?, ?, ?, ?, ? )";
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPass);
            PreparedStatement stmt = conn.prepareStatement(insertStatement);
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
        }
    }
	
	/**
	 * Finds the database ID for a channel && server
	 * 
	 * @param channel  The name of the channel.
	 * @param server   The server the bot is connected too.
	 * @return         The ID # for the channel or -1 if it
	 *                 is unable to find the channel.
	 */
	public static int getChannelID(String channel, String server) {
	    if(!jIRCTools.jdbcEnabled)
            return -1;
	    
	    String stmtGetChannelID = "SELECT pk_ChannelID FROM channel WHERE vcServer=? AND vcChannel=?";
	    
	    try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPass);
            PreparedStatement stmt = conn.prepareStatement(stmtGetChannelID);
            stmt.setString(1, server);
            stmt.setString(2, channel);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                return rs.getInt("pk_ChannelID");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	    return -1; // Failed to find a channelID
	}
	
	/**
	 * Inserts a new channel, it does not check to make sure the
	 * channel // server pair does not already exist.
	 * 
	 * @param channel  Name of the channel
	 * @param server   Name of the server
	 * @return         The ID # for the new channel or 
	 *                 -1 if it fails to add the channel.
	 */
	public static int insertChannel(String channel, String server) {
	    if(!jIRCTools.jdbcEnabled)
            return -1;
	    
	    String stmtInsertChannel = "INSERT INTO channel " +
	    		"( vcChannel, vcServer )" +
	    		"VALUES" +
	    		"( ?, ?)";
	    
	    try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPass);
            PreparedStatement stmt = conn.prepareStatement(stmtInsertChannel);
            stmt.setString(1, channel);
            stmt.setString(2, server);
            stmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
	    return getChannelID(channel, server);
	}

}
