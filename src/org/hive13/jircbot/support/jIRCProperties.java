package org.hive13.jircbot.support;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class jIRCProperties {
	private static jIRCProperties instance = null;

	private Properties config;
	private final String defaultBotName 	= "Hive13Bot";
	private final String defaultBotPass   = "";
	private final String defaultServer 	= "irc.freenode.net";
	private final String defaultChannels 	= "#Hive13_test";
	
	private final String defaultBitlyName = "";
	private final String defaultBitlyKey 	= "";

	private final String defaultJDBCUrl 	= "";
	private final String defaultJDBCUser 	= "";
	private final String defaultJDBCPass 	= "";

	private final String defaultUserAgent = "Googlebot/2.1 (+http://www.googlebot.com/bot.html)";
	
	private final String defaultNickServ	= "nickserv";
	private final String defaultOpUserList	   = "";
	private final String defaultAdminUserList    = "";
	
	private final String defaultPlugins   = "";

	private String parsedChannels[] = null;
	private List<String> parsedOpList = null;
	private List<String> parsedAdminList = null;
	private List<String> parsedPlugins = null;
	
	protected jIRCProperties() {
		config = new Properties();
		try {
			config.load(new FileInputStream("jIRCBot.properties"));
		} catch (IOException ex) {
			System.err.println(ex);
		}
		parsedOpList = null;
		parsedAdminList = null;
		parsedPlugins = null;
	}

	public synchronized static jIRCProperties getInstance() {
		if (instance == null) {
			instance = new jIRCProperties();
		}
		return instance;
	}

	public synchronized static jIRCProperties refresh() {
		instance = null; // Kill the current instance.
		return getInstance();
	}
	
	public String getProp(String key, String defaultString) {
		return config.getProperty(key, defaultString);
	}

	public String getBotName() {
        return getProp("nick", defaultBotName);
    }
	
	public String getBotPass() {
        return getProp("pass", defaultBotPass);
    }

	public String getServer() {
		return getProp("server", defaultServer);
	}

	public String[] getChannels() {
		// Since we only read the properties once, it does not make sense
		// to repeatedly re-parse the channel string.
		if(parsedChannels == null) {
			String channels = getProp("channels", defaultChannels);
			String splitChannels[] = channels.split(",");
			for (int i = 0; i < splitChannels.length; i++) {
				splitChannels[i] = splitChannels[i].trim();
			}
			parsedChannels = splitChannels;
		}
		return parsedChannels;
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

	/**
	 * When connecting to certain websites, if it thinks the connection is from
	 * a bot it will block the connection. In this case we pretend that we are
	 * the google bot.
	 */
	public String getUserAgentString() {
		return getProp("userAgentString", defaultUserAgent);
	}
	
	/**
	 * It is possible that on different servers different
	 * usernames may be used for the bot that handles
	 * authenticating users.
	 * 
	 * However, the bot will still need to follow the
	 * message format used by the bot on the Freenode.net
	 * network.
	 */
	public String getNickServUsername() {
		return getProp("NickServUsername", defaultNickServ);
	}

    /**
     * Find the list of users that are to be authorized.
     */
    public List<String> getOpUserList() {
        // Since we only read the properties once, it does not make sense
        // to repeatedly re-parse the channel string.
        if(parsedOpList == null) {
            String users = getProp("OpUserList", defaultOpUserList).toLowerCase();
            String splitUsers[] = users.split(", ?");
            parsedOpList = new ArrayList<String>(Arrays.asList(splitUsers));
        }
        return parsedOpList;
    }

    /**
     * Find the list of users that are to be authorized.
     */
    public List<String> getAdminUserList() {
        // Since we only read the properties once, it does not make sense
        // to repeatedly re-parse the string.
        if(parsedAdminList == null) {
            String users = getProp("AdminUserList", defaultAdminUserList).toLowerCase();
            String splitUsers[] = users.split(", ?");
            parsedAdminList = new ArrayList<String>(Arrays.asList(splitUsers));
        }
        return parsedAdminList;
    }

    /**
     * Find the list of plugins that need to be loaded.
     */
    public List<String> getPluginsList() {
        // Since we only read the properties once, it does not make sense
        // to repeatedly re-parse the string.
        if(parsedPlugins == null) {
            String plugins = getProp("plugins", defaultPlugins).toLowerCase();
            String splitPlugins[] = plugins.split(", ?");
            parsedPlugins = new ArrayList<String>(Arrays.asList(splitPlugins));
        }
        return parsedPlugins;
    }
}
