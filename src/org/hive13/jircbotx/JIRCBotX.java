package org.hive13.jircbotx;

import java.io.IOException;
import java.util.ArrayList;

import org.hive13.jircbotx.listener.Linkify;
import org.hive13.jircbotx.listener.Logger;
import org.hive13.jircbotx.listener.Magic8Ball;
import org.hive13.jircbotx.listener.Quit;
import org.hive13.jircbotx.listener.Tell;
import org.hive13.jircbotx.listener.Temperature;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

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
      bot.getListenerManager().addListener(new Logger());
      bot.getListenerManager().addListener(new Quit());
      
      try {
         bot.connect("irc.freenode.net");
         bot.joinChannel("#hive13_bot");
      } catch (NickAlreadyInUseException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } catch (IrcException e) {
         e.printStackTrace();
      }
   }

}
