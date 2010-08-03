package jircbot;

import static com.rosaloves.bitlyj.Bitly.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import jircbot.jIRCTools.eMsgTypes;

/*
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
*/
public class jIRCTools {
	public static String bitlyName = "";
	public static String bitlyAPIKey = "";
	
	public static String jdbcURL = "jdbc:mysql://192.168.2.143:3306/channellog";
	public static String jdbcUser = "chanlogger";
	public static String jdbcPass = "qF7yWI4Tcnsb4qD";
	
	public enum eMsgTypes {
	    publicMsg,
	    privateMsg,
	    statusMsg
	}
	
	public static String generateShortURL(String longURL) {
		return generateShortURL(longURL, bitlyName, bitlyAPIKey);
	}
	public static String generateShortURL(String longURL, String username, String apikey) {
		String result = "Username or API key are not initialized";
		if(bitlyName.length() > 0 && bitlyAPIKey.length() > 0)
			result = as(username, apikey).call(shorten(longURL)).getShortUrl();
		return result;
	}
	
	public static void insertMessage(String channel, String server, String username, String msg, eMsgTypes msgType) {
	    
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
	
	public static int getChannelID(String channel, String server) {
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
	
	public static int insertChannel(String channel, String server) {
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
