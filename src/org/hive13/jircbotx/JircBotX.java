package org.hive13.jircbotx;

import org.hive13.jircbotx.support.BotDatabase;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;

public class JircBotX extends PircBotX {
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

   
   /* (non-Javadoc)
    * @see org.pircbotx.PircBotX#sendMessage(org.pircbotx.User, java.lang.String)
    */
   @Override
   public void sendMessage(User target, String message) {
      super.sendMessage(target, message);
      logMessage(target.getNick(), message, eMsgTypes.privateMsg);
   }

   /* (non-Javadoc)
    * @see org.pircbotx.PircBotX#sendMessage(org.pircbotx.Channel, java.lang.String)
    */
   @Override
   public void sendMessage(Channel target, String message) {
      super.sendMessage(target, message);
      logMessage(target.getName(), message, eMsgTypes.publicMsg);
   }

   /* (non-Javadoc)
    * @see org.pircbotx.PircBotX#sendMessage(org.pircbotx.Channel, org.pircbotx.User, java.lang.String)
    */
   @Override
   public void sendMessage(Channel chan, User user, String message) {
      super.sendMessage(chan, user, message);
      logMessage(chan.getName(), user.getNick() + ": " + message, eMsgTypes.publicMsg);
   }

   /* (non-Javadoc)
    * @see org.pircbotx.PircBotX#sendMessage(java.lang.String, java.lang.String)
    */
   @Override
   public void sendMessage(String target, String message) {
      super.sendMessage(target, message);
   }
   
   public void logMessage(String target, String message, eMsgTypes msgType)
   {
      BotDatabase.insertMessage(target, this.getServer(), this.getNick(), message, msgType);
   }
   
}