package org.hive13.jircbotx.listener;

import java.util.Iterator;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerAdapterX;
import org.hive13.jircbotx.ListenerThreadX;
import org.pircbotx.hooks.Listener;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

public class Quit extends ListenerAdapterX {
   
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
         initiateQuit(event.getBot());
      }
   }
   
   /* (non-Javadoc)
    * @see org.pircbotx.hooks.ListenerAdapter#onPrivateMessage(org.pircbotx.hooks.events.PrivateMessageEvent)
    */
   @Override
   public void onPrivateMessage(PrivateMessageEvent<JircBotX> event)
         throws Exception {
      super.onPrivateMessage(event);
      String[] splitMessage = event.getMessage().split(" ");
      if((event.getUser().getChannelsOpIn().size() > 0)
            && splitMessage.length > 0 && (splitMessage[0].compareToIgnoreCase("!quit") == 0))
      {
         initiateQuit(event.getBot());
      }
   }

   public void initiateQuit(JircBotX bot)
   {
      // Stop any running listeners
      boolean hadListeners = false;
      @SuppressWarnings("rawtypes")
      Iterator<Listener> itListeners = bot.getListenerManager().getListeners().iterator();
      while(itListeners.hasNext())
      {
         @SuppressWarnings("unchecked")
         Listener<JircBotX> curListener = itListeners.next();
         if(curListener instanceof ListenerThreadX)
         {
            ListenerThreadX listenerThread = (ListenerThreadX)curListener;
            listenerThread.stopCommandThread(true);
            hadListeners = true;
         }
      }
      if(hadListeners)
      {
         // Lets wait for a few seconds so the listenerThreads have a chance to stop.
         try {
            Thread.sleep(3000);
         } catch (InterruptedException e) {
            // if this fails... not a big deal.
         }
      }
      
      bot.quitServer("Goodbye cruel world!");
   }
}
