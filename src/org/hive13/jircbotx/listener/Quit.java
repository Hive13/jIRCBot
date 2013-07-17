package org.hive13.jircbotx.listener;

import org.hive13.jircbotx.ListenerAdapterX;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.QuitEvent;

public class Quit extends ListenerAdapterX {

   boolean ranQuit = false;
   
   // TODO: Add an auth level restriction to 'Quit', for now we hide it.
   public Quit()
   {
      bHideCommand = true;
   }
   
   @Override
   public String getCommandName() {
      return "quit";
   }

   @Override
   public String getHelp() {
      return "Shuts down the bot";
   }

   public void handleMessage(MessageEvent<PircBotX> event) throws Exception {
      if (event.getMessage().toLowerCase().startsWith("!quit"))
      {
         event.getBot().quitServer();
         ranQuit = true;
      }
   }
   
   public void onQuit(QuitEvent<PircBotX> event) throws Exception {
      if(ranQuit)
         System.exit(0);
   }
   
   
}
