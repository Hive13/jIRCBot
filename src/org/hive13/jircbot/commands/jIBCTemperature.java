/*
 * Weather Underground Info:
 * - General API documentation: http://www.wunderground.com/weather/api/d/documentation.html
 * - Java API page: http://code.google.com/p/wunderground-core/
 * - Wunderground API Key: ad6ac91937e9a86c/
 * IMPORTANT: API key is limited to 10 requests / minute, 500 requests / day
 * 
 */
package org.hive13.jircbot.commands;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.jIRCProperties;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 * @author vincentp
 *
 */
public class jIBCTemperature extends jIBCommand {

   private URL tempFeedURL = null;
   
   /* (non-Javadoc)
    * @see org.hive13.jircbot.commands.jIBCommand#getCommandName()
    */
   @Override
   public String getCommandName() {
      return "Temp";
   }

   /* (non-Javadoc)
    * @see org.hive13.jircbot.commands.jIBCommand#getHelp()
    */
   @Override
   public String getHelp() {
      return "Returns the temperature inside Hive13 and the local temperature. Ex. !" + getCommandName();
   }

   /* (non-Javadoc)
    * @see org.hive13.jircbot.commands.jIBCommand#handleMessage(org.hive13.jircbot.jIRCBot, java.lang.String, java.lang.String, java.lang.String)
    */
   @Override
   protected void handleMessage(jIRCBot bot, String channel, String sender,
         String message) {
      // Retrieve the current Hive13 temperature
      float fTemperature = getTemperatureFromRSS();
      // Retrieve the current local temperature
      // Send a message to the channel w/ the current Hive13 & Local temperature.
   }
   
   private float getTemperatureFromRSS() {
      float result = -1;
      // Here we pretend to be the google bot to fake out User-Agent
      // sniffing programs.
      try {
         URLConnection conn = tempFeedURL.openConnection();
         conn.setRequestProperty("User-Agent", jIRCProperties.getInstance()
               .getUserAgentString());

         // Create a feed off of the URL and get the latest news.
         SyndFeedInput input = new SyndFeedInput();
         SyndFeed feed = input.build(new XmlReader(conn));

         // Get the feed's list of entries
         @SuppressWarnings("unchecked")
         List<SyndEntry> entryList = feed.getEntries();
         Collections.sort(entryList, new SyndEntryComparator());
         
         if(entryList.size() > 0) {
            // We have found an RSS feed at least, is it formatted correctly?
         }
         else {
            // We have not found an RSS feed for the hackerspace tempreature.
         }
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (IllegalArgumentException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (FeedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      
      return result;
   }
   
   private double getTemperatureFromWunderground() {
      double result = -1;
      
      // TODO: See Tools for method of retrieving wUnderground data.
      
      return result;
   }

   /**
    * 
    * This class is simply here to sort lists of SyndEntries.
    * 
    */
   class SyndEntryComparator implements Comparator<SyndEntry> {
      public int compare(SyndEntry o1, SyndEntry o2) {
         int pubDateCompare = o2.getPublishedDate().compareTo(
               o1.getPublishedDate());
         return pubDateCompare;
      }
   }
}
