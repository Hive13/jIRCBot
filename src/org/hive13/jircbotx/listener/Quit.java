package org.hive13.jircbotx.listener;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerAdapterX;
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

   public void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      
      if (event.getUser().getChannelsOpIn().contains(event.getChannel()) // If the calling user is an op in this channel
            && event.getMessage().toLowerCase().startsWith("!quit"))     // and if the the user issued the !quit command.
      {
         event.getBot().disconnect();
         ranQuit = true;
      }
   }
   
   public void onQuit(QuitEvent<JircBotX> event) throws Exception {
      //if(ranQuit)
      //   System.exit(0);
   }
   
   
}
