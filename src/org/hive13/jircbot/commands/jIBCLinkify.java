package org.hive13.jircbot.commands;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.jIRCTools;

public class jIBCLinkify extends jIBCommand {
    public final int        MAX_URL_LENGTH  = 25;
    public final boolean    USE_BITLY_TITLE = true;
    public final boolean    WAIT_FOR_TITLE  = true;
    public final int        WAIT_FOR_TITLE_TIMEOUT  = 5000;
    
    public jIBCLinkify() {
    	hideCommand = true;
    }
    
    @Override
    public String getCommandName() {
        return "Linkify";
    }

    @Override
    public void handleMessage(jIRCBot bot, String channel, String sender,
            String message) {
        // (http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\[\]\(\)\w\-\.,@?^=%&amp;:/~\+#]*[\[\]\(\)\w\-\@?^=%&amp;/~\+#])
        String regex = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\[\\]\\(\\)\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\[\\]\\(\\)\\w\\-\\@?^=%&amp;/~\\+#])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);

        String returnMsg = "";
        while (m.find()) {
            String url = "";
            String urlTitle = "";
            if((url = m.group()).length() > MAX_URL_LENGTH) {
                url = jIRCTools.generateShortURL(url);
                urlTitle = jIRCTools.getShortURLTitle(url);
            }
            
            if(urlTitle.isEmpty()) {
                bot.log("jIBCLinkify - initial bit.ly failed, trying jIRCTools.getURLTitle", eLogLevel.info);
                urlTitle = jIRCTools.getURLTitle(url);
            }
            
            if(urlTitle.isEmpty() && USE_BITLY_TITLE) { // Are we forcing ourselves to only use Bit.ly?
                bot.log("jIBCLinkify - jIRCTools.getURLTitle failed, waiting for bit.ly to cache title.", eLogLevel.info);
                // The title is not retrieved by bit.ly immediately, we can optionally
                // move on, likely without the title, or we can repeatedly try
                // until we get a response or timeout.
                Date start = new Date();
                long duration = 0;
                while((urlTitle = jIRCTools.getShortURLTitle(url)).isEmpty()
                        && WAIT_FOR_TITLE && duration < WAIT_FOR_TITLE_TIMEOUT) {
                    try {
                        Thread.sleep(200);
                        duration = (new Date()).getTime() - start.getTime();
                        bot.log("Waiting for URL title [ " + duration + " ms ]");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // Remove ASCII characters, new lines, and excessive spaces.
            urlTitle = urlTitle.replaceAll("([^\\p{ASCII}]|\\n|  )", "");
            returnMsg += urlTitle + " [ " + url + " ]; ";
        }
        
        if(!returnMsg.isEmpty()) {
            // Remove the last ';' character
            returnMsg = returnMsg.substring(0, returnMsg.length()-2);
            bot.sendMessage(channel, returnMsg);
        }

    }

}
