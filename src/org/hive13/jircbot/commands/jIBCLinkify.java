package org.hive13.jircbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.support.jIRCTools;
import org.hive13.jircbot.support.jIRCTools.eMsgTypes;

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
    public String getHelp() {
    	return "This command does not support this functionality.";
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
            String shortURL = "";
            if((url = m.group()).length() > MAX_URL_LENGTH) {
            	shortURL = jIRCTools.generateShortURL(url);
            } else
            	shortURL = url;
            
            urlTitle = jIRCTools.findURLTitle(url, shortURL, bot);
            returnMsg += urlTitle + " [ " + shortURL + " ]; ";
        }
        
        if(!returnMsg.isEmpty()) {
            // Remove the last ';' character
            returnMsg = returnMsg.substring(0, returnMsg.length()-2);
            bot.sendMessage(channel, returnMsg, eMsgTypes.publicMsg);
        }

    }

}
