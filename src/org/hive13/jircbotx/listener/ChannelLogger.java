package org.hive13.jircbotx.listener;

import java.util.Iterator;

import org.hive13.jircbotx.JircBotX.eMsgTypes;
import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerAdapterX;
import org.hive13.jircbotx.support.BotDatabase;
import org.pircbotx.Channel;
import org.pircbotx.User;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NickChangeEvent;
import org.pircbotx.hooks.events.PartEvent;
import org.pircbotx.hooks.events.QuitEvent;

public class ChannelLogger extends ListenerAdapterX {

   public ChannelLogger()
   {
      bHideCommand = false;
   }
   
   @Override
   public String getCommandName() {
      return "logger";
   }

   @Override
   public String getHelp() {
      return "";
   }
   
   @Override
   public void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      logEvent(event.getChannel(), event.getBot().getServer(), event.getUser(), event.getMessage(), eMsgTypes.publicMsg);
   }
   
   public void onAction(ActionEvent<JircBotX> event) {
      logEvent(event.getChannel(), event.getBot().getServer(), event.getUser(), event.getMessage(), eMsgTypes.actionMsg);
   }
   
   public void onNickChange(NickChangeEvent<JircBotX> event) {
      // This is a User changing their Nick.
      // This could be US changing our Nick, but who cares if we change our
      // nick?
      logEvent(null, event.getBot().getServer(), event.getUser(), event.getOldNick(), event.getNewNick(), eMsgTypes.actionMsg);
   }
   
   public void onJoin(JoinEvent<JircBotX> event) {
      logEvent(event.getChannel(), event.getBot().getServer(), event.getUser(), "", eMsgTypes.joinMsg);
   }
   public void onPart(PartEvent<JircBotX> event) {
      // TODO: Make sure the following statements are still true, at its worse, we will just be doing an unnecessary check...
      // This function is called when a user leaves a channel we are in.
      // This function is also called when WE leave a channel.
      if(!event.getUser().getNick().equals(event.getBot().getNick()))
      {
         logEvent(event.getChannel(), event.getBot().getServer(), event.getUser(), "", eMsgTypes.partMsg);
      }
   }

   public void onQuit(QuitEvent<JircBotX> event) {
      // Send event for all channel's 'User' is in, therefore pass a 'null' channel.
      logEvent(null, event.getBot().getServer(), event.getUser(), "", eMsgTypes.quitMsg);
   }
   
   private void logEvent(Channel EventChannel, String server, 
         User EventUser, String message, eMsgTypes msgType) {
      logEvent(EventChannel, server, EventUser, EventUser.getNick(), message, msgType);
   }
   
   // Use when you want to fake out the saved user
   private void logEvent(Channel EventChannel, String server, 
         User EventUser, String username, String message, eMsgTypes msgType) {
   
      // TODO: Add back in message obfuscation?  Leaving it out for simplicity sake for now.
      
      if(EventChannel == null)
      {
         Iterator<Channel> it = EventUser.getChannels().iterator();
         while(it.hasNext())
         {
            BotDatabase.insertMessage(it.next().getName(), server, username, message, msgType);
         }
      }
      else
         BotDatabase.insertMessage(EventChannel.getName(), server, username, message, msgType);
   }
}
