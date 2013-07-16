package org.hive13.jircbotx;

import java.io.IOException;

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
      if (event.getMessage().startsWith("?hello"))
         event.respond("Hello World!");

      // If this isn't a waittest, ignore
      // This way to handle commands is useful for listers that only listen for
      // one command
      if (!event.getMessage().startsWith("?waitTest start"))
         return;

      // WaitTest has started
      event.respond("Started...");
      WaitForQueue queue = new WaitForQueue(event.getBot());
      // Infinite loop since we might receive messages that aren't WaitTest's.
      while (true) {
         // Use the waitFor() method to wait for a MessageEvent.
         // This will block (wait) until a message event comes in, ignoring
         // everything else
         MessageEvent<PircBotX> currentEvent = queue.waitFor(MessageEvent.class);
         // Check if this message is the "ping" command
         if (currentEvent.getMessage().startsWith("?waitTest ping"))
            event.respond("pong");
         // Check if this message is the "end" command
         else if (currentEvent.getMessage().startsWith("?waitTest end")) {
            event.respond("Stopping");
            queue.close();
            // Very important that we end the infinite loop or else the test
            // will continue forever!
            return;
         }
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
