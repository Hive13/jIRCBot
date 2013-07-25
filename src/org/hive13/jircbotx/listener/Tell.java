package org.hive13.jircbotx.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerAdapterX;
import org.hive13.jircbotx.support.BotDataCache;
import org.pircbotx.hooks.events.MessageEvent;

public class Tell extends ListenerAdapterX {
   private static String sControlSubstitutes[][] = {{";", "[!semi!]"}, {"|", "[!pipe!]"}};

   @Override
   public String getCommandName() {
      return "tell";
   }

   @Override
   public String getHelp() {
      return "This command stores a message for a user.  The next time" +
            " that user sends a message in the chat room I will send" +
            " them the messages stored for them. Ex. !Tell" +
            " jimboJones Remember to bring that thing with you.";
   }

   private final int MAX_MSG_QUEUE = 10;
   private HashMap<String, ArrayList<storedMsg>> msgMap = new HashMap<String, ArrayList<storedMsg>>();
   
   public Tell() {
      super();
      loadMessages();
   }

   public void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      String message = event.getMessage();
      String sender = event.getUser().getNick();
      
      String[] splitMsg = message.split(" ", 3);
      if (splitMsg[0].toLowerCase().equals("!tell")) {
         if (splitMsg.length == 3) {
            // Add the tell message
            // - [command] [target user] [message]
            ArrayList<storedMsg> msglist;
            if ((msglist = msgMap.get(splitMsg[1].toLowerCase())) == null) {
               msglist = new ArrayList<storedMsg>();
               msgMap.put(splitMsg[1].toLowerCase(), msglist);
            }
            if (msglist.size() <= MAX_MSG_QUEUE) {
               msglist.add(new storedMsg(new Date(), splitMsg[2], sender, splitMsg[1]));

               // Tell the sender that the 'tell' was added for 'target
               // user'
               event.getUser().sendMessage("I will tell " + splitMsg[1]
                     + " '" + splitMsg[2] + "'" 
                     + " the next time they talk in channel.");
               
               flushMessages();
            } else {
               event.getUser().sendMessage(splitMsg[1]
                     + " already has the max (" + MAX_MSG_QUEUE
                     + ") number of messages saved for them.");
            }
         } else {
            event.getUser().sendMessage("The correct syntax is: !tell username Remember the milk");
         }
      }
      
      // Now we need to check to see if sender has any waiting messages.
      ArrayList<storedMsg> msgList;
      if ((msgList = msgMap.remove(sender.toLowerCase())) != null) {
         Iterator<storedMsg> i = msgList.iterator();
         while (i.hasNext()) {
            storedMsg curMsg = i.next();
            event.getUser().sendMessage( curMsg.sender + " sent the following to you on "
                                          + curMsg.storedDate.toString() + " : "
                                          + curMsg.message);
         }
         flushMessages();
      }
   }
   
   private void loadMessages()
   {
      String[] splitMsgs = BotDataCache.getInstance().getSavedTells(getCommandName()).split("\\|");
      for(int i = 0; i < splitMsgs.length; ++i)
      {
         storedMsg msg = getStoredMsgFromString(splitMsgs[i]);
         if(msg != null)
         {
            ArrayList<storedMsg> msglist;
            if ((msglist = msgMap.get(msg.dest.toLowerCase())) == null) {
               msglist = new ArrayList<storedMsg>();
               msgMap.put(msg.dest.toLowerCase(), msglist);
            }
            msglist.add(msg);
         }
      }
   }
   
   private void flushMessages()
   {
      String outString = "";
      Iterator<ArrayList<storedMsg>> itMsgs = msgMap.values().iterator();
      while(itMsgs.hasNext())
      {
         Iterator<storedMsg> itMsg = itMsgs.next().iterator();
         while(itMsg.hasNext())
         {
            outString += getStoredMsgString(itMsg.next()) + "|";
         }
      }
      if(outString.length() > 1)
         outString = outString.substring(0, outString.length()-1);
      BotDataCache.getInstance().setSavedTells(getCommandName(), outString);
   }
   
   private class storedMsg {
      Date storedDate;
      String message;
      String sender;
      String dest;

      public storedMsg(Date storedDate, String message, String sender, String dest) {
         super();
         this.storedDate = storedDate;
         this.message = message;
         this.sender = sender;
         this.dest = dest;
      }
   }

   
   private storedMsg getStoredMsgFromString(String savedMsg)
   {
      storedMsg msg = null;
      Date date = null;
      String message = null;
      String sender = null;
      String dest = null;
      
      String[] splitMsg = savedMsg.split(";");
      if(splitMsg.length == 4)
      {
         date = new Date(Long.parseLong(splitMsg[0]));
         message = replaceSubWithCon(splitMsg[1]);
         sender = replaceSubWithCon(splitMsg[2]);
         dest = replaceSubWithCon(splitMsg[3]);
         msg = new storedMsg(date, message, sender, dest);
      }
      return msg;
   }
   
   private String getStoredMsgString(storedMsg msg)
   {
      String outString = "";
      outString += Long.toString(msg.storedDate.getTime()) + ";";
      outString += replaceConWithSub(msg.message) + ";";
      outString += replaceConWithSub(msg.sender) + ";";
      outString += replaceConWithSub(msg.dest);
      return outString;
   }
   
   private String replaceConWithSub(String input)
   {
      String result = input;
      for(int i = 0; i < sControlSubstitutes.length; i++)
      {
         result = result.replace(sControlSubstitutes[i][0], sControlSubstitutes[i][1]);
      }
      return result;
   }
   
   private String replaceSubWithCon(String input)
   {
      String result = input;
      for(int i = 0; i < sControlSubstitutes.length; i++)
      {
         result = result.replace(sControlSubstitutes[i][1], sControlSubstitutes[i][0]);
      }
      return result;
   }
   
   
}
