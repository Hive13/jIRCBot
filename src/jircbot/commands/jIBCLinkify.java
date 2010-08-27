package jircbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jircbot.support.jIRCTools;

public class jIBCLinkify extends jIBCommand {

	public final int MAX_URL_LENGTH = 25;
	
	public final boolean WAIT_FOR_TITLE = true;
	
	public final int WAIT_FOR_TITLE_TIMEOUT = 5000;
	
    @Override
    public String getCommandName() {
        return "Linkify";
    }

    @Override
    public void run() {
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
	            int sleepCount = 0;
	            // The title is not retrieved by bit.ly immediately, we can optionally
	            // move on, likely without the title, or we can repeatedly try
	            // until we get a response or timeout.
	            while((urlTitle = jIRCTools.getShortURLTitle(shortURL)).isEmpty()
	            		&& WAIT_FOR_TITLE && (sleepCount*200) < WAIT_FOR_TITLE_TIMEOUT) {
	            	try {
						Thread.sleep(200);
						if(sleepCount % 5 == 0)
							bot.log("Waiting for URL title [ " + sleepCount*200 + " ms ]");
						sleepCount += 1;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
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
