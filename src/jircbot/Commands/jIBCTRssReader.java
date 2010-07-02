/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jircbot.Commands;

import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jibble.pircbot.PircBot;

/**
 *
 * @author vincenpt
 */
public class jIBCTRssReader extends jIBCommandThread {

    public jIBCTRssReader(PircBot bot, String channel, String sender,
            URL rssFeedLink) {
        super(bot, channel, sender);
    }

    public void run() {
        isRunning = true;
        while (isRunning) {
            bot.sendMessage(channel, sender + " triggered me.");
            try {
                URL feedURL = new URL("http://www.hive13.org/?feed=rss2");

                SyndFeedInput input = new SyndFeedInput();

                SyndFeed feed = input.build(new XmlReader(feedURL));



            } catch (MalformedURLException ex) {
                Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FeedException ex) {
                Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // not going to do anything
            }
        }
    }

    public String getCommandName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
