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

   /**
    * Various log levels for when the bot is writing to the log. This is
    * primarily used for formatting the log message.
    * 
    * @author vincentp
    * 
    */
   public enum eLogLevel {
      /** Just an alert message giving a bot status update. */
      info,
      /**
       * Either a minor error or slightly unexpected event has occurred. this
       * is not really serious and the bot will continue normal operations
       */
      warning,
      /**
       * A more serious event has occurred. The bot will probably recover from
       * this error and continue normal operations, however it is not
       * guaranteed.
       */
      error,
      /**
       * A major error has occurred that will effect bot actions and
       * operations. This should never occur in the course of bot operation
       * unless it has been incorrectly configured or compiled. The bot can
       * not continue normal operations and will probably act very strangely
       * until it is restarted. This error is thrown when a key process is
       * interrupted or throws an error.
       */
      severe
   }
   
   /**
    * This function uses the logLevel to determine how to treat each message
    * that is passed to it.
    * 
    * @param line
    *            Line to write to the log.
    * @param logLevel
    *            The level of importance to give the message.
    */
   public void log(String line, eLogLevel logLevel) {
      switch (logLevel) {
      case info:
         line = "<Info> " + line;
         break;
      case warning:
         line = "<Warning> " + line;
         break;
      case error:
         line = "<Error> " + line;
         break;
      case severe:
         line = "<Severe Error> " + line;
         break;
      }
      
      super.log(line);
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