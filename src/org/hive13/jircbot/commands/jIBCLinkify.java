package org.hive13.jircbot.commands;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.WebFile;
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
            if((url = m.group()).length() > MAX_URL_LENGTH) {
                url = jIRCTools.generateShortURL(url);
                urlTitle = jIRCTools.getShortURLTitle(url);
            }
            
            if(urlTitle.isEmpty()) {
                bot.log("jIBCLinkify - initial bit.ly failed, trying jIRCTools.getURLTitle", eLogLevel.info);
                try {
                    WebFile website = new WebFile(url);
                    urlTitle = jIRCTools.getURLTitle(website);
                    Object content = website.getContent();
                    if(content instanceof Image) {
                        // We want to resize this image.
                        Image img = (Image)content;
                        
                        // Then write this image to a directory.
                        // Then write a 'formatedMsg' to the log.
                    }
                } catch (MalformedURLException e) {
                    bot.log("jIBCLinkify - WTF! Bit.ly gave us an invalid URL...", eLogLevel.error);
                    e.printStackTrace();
                } catch (IOException e) {
                    bot.log("jIBCLinkify - failed (badly) to getURLTitle()", eLogLevel.error);
                    e.printStackTrace();
                }
            }
            
            if(urlTitle.isEmpty() && USE_BITLY_TITLE) { // Are we allowing ourselves to fall back to Bit.ly?
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
            bot.sendMessage(channel, returnMsg, eMsgTypes.publicMsg);
        }

    }

}
