package jircbot;

import static com.rosaloves.bitlyj.Bitly.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	
	public static String generateShortURL(String longURL) {
		return generateShortURL(longURL, bitlyName, bitlyAPIKey);
	}
	public static String generateShortURL(String longURL, String username, String apikey) {
		String result = "Username or API key are not initialized";
		if(bitlyName.length() > 0 && bitlyAPIKey.length() > 0)
			result = as(username, apikey).call(shorten(longURL)).getShortUrl();
		return result;
	}
	
	public static void insertMessage(String channel, String server, String username, String msg) {
	    String insertStatement = "INSERT INTO messages " +
	    		"( fk_ChannelID, intMsgType, vcUsername, vcMessage )" +
	    		"VALUES" +
	    		"( ?, ?, ?, ? )";
	    try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/channellog", "chanlogger", "qF7yWI4Tcnsb4qD");
            PreparedStatement stmt = conn.prepareStatement(insertStatement);
            stmt.setInt(1, 1);
            stmt.setInt(2, 1);
            stmt.setString(3, username);
            stmt.setString(4, msg);
            stmt.executeUpdate();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
}
