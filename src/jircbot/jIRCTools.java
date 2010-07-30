package jircbot;

import static com.rosaloves.bitlyj.Bitly.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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
	    String query = "INSERT INTO messages ";
	    query += "( fk_ChannelID, intMsgType, vcUsername, vcMessage )";
	    query += "VALUES ";
	    query += "( 1, 1, \"" + username + "\", \"" + msg + "\" )";
        
	    updateDatabase(query);
	}
	
	private static ResultSet queryDatabase(String statement) {
	    ResultSet rs = null;
	    
	    try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.2.143:3306/channellog", "chanlogger", "qF7yWI4Tcnsb4qD");
            Statement stmt = conn.createStatement();
            
            rs = stmt.executeQuery(statement);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	    
	    return rs;
	}
	
	private static void updateDatabase(String statement) {
	    try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.2.143:3306/channellog", "chanlogger", "qF7yWI4Tcnsb4qD");
            Statement stmt = conn.createStatement();
            
            stmt.execute(statement);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}
}
