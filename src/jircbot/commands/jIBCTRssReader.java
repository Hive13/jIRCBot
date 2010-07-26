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
   
    private URL feedURL = null;

    private List<SyndEntry> lastEntryList = null;

    public jIBCTRssReader(PircBot bot, String channel, String rssFeedLink) throws MalformedURLException {
        this(bot, channel, new URL(rssFeedLink));
    }

    public jIBCTRssReader(PircBot bot, String channel, URL rssFeedLink) {
    	this(bot, "RssReader", channel, rssFeedLink);
    }
    
    public jIBCTRssReader(PircBot bot, String commandName, String channel, String rssFeedLink) throws MalformedURLException{
    	this(bot, commandName, channel, new URL(rssFeedLink));
    }
    
    public jIBCTRssReader(PircBot bot, String commandName, String channel, URL rssFeedLink) {
        super(bot, commandName, channel, 1000*30);
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
                sendMessage(this.getSimpleCommandName() + " - " + entryList.get(0).getTitle() + " [ " + entryList.get(0).getLink() + " ]");
            }
            
            lastEntryList = tempEntryList;
  
        } catch (MalformedURLException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FeedException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
        }
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
