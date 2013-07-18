package org.hive13.jircbotx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hive13.jircbotx.listener.Linkify;
import org.hive13.jircbotx.listener.ChannelLogger;
import org.hive13.jircbotx.listener.Magic8Ball;
import org.hive13.jircbotx.listener.Plugins;
import org.hive13.jircbotx.listener.Quit;
import org.hive13.jircbotx.listener.RssReader;
import org.hive13.jircbotx.listener.Tell;
import org.hive13.jircbotx.listener.Temperature;
import org.hive13.jircbotx.listener.UserAuth;
import org.hive13.jircbotx.support.BotProperties;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.WhoisEvent;

/**
 * 
 * @author vincentp
 * 
 */
public class JIRCBotX {

   private static PircBotX bot;
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
      Properties config = new Properties();
      try {
         config.load(new FileInputStream("jIRCBot.properties"));
      } catch (FileNotFoundException e2) {
         Logger.getLogger(JIRCBotX.class.getName()).log(Level.SEVERE, null,
               e2);
      } catch (IOException e2) {
         Logger.getLogger(JIRCBotX.class.getName()).log(Level.SEVERE, null,
               e2);
      }
      
      String[] botChannels = BotProperties.getInstance().getChannels();
      String botChannel = botChannels[0];
      
      bot = new PircBotX();
      bot.setName(BotProperties.getInstance().getBotName());
      bot.setLogin(BotProperties.getInstance().getBotName());

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
      bot.getListenerManager().addListener(new Plugins(bot));
      bot.getListenerManager().addListener(new UserAuth());
      
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
         bot.connect(BotProperties.getInstance().getServer(), 
               6667, 
               BotProperties.getInstance().getBotPass());
         
         bot.joinChannel(botChannel);
      } catch (NickAlreadyInUseException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (IrcException e) {
         e.printStackTrace();
      }
   }

   public static String getRegisteredName(String userNick) throws InterruptedException
   {
      String result;
      bot.sendRawLine("WHOIS " + userNick + " " + userNick);
      WaitForQueue queue = new WaitForQueue(bot);
      while(true) {
         @SuppressWarnings("unchecked")
         WhoisEvent<PircBotX> mevent = queue.waitFor(WhoisEvent.class);
         result = mevent.getRegisteredAs();
         break;
      }
      queue.close();
      return result;
   }
}
