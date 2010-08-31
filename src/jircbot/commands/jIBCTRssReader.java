/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jircbot.commands;
//TODO: Document this class.
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import jircbot.support.jIRCTools;

import org.jibble.pircbot.PircBot;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

/**
 *
 * @author vincenpt
 */
public class jIBCTRssReader extends jIBCommandThread {
    /* This is not used * /
     public enum formatItem {
        commandName,
        title,
        link,
        author
    }//*/
    
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock read = readWriteLock.readLock();
	private final Lock write = readWriteLock.writeLock();
	
    public final String[] formatItems = { "[commandName]", "[Title]", "[Link]", "[Author]" };
    
    private String formatString = "[commandName] - [Title] [Link]";
    
    private URL feedURL = null;

    private List<SyndEntry> lastEntryList_private = null;
    
    private File cacheFile = null;
    
    private void lastEntryListSet(List<SyndEntry> lastEntryList) {
    	write.lock();
    	try {
    		this.lastEntryList_private = lastEntryList;
    	} finally {
    		write.unlock();
    	}
    }
    private SyndEntry lastEntryListGet(int index) {
    	SyndEntry result = null;
    	read.lock();
    	try {
    		if(lastEntryList_private != null)
    			result = (SyndEntry) lastEntryList_private.get(index).clone();
    	} catch (CloneNotSupportedException e) {
    		// Ok, this is a bit of a cheat, and is not technically threadsafe.
    		// We return a pointer to the SyndEntry which could lead to errors
    		// if something else causes that pointer to become invalid.
			if(lastEntryList_private != null)
				result = lastEntryList_private.get(index);
		} finally {
    		read.unlock();
    	}
    	return result;
    }
    private int lastEntryListSize() {
    	int size = -1;
    	read.lock();
    	try {
    		if(lastEntryList_private != null)
    			size = lastEntryList_private.size();
    	} finally {
    		read.unlock();
    	}
    	return size;
    }
    
    public jIBCTRssReader(PircBot bot, String commandName, String channel, String rssFeedLink) throws MalformedURLException {
        this(bot, "[commandName] - [Title] [Link]", commandName, channel, new URL(rssFeedLink));
    }
    
    public jIBCTRssReader(PircBot bot, String formatString, String commandName, String channel, String rssFeedLink) throws MalformedURLException {
        this(bot, formatString, commandName, channel, new URL(rssFeedLink));
    }
    
    @SuppressWarnings("unchecked")
	public jIBCTRssReader(PircBot bot, String formatString, String commandName, String channel, URL rssFeedLink) {
        super(bot, commandName, channel, 1000*30);
        this.formatString = formatString;
        feedURL = rssFeedLink;
        lastEntryList_private = new ArrayList<SyndEntry>();
        
        // Attempt to read in a cached version of the feed.
        cacheFile = new File(getCommandName() + ".xml");
        if(cacheFile.exists()) {
	        SyndFeedInput input = new SyndFeedInput();
	        try {
				SyndFeed feed = input.build(new XmlReader(cacheFile));
				lastEntryListSet(feed.getEntries());
			} catch (IllegalArgumentException ex) {
	            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
	            bot.log("Error: " + getCommandName() + " " + ex.toString());
			} catch (FeedException ex) {
	            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE, null, ex);
	            bot.log("Error: " + getCommandName() + " " + ex.toString());
			} catch (IOException ex) {
	            Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.INFO, null, ex);
	            bot.log("Info: " + getCommandName() + " " + ex.toString());
			}
        }
        
    }

    protected void loop() {

        try {

            /* Google Groups RSS Feed was giving me 403, here is why:
             * Hi Dinesh,
                You can set User-Agent of your http request to any of the bot�s user-agent 
                so that Google treat it as a bot. To change the user agent of request use 
                XmlReader(java.net.URLConnection conn) constructor of XmlReader class. 
                Pass the conn object which has the user agent set to proper value.
                conn.setRequestProperty(�User-Agent�,�whateveryouwant�);
                
                Also, check this out:
                http://www.java2s.com/Code/Java/Network-Protocol/UsingURLConnection.htm
                
                And here... a list of user-age strings:
                http://www.useragentstring.com/pages/useragentstring.php
                
                Firefox 3.6.8:
                Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.8) Gecko/20100722 Firefox/3.6.8
             */
            // Here we pretend to be the google bot to fake out User-Agent sniffing programs.
            URLConnection conn = feedURL.openConnection();
            conn.setRequestProperty("User-Agent", jIRCTools.UserAgentString);
            
            // Create a feed off of the URL and get the latest news.
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(conn));
            
            // Get the feed's list of entries
            @SuppressWarnings("unchecked")
            List<SyndEntry> entryList = feed.getEntries();
            Collections.sort(entryList, new SyndEntryComparator());
            
            // Check for new entries.
            List<SyndEntry> tempEntryList = getNewEntries(entryList);
            
            if(tempEntryList.size() > 0) {
                // If any entries remain, send a message to the channel.
                sendMessage(formatMessage(entryList.get(0)));

                lastEntryListSet(entryList);
                
                // This means the list changed, update the saved file version.
                Writer writer = new FileWriter(cacheFile, false);
                SyndFeedOutput output = new SyndFeedOutput();
                output.output(feed, writer);
                writer.close();
            }
            
  
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

    private List<SyndEntry> getNewEntries(List<SyndEntry> newEntryList) {
        /*
         * So here is the deal. We have two lists.
         * 1. newEntryList
         * 2. lastEntryList
         * 
         * Both should be sorted by publishedDate so that the 0
         * item is the latest item in that list. If they were integers:
         * 1. newEntryList
         * - [8, 7, 6, 5, 4, 3, 2]
         * 2. lastEntryList
         * - [6, 5, 4, 3, 2, 1, 0]
         * 
         * The result should be:
         * - [8, 7]
         * 
         * Go through newEntryList until item 'i' is < lastEntryList[0].
         * Then remove all from newEntryList until only.
         */
        if(lastEntryListSize() <= 0) {
            return newEntryList;
        }
        List<SyndEntry> resultList = new ArrayList<SyndEntry>();
        SyndEntry lastSyndEntry = lastEntryListGet(0);
        Iterator<SyndEntry> i = newEntryList.iterator();
        while(i.hasNext()) {
            SyndEntry curEntry = i.next();
            if(curEntry.getPublishedDate().after(lastSyndEntry.getPublishedDate())) {
                resultList.add(curEntry);
            } else {
                return resultList;
            }
        }
        return resultList;
    }
    
    private String formatMessage(SyndEntry entry) {
        String message = formatString;
        message = message.replace(formatItems[0], getSimpleCommandName());
        message = message.replace(formatItems[1], entry.getTitle());
        message = message.replace(formatItems[2], "[ " + jIRCTools.generateShortURL(entry.getLink()) + " ]");
        message = message.replace(formatItems[3], entry.getAuthor());
        
        return message.replace("\n", "");
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

	@Override
	public void runHandleMessage(PircBot bot, String channel, String sender, String message) {
		String[] splitMsg = message.split(" ", 2);
		if(splitMsg.length > 1) {
			if(splitMsg[1].equalsIgnoreCase("stop")) {
				// Stop this command from running.
				if(this.getIsRunning())
					this.stop();
			} else if(splitMsg[1].equalsIgnoreCase("start")) {
				/* Start this command:
	             * This is more of a "resume" than a start.
	             * We still have all of the old information
	             * cached here, we are just kicking off a new thread.
	             */
				if(!this.getIsRunning())
					new Thread(this).start();
			} else {
				// In all other cases just re-output the last entry.
				// !!!! WARNING !!!! THIS IS VERY BAD.
				//				     lastEntryList IS NOT THREAD SAFE
				// 	- this function can be called while loop() is in being
				//	  executed.  This means that lastEntryList will be accessed
				//	  by two different threads at the same time.
				if(lastEntryListSize() >= 1) {
					sendMessage(formatMessage(lastEntryListGet(0)));
				}
			}
		}
	}
}
