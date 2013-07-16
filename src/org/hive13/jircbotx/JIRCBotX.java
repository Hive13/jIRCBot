package org.hive13.jircbotx;

import java.io.IOException;

import org.hive13.jircbotx.listener.Linkify;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.pircbotx.exception.NickAlreadyInUseException;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;

/**
 * 
 * @author vincentp
 * 
 */
public class JIRCBotX extends ListenerAdapter<PircBotX> implements
      Listener<PircBotX> {

   public void onMessage(MessageEvent<PircBotX> event) throws Exception {
      // Hello world
      // This way to handle commands is useful for listeners that listen for
      // multiple commands
      if (event.getMessage().startsWith("!quit"))
      {
         event.getBot().quitServer();
         System.exit(0);
         
      }
   }

   /**
    * @param args
    */
   public static void main(String[] args) {
      PircBotX bot = new PircBotX();
      bot.setName("Hive13Bot_InTraining");
      bot.setLogin("Hive13Bot");

      bot.setAutoNickChange(true);
      bot.setAutoReconnect(true);
      bot.setAutoReconnectChannels(true);
      bot.setVerbose(true);
      
      bot.setCapEnabled(true);

      bot.getListenerManager().addListener(new JIRCBotX());
      bot.getListenerManager().addListener(new Linkify());
      
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
