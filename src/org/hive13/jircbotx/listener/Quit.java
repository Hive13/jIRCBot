package org.hive13.jircbotx.listener;

import org.pircbotx.PircBotX;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public class Quit extends ListenerAdapter<PircBotX> {

   public void onMessage(MessageEvent<PircBotX> event) throws Exception {
      if (event.getMessage().toLowerCase().startsWith("!quit"))
      {
         event.getBot().quitServer();
         System.exit(0);
         
      }
   }
}
