/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jircbot.commands;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jircbot.jIRCTools;

import org.jibble.pircbot.PircBot;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;

/**
 *
 * @author vincenpt
 */
public class jIBCTRssReader extends jIBCommandThread {
    public enum formatItem {
        commandName,
        title,
        link,
        author
    }
    
    public final String[] formatItems = { "[commandName]", "[Title]", "[Link]", "[Author]" };
    
    private String formatString = "[commandName] - [Title] [Link]";
    
    private URL feedURL = null;

    private List<SyndEntry> lastEntryList = null;
    
    public jIBCTRssReader(PircBot bot, String commandName, String channel, String rssFeedLink) throws MalformedURLException {
        this(bot, "[commandName] - [Title] [Link]", commandName, channel, new URL(rssFeedLink));
    }
    
    public jIBCTRssReader(PircBot bot, String formatString, String commandName, String channel, String rssFeedLink) throws MalformedURLException {
        this(bot, formatString, commandName, channel, new URL(rssFeedLink));
    }
    
    public jIBCTRssReader(PircBot bot, String formatString, String commandName, String channel, URL rssFeedLink) {
        super(bot, commandName, channel, 1000*30);
        this.formatString = formatString;
        feedURL = rssFeedLink;
        lastEntryList = new ArrayList<SyndEntry>();
    }

    protected void loop() {

        try {

            // Create a feed off of the URL and get the latest news.
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedURL));

            // Get the feed's list of entries
            @SuppressWarnings("unchecked")
            List<SyndEntry> entryList = feed.getEntries();
            Collections.sort(entryList, new SyndEntryComparator());
            List<SyndEntry> tempEntryList = new ArrayList<SyndEntry>(entryList);
            
            // Remove any entries that we have already seen.
            entryList.removeAll(lastEntryList);
            if(entryList.size() > 0) {
                // If any entries remain, send a message to the channel.
                sendMessage(formatMessage(entryList.get(0)));
            }
            
            lastEntryList = tempEntryList;
  
        } catch (MalformedURLException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            bot.log("Error: " + getCommandName() + " " + ex.toString());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            bot.log("Error: " + getCommandName() + " " + ex.toString());
        } catch (FeedException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            bot.log("Error: " + getCommandName() + " " + ex.toString());
        } catch (IOException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
            bot.log("Error: " + getCommandName() + " " + ex.toString());
        }
    }

    private String formatMessage(SyndEntry entry) {
        String message = formatString;
        message = message.replace(formatItems[0], getSimpleCommandName());
        message = message.replace(formatItems[1], entry.getTitle());
        message = message.replace(formatItems[2], "[ " + jIRCTools.generateShortURL(entry.getLink()) + " ]");
        message = message.replace(formatItems[3], entry.getAuthor());
        
        return message;
    }
    
    /**
     * 
     * This class is simply here to sort lists of SyndEntries.
     *
     */
    class SyndEntryComparator implements Comparator<SyndEntry> {
        public int compare(SyndEntry o1, SyndEntry o2) {
            int pubDateCompare = o2.getPublishedDate().compareTo(o1.getPublishedDate());
            return pubDateCompare;
        }
    }
}
