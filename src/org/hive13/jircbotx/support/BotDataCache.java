package org.hive13.jircbotx.support;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class BotDataCache {
   private static final String dataFileName = "jIRCBot.data";
   private static BotDataCache instance = null;

   private Properties data;

   private String defaultTweetStatusID = "0";
   
   protected BotDataCache() {
      data = new Properties();
      try {
         data.load(new FileInputStream(dataFileName));
      } catch (IOException ex) {
         System.err.println(ex);
      }
   }

   /**
    * Get an instance of jIRCData.
    * @return  The singleton instance of jIRCData.
    */
   public synchronized static BotDataCache getInstance() {
      if (instance == null) {
         instance = new BotDataCache();
      }
      return instance;
   }
   
   /**
    * Refresh information from the data file.
    * @return Returns a fresh instance of jIRCData
    */
   public synchronized static BotDataCache refresh() {
       instance = null; // Kill the current instance.
       return getInstance();
   }
   
   public String getProp(String key, String defaultString) {
       return data.getProperty(key, defaultString);
   }
   
   public void setProp(String key, String value) {
       data.setProperty(key, value);
       try {
           data.store(new FileOutputStream(dataFileName), null);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
   
   public long getLatestTweetID(String commandName)
   {
      String result = getProp("twitterStatusID-" + commandName, defaultTweetStatusID);
      return Long.parseLong(result);
   }
   
   public void setLatestTweetID(String commandName, long StatusID)
   {
      setProp("twitterStatusID-" + commandName, Long.toString(StatusID));
   }
}
