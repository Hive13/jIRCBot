package org.hive13.jircbotx.listener;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbotx.ListenerAdapterX;
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
      }
      
      if(!returnMsg.isEmpty()) {
          // Remove the last ';' character
          returnMsg = returnMsg.substring(0, returnMsg.length()-2);
          event.getBot().sendMessage(event.getChannel(), returnMsg);
      }
      
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
