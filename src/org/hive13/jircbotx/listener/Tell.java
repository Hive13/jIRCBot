package org.hive13.jircbotx.listener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.ListenerAdapterX;
import org.pircbotx.hooks.events.MessageEvent;

public class Tell extends ListenerAdapterX {

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
   
   private class storedMsg {
      Date storedDate;
      String message;
      String sender;

      public storedMsg(Date storedDate, String message, String sender) {
         super();
         this.storedDate = storedDate;
         this.message = message;
         this.sender = sender;
      }
   }
   
   private final int MAX_MSG_QUEUE = 10;
   private HashMap<String, ArrayList<storedMsg>> msgMap = new HashMap<String, ArrayList<storedMsg>>();
   
   public void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      String message = event.getMessage();
      String sender = event.getUser().getNick();
      
      String[] splitMsg = message.split(" ", 3);
      if (splitMsg[0].toLowerCase().equals("!tell")) {
         if (splitMsg.length == 3) {
            // Add the tell message
            // - [command] [target user] [message]
            ArrayList<storedMsg> msglist;
            if ((msglist = msgMap.get(splitMsg[1])) == null) {
               msglist = new ArrayList<storedMsg>();
               msgMap.put(splitMsg[1].toLowerCase(), msglist);
            }
            if (msglist.size() <= MAX_MSG_QUEUE) {
               msglist.add(new storedMsg(new Date(), splitMsg[2], sender));

               // Tell the sender that the 'tell' was added for 'target
               // user'
               event.getUser().sendMessage("I will tell " + splitMsg[1]
                     + " '" + splitMsg[2] + "'" 
                     + " the next time they talk in channel.");
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
      }
   }
}
