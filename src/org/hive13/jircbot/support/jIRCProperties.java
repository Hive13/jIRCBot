package org.hive13.jircbot.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class jIRCProperties {
	private static jIRCProperties instance = null;
	
	private Properties config;
	private String defaultBotName 	= "Hive13Bot";
	private String defaultServer 	= "irc.freenode.net";
	private String defaultChannels	= "#Hive13_test";
	
	private String defaultBitlyName = "";
	private String defaultBitlyKey 	= "";
	
	private String defaultJDBCUrl 	= "";
	private String defaultJDBCUser 	= "";
	private String defaultJDBCPass 	= "";
	
	private String defaultUserAgent	= "Googlebot/2.1 (+http://www.googlebot.com/bot.html)";
	
	protected jIRCProperties() {
		config = new Properties();
		try {
			config.load(new FileInputStream("jIRCBot.properties"));
		} catch (IOException ex) {
			System.err.println(ex);
		}
	}
	
	public synchronized static jIRCProperties getInstance() {
		if(instance == null) {
			instance = new jIRCProperties();
		}
		return instance;
	}
	
	public String getProp(String key, String defaultString) {
		return config.getProperty(key, defaultString);
	}
	
	public String getBotName() {
		return getProp("nick", defaultBotName);
	}
	public String getServer() {
		return getProp("server", defaultServer);
	}
	public String[] getChannels() {
		String channels = getProp("channels", defaultChannels);
		String splitChannels[] = channels.split(",");
		for(int i = 0; i < splitChannels.length; i++) {
			splitChannels[i] = splitChannels[i].trim();
		}
		return splitChannels;
	}
	
	/** Username to use for the bit.ly API */
	public String getBitlyName() {
		return getProp("bitlyName", defaultBitlyName);
	}
	/** API key to use for the bit.ly API */
	public String getBitlyAPIKey() {
		return getProp("bitlyAPI", defaultBitlyKey);
	}

	/** JDBC URL to use to connect to the database */
	public String getJDBCUrl() {
		return getProp("jdbcURL", defaultJDBCUrl);
	}
	/** Username for the MySQL database to connect too */
	public String getJDBCUser() {
		return getProp("jdbcUsername", defaultJDBCUser);
	}
	/** Password for the MySQL database user. */
	public String getJDBCPass() {
		return getProp("jdbcPassword", defaultJDBCPass);
	}
	
	/**	When connecting to certain websites, if it thinks the connection
	 *  is from a bot it will block the connection.  In this case we
	 *  pretend that we are the google bot.
	 */
	public String getUserAgentString() {
		return getProp("userAgentString", defaultUserAgent);
	}
}
