package org.hive13.jircbotx.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class BotProperties {
   private static BotProperties instance = null;

   private Properties config;
   private final String defaultBotName    = "Hive13Bot";
   private final String defaultBotPass   = "";
   private final String defaultServer  = "irc.freenode.net";
   private final String defaultChannels   = "#Hive13_test";
   private final String defaultOpChannels    = "#Hive13_test";
   
   private final String defaultBitlyName = "";
   private final String defaultBitlyKey   = "";

   private final String defaultWundergroundKey = "";

   private final String defaultGitHubUser = "";
   private final String defaultGitHubPass = "";
   private final String defaultGitHubOrg = "";

   private final String defaultTwitterKey = "";
   private final String defaultTwitterSecret = "";
   private final String defaultTwitterAccessToken = "";
   private final String defaultTwitterAccessSecret = "";
   
   private final String defaultJDBCUrl    = "";
   private final String defaultJDBCUser   = "";
   private final String defaultJDBCPass   = "";

   private final String defaultUserAgent = "Googlebot/2.1 (+http://www.googlebot.com/bot.html)";
   
   private final String defaultNickServ   = "nickserv";
   private final String defaultOpUserList    = "";
   private final String defaultAdminUserList    = "";
   
   private final String defaultPlugins   = "";

    private final String defaultCacheDirPath = "./jIRCBotCache";
   
    /** Directory for commands to use as a cache for data. */
    private static File cacheDirectory = null;
 
    private String parsedChannels[] = null;
    private String parsedOpChannels[] = null;
   private List<String> parsedOpList = null;
   private List<String> parsedAdminList = null;
   private List<String> parsedPlugins = null;
   
   protected BotProperties() {
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

   public synchronized static BotProperties getInstance() {
      if (instance == null) {
         instance = new BotProperties();
      }
      return instance;
   }

   public synchronized static BotProperties refresh() {
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

   public String[] getOpChannels() {
      return getOpChannels(false);
   }
   public String[] getOpChannels(boolean refreshList) {
      // Since we only read the properties once (unless refreshed), it does not make sense
      // to repeatedly re-parse the channel string.
      if(parsedChannels == null || refreshList) {
         String channels = getProp("opChannels", defaultOpChannels);
         String splitChannels[] = channels.split(",");
         for (int i = 0; i < splitChannels.length; i++) {
            splitChannels[i] = splitChannels[i].trim();
         }
         parsedOpChannels = splitChannels;
      }
      return parsedOpChannels;
   }

   /** Username to use for the bit.ly API */
   public String getBitlyName() {
      return getProp("bitlyName", defaultBitlyName);
   }

   /** API key to use for the bit.ly API */
   public String getBitlyAPIKey() {
      return getProp("bitlyAPI", defaultBitlyKey);
   }

   /** API key to use for the Weather Underground API */
   public String getWundergroundAPIKey() {
      return getProp("WUAPI", defaultWundergroundKey);
   }

   /** API key to use for the Weather Underground API */
   public String getGitHubLogin() {
      return getProp("githubUser", defaultGitHubUser);
   }

   /** API key to use for the Weather Underground API */
   public String getGitHubPass() {
      return getProp("githubPass", defaultGitHubPass);
   }

   /** API key to use for the Weather Underground API */
   public String getGitHubOrg() {
      return getProp("githubOrg", defaultGitHubOrg);
   }

   /** API key to use for the Weather Underground API */
   public String getTwitterKey() {
      return getProp("twitterKey", defaultTwitterKey);
   }

   /** API key to use for the Weather Underground API */
   public String getTwitterSecret() {
      return getProp("twitterSecret", defaultTwitterSecret);
   }

   /** API key to use for the Weather Underground API */
   public String getTwitterAccessToken() {
      return getProp("twitterAccessToken", defaultTwitterAccessToken);
   }

   /** API key to use for the Weather Underground API */
   public String getTwitterAccessSecret() {
      return getProp("twitterAccessSecret", defaultTwitterAccessSecret);
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
    * This function returns the path to the cache directory.
    * It also attempts to create the cache directory if it
    * does not already exist.
    * 
    * @return Path to the cache directory.
    */
   public String getCacheDirectoryPath() {
       String path = getProp("CacheDirectoryPath", defaultCacheDirPath);
       if(cacheDirectory == null)
           cacheDirectory = new File(path);
       if(!cacheDirectory.exists())
           cacheDirectory.mkdir();
       return path;
   } 
   
   /**
     * Attempt to get a pointer to the jIRCBot cache directory. This method
     * attempts to create the directory first if it does not exist, however it
     * does not guarantee that the directory will actually exist.
     * 
     * @return Returns a pointer to the cache directory that the bot can read //
     *         write too.
     */
    public File getCacheDirectory() {
        if(cacheDirectory == null) {
            cacheDirectory = new File(getCacheDirectoryPath());
        }
        if (!cacheDirectory.exists()) {
            cacheDirectory.mkdir();
        }
        return cacheDirectory;
    }

    /**
     * Find the list of users that are to be authorized.
     */
    public List<String> getOpUserList() {
        return getOpUserList(false);
    }
    /**
     * Find the list of users that are to be authorized.
     * @param refresh   Force refresh from the properties file.
     * @return
     */
    public List<String> getOpUserList(boolean refresh) {
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
