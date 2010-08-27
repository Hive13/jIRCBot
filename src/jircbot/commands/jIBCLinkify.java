package jircbot.commands;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class jIBCLinkify extends jIBCommand {

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
        ArrayList<URL> foundURLs = new ArrayList<URL>();
        while (m.find()) {
            try {
                foundURLs.add(new URL(m.group()));
                
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        
    }

}
