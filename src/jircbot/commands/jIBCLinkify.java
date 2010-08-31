package jircbot.commands;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jibble.pircbot.PircBot;

import jircbot.support.jIRCTools;

public class jIBCLinkify extends jIBCommand {

	public final int MAX_URL_LENGTH = 25;
	
	public final boolean USE_BITLY_TITLE = false;
	public final boolean WAIT_FOR_TITLE = true;
	public final int WAIT_FOR_TITLE_TIMEOUT = 5000;
	
    @Override
    public String getCommandName() {
        return "Linkify";
    }

    @Override
    public void runHandleMessage(PircBot bot, String channel, String sender, String message) {
        // (http|ftp|https):\/\/[\w\-_]+(\.[\w\-_]+)+([\[\]\(\)\w\-\.,@?^=%&amp;:/~\+#]*[\[\]\(\)\w\-\@?^=%&amp;/~\+#])
        String regex = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\[\\]\\(\\)\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\[\\]\\(\\)\\w\\-\\@?^=%&amp;/~\\+#])";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(message);

        String returnMsg = "";
        while (m.find()) {
        	String url = "";
        	if((url = m.group()).length() > MAX_URL_LENGTH) {
	            String shortURL = jIRCTools.generateShortURL(url);
	            String urlTitle;
	            
	            if(USE_BITLY_TITLE) {
		            // The title is not retrieved by bit.ly immediately, we can optionally
		            // move on, likely without the title, or we can repeatedly try
		            // until we get a response or timeout.
		            Date start = new Date();
		            long duration = 0;
		            while((urlTitle = jIRCTools.getShortURLTitle(shortURL)).isEmpty()
		            		&& WAIT_FOR_TITLE && duration < WAIT_FOR_TITLE_TIMEOUT) {
		            	try {
							Thread.sleep(200);
							duration = (new Date()).getTime() - start.getTime();
							bot.log("Waiting for URL title [ " + duration + " ms ]");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
		            }
	            } else {
	            	urlTitle = jIRCTools.getURLTitle(url);
	            }
	            returnMsg += urlTitle + " [ " + shortURL + " ]; ";
        	}
        }
        
        if(!returnMsg.isEmpty()) {
            // Remove the last ';' character
            returnMsg = returnMsg.substring(0, returnMsg.length()-2);
            bot.sendMessage(channel, returnMsg);
        }

    }

}
