package org.hive13.jircbotx.listener;

import java.util.Iterator;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerAdapterX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.MessageEvent;

public class Plugins extends ListenerAdapterX {

   @Override
   public void handleMessage(MessageEvent<JircBotX> event) throws Exception
   {
      String[] splitMsg = event.getMessage().split(" ");
      if(splitMsg.length > 0 && splitMsg[0].equals("!" + getCommandName()))
      {
         String cmdList = "";
         @SuppressWarnings("rawtypes")
         Iterator<Listener> it = event.getBot().getListenerManager().getListeners().iterator();
         while(it.hasNext())
         {
            @SuppressWarnings("rawtypes")
            Listener cur = it.next();
            if(cur instanceof ListenerAdapterX)
            {
               ListenerAdapterX curX = (ListenerAdapterX) cur;
               if(curX != null && !curX.isHidden() && curX.shouldListenToChannel(event.getChannel().getName()))
                  cmdList += curX.getCommandName() + ", ";
               
            }
         }
         if(!cmdList.isEmpty())
         {
            cmdList = cmdList.substring(0, cmdList.length() - 2); // remove trailing ,
            cmdList = "Type !help commandName for more information.  Available commands: " + cmdList;
            event.getUser().sendMessage(cmdList);
         }
      }
   }
   
   @Override
   public String getCommandName() {
      return "plugins";
   }

   @Override
   public String getHelp() {
      return "Returns a list of available plugins";
   }

}
