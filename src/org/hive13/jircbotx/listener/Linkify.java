package org.hive13.jircbotx.listener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbotx.ListenerAdapterX;
import org.hive13.jircbotx.support.BotDatabase;
import org.hive13.jircbotx.support.MessageRow;
import org.hive13.jircbotx.support.UrlTools;
import org.hive13.jircbotx.JircBotX;
import org.pircbotx.hooks.events.MessageEvent;

public class Linkify extends ListenerAdapterX {
   public final int        MAX_URL_LENGTH  = 25;
   public final boolean    USE_BITLY_TITLE = true;
   public final boolean    WAIT_FOR_TITLE  = true;
   public final int        WAIT_FOR_TITLE_TIMEOUT  = 5000;
   
   public Linkify()
   {
      bHideCommand = true;
   }
   
   public void handleMessage(MessageEvent<JircBotX> event) throws Exception {
      String message = event.getMessage();
      String regex = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\[\\]\\(\\)\\w\\-\\.,@?^=%&amp;:/~\\+#!]*[\\[\\]\\(\\)\\w\\-\\@?^=%&amp;/~\\+#!])";
      Pattern p = Pattern.compile(regex);
      Matcher m = p.matcher(message);
      
      String returnMsg = "";
      ArrayList<String> previousMsgs = new ArrayList<String>();
      
      while (m.find()) {
          String url = "";
          String urlTitle = "";
          String shortURL = "";
          if((url = m.group()).length() > MAX_URL_LENGTH) {
             shortURL = UrlTools.generateShortURL(url);
          } else
             shortURL = url;
          
          urlTitle = UrlTools.findURLTitle(url, shortURL, event.getBot());
          returnMsg += urlTitle + " [ " + shortURL + " ]; ";
          
          // Find previous messages
          previousMsgs.add(checkForPastURL(url, new Date(event.getTimestamp())));
      }
      
      if(!returnMsg.isEmpty()) {
          // Remove the last ';' character
          returnMsg = returnMsg.substring(0, returnMsg.length()-2);
          event.getChannel().sendMessage(returnMsg);
      }
      
      Iterator<String> itMsgs = previousMsgs.iterator();
      while(itMsgs.hasNext())
      {
         event.getChannel().sendMessage(itMsgs.next());
      }
      
   }

   public String checkForPastURL(String fullURL, Date eventDate)
   {
      String result = "";
      if(BotDatabase.jdbcEnabled)
      {
         ArrayList<MessageRow> foundMessages = BotDatabase.searchMessagesForString(fullURL, eventDate);
         Iterator<MessageRow> itMsgs = foundMessages.iterator();
         while(itMsgs.hasNext())
         {
            result += "[" + getStringForMessageRow(itMsgs.next()) + "]";
         }
      }
      if(!result.isEmpty())
         result = "URL also sent by " + result;
      
      return result;
   }
   
   public String getStringForMessageRow(MessageRow msgRow)
   {
      String result = "";
      if(msgRow != null)
      {
         String dateURL = "http://portal.hive13.org/irclogger/index.php?d=";
         dateURL += new SimpleDateFormat("yyyy-MM-dd").format(msgRow.tsMsgTime);
         dateURL += "#msg" + msgRow.pk_MessageID;
         
         result = msgRow.vcUsername + " on " + UrlTools.generateShortURL(dateURL);
      }
      return result;
   }
   
   @Override
   public String getCommandName() {
      return "linkify";
   }

   @Override
   public String getHelp() {
      return "";
   }
}
