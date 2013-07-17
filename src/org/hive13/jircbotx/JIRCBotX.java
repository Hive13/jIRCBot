package org.hive13.jircbotx;

import java.io.IOException;
import java.net.MalformedURLException;

import org.hive13.jircbotx.listener.Linkify;
import org.hive13.jircbotx.listener.ChannelLogger;
import org.hive13.jircbotx.listener.Magic8Ball;
import org.hive13.jircbotx.listener.Quit;
import org.hive13.jircbotx.listener.RssReader;
import org.hive13.jircbotx.listener.Tell;
import org.hive13.jircbotx.listener.Temperature;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;

/**
 * 
 * @author vincentp
 * 
 */
public class JIRCBotX {

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
   
   /**
    * @param args
    */
   public static void main(String[] args) {
      String botChannel = "#hive13_bot";
      PircBotX bot = new PircBotX();
      bot.setName("H13Bot_Dev");
      bot.setLogin("H13Bot_Dev");

      bot.setAutoNickChange(true);
      bot.setAutoReconnect(true);
      bot.setAutoReconnectChannels(true);
      bot.setVerbose(true);
      
      bot.setCapEnabled(true);

      bot.getListenerManager().addListener(new Linkify());
      bot.getListenerManager().addListener(new Magic8Ball());
      bot.getListenerManager().addListener(new Temperature());
      bot.getListenerManager().addListener(new Tell());
      bot.getListenerManager().addListener(new ChannelLogger());
      bot.getListenerManager().addListener(new Quit());
      
      try {
         bot.getListenerManager().addListener(new RssReader(bot, "DoorAlert", botChannel, "http://www.hive13.org/isOpen/RSS.php"));
         bot.getListenerManager().addListener(new RssReader(bot, "Wiki", botChannel, "[commandName]: [Title|c50] ~[Author|c20] ([Link])", "http://wiki.hive13.org/index.php?title=Special:RecentChanges&feed=rss&hideminor=1"));
         bot.getListenerManager().addListener(new RssReader(bot, "Blog", botChannel, "[commandName]: [Title|c50] ([Link])", "http://www.hive13.org/?feed=rss2"));
         bot.getListenerManager().addListener(new RssReader(bot, "Flickr", botChannel, "[commandName]: [Title|c50] ([Link])", "http://api.flickr.com/services/feeds/photos_public.gne?tags=hive13&lang=en-us&format=rss_200", 15 * 60 * 1000)); // 15 min refresh period.
         bot.getListenerManager().addListener(new RssReader(bot, "Youtube", botChannel, "[commandName]: [Title|c30] ~[Author|c20|r\\(.+\\)] ([Link])", "http://gdata.youtube.com/feeds/base/videos/-/hive13?client=ytapi-youtube-browse&v=2"));
         bot.getListenerManager().addListener(new RssReader(bot, "Vimeo", botChannel, "[commandName]: [Title|c30] ~[Author|c20|r\\(.+\\)] ([Link])", "http://vimeo.com/groups/hive13/videos/rss"));
      } catch (MalformedURLException e1) {
         e1.printStackTrace();
      }
      
      try {
         bot.connect("irc.freenode.net");
         bot.joinChannel(botChannel);
      } catch (NickAlreadyInUseException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (IrcException e) {
         e.printStackTrace();
      }
   }

}
