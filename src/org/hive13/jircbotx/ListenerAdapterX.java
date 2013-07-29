package org.hive13.jircbotx;

import java.util.ArrayList;

import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

public abstract class ListenerAdapterX extends ListenerAdapter<JircBotX> {
   
   
   protected ArrayList<String> ListenerChannelList;

   public ListenerAdapterX() { this(""); }
   public ListenerAdapterX(String channelList)
   {
      ListenerChannelList = new ArrayList<String>();
      
      String[] splitList = channelList.split(",");
      for(int i = 0; i < splitList.length; ++i)
      {
         if(!splitList[i].isEmpty())
            ListenerChannelList.add(splitList[i]);
      }
   }
   
   public boolean shouldListenToChannel(String channel)
   {
      return (!isChannelRestricted() || (isChannelRestricted() && isListenerChannel(channel)));
   }
   public boolean isChannelRestricted()
   {
      return (ListenerChannelList.size() > 0);
   }
   public boolean isListenerChannel(String channel)
   {
      return ListenerChannelList.contains(channel.toLowerCase());
   }
   public void addListenerChannels(String channel)
   {
      String[] splitChannels = channel.toLowerCase().split(",");
      for(int i = 0; i < splitChannels.length; ++i)
      {
         ListenerChannelList.add(splitChannels[i]);
      }
   }
   
   /**
    * Check if we should handle the passed in msg. 
    * Returns the # of parameters in the msg, including the command name.
    * Returns -1 if we should NOT handle the message.  
    * 
    * @param msg  Msg to check if we should handle.
    * @return
    */
   public String[] shouldHandleMsg(String msg)
   {
      String result[] = null;
      
      String splitMessage[] = msg.split(" ");
      
      if (splitMessage.length >= 1 && splitMessage[0].equalsIgnoreCase("!" + getCommandName().toLowerCase()))
         // do nothing
         result = splitMessage;
      
      return result;
   }
   
   /**
    * This member variable is of pretty limited use. It basically exists for
    * commands that we might use on the backend (like Linkify) which the user
    * does not need to ever know about, but are actually implemented as
    * commands.
    */
   protected boolean bHideCommand = false;
   public boolean isHidden() {
      return bHideCommand;
   }
   
   public final void onMessage(MessageEvent<JircBotX> event) throws Exception
   {
      if(shouldListenToChannel(event.getChannel().getName()))
      {
         String message = event.getMessage().toLowerCase();
         String[] splitMsg = message.split(" ");
         
         if(splitMsg.length == 2 &&       // Only two items in the message
               splitMsg[0].equals("!" + getCommandName()) && // First is !command
               splitMsg[1].equals("help") &&                 // Second is help
               !getHelp().isEmpty())                         // We have help text to send.
         {
            event.getUser().sendMessage(getHelp());
         }
         else if(splitMsg.length == 2 && 
               splitMsg[0].equals("!help") &&
               splitMsg[1].equals(getCommandName()) &&
               !getHelp().isEmpty())
         {
            event.getUser().sendMessage(getHelp());
         }
         else
            handleMessage(event);
      }
   }
   
   /**
    * This method is internal to the commands. If a command wants to receive the 'onMessage'
    * function, it needs to use handleMessage instead. 
    */
   protected void handleMessage(MessageEvent<JircBotX> event) throws Exception {}
   
   /**
    * This method returns a unique name for the command.
    * 
    * @return A unique name for the command.
    */
   public abstract String getCommandName();
   
   /**
    * This method returns instructions on how to use the command.
    * 
    * @return Help instructions on how to use the command.
    */
   public abstract String getHelp();
   
}
