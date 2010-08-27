package jircbot.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jircbot.support.jIRCTools;

public class jIBCLinkify extends jIBCommand {

	public final int MAX_URL_LENGTH = 25;
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
	            String urlTitle = jIRCTools.getShortURLTitle(shortURL);
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
