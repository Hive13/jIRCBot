package org.hive13.jircbot.commands;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbot.jIRCBot;
import org.hive13.jircbot.jIRCBot.eLogLevel;
import org.hive13.jircbot.support.jIRCProperties;
import org.hive13.jircbot.support.jIRCTools;
import org.hive13.jircbot.support.jIRCTools.eMsgTypes;
import org.hive13.jircbot.support.jIRCUser.eAuthLevels;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;

public class jIBCTRssReader extends jIBCommandThread {
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	private final Lock read = readWriteLock.readLock();
	private final Lock write = readWriteLock.writeLock();
	private final String[] formatItems = { "commandName", "Title", "Link",
			"Author" };

	private String formatString = "[commandName] - [Title|c50] ( [Link] )";
	private URL feedURL = null;
	private File cacheFile = null;

	private List<SyndEntry> lastEntryList_private = null;
	
	public jIBCTRssReader(jIRCBot bot, String commandName, String channel,
			String rssFeedLink) throws MalformedURLException {
		this(bot, commandName, channel,
				"[commandName] - [Title|c50] ( [Link] )", new URL(rssFeedLink),
				30 * 1000);
	}

	public jIBCTRssReader(jIRCBot bot, String commandName, String channel,
			String rssFeedLink, long refreshRate) throws MalformedURLException {
		this(bot, commandName, channel,
				"[commandName] - [Title|c50] ( [Link] )", new URL(rssFeedLink),
				refreshRate);
	}

	public jIBCTRssReader(jIRCBot bot, String commandName, String channel,
			String formatString, String rssFeedLink)
			throws MalformedURLException {
		this(bot, commandName, channel, formatString, new URL(rssFeedLink),
				30 * 1000);
	}

	public jIBCTRssReader(jIRCBot bot, String commandName, String channel,
			String formatString, String rssFeedLink, long refreshRate)
			throws MalformedURLException {
		this(bot, commandName, channel, formatString, new URL(rssFeedLink),
				refreshRate);
	}

	@SuppressWarnings("unchecked")
	public jIBCTRssReader(jIRCBot bot, String commandName, String channel,
			String formatString, URL feedURL, long refreshRate) {
		super(bot, commandName, channel, refreshRate, eAuthLevels.operator);
		this.formatString = formatString;
		this.feedURL = feedURL;

		// Attempt to read in a cached version of the feed.
		cacheFile = new File(jIRCTools.getCacheDirectory().getPath() + "/"
				+ getCommandName() + ".xml");
		if (cacheFile.exists()) {
			SyndFeedInput input = new SyndFeedInput();
			try {
				SyndFeed feed = input.build(new XmlReader(cacheFile));
				lastEntryListSet(feed.getEntries());
			} catch (IllegalArgumentException ex) {
				Logger.getLogger(jIBCTRssReader.class.getName()).log(
						Level.SEVERE, null, ex);
				bot.log("Error: " + getCommandName() + " " + ex.toString());
			} catch (FeedException ex) {
				Logger.getLogger(jIBCTRssReader.class.getName()).log(
						Level.SEVERE, null, ex);
				bot.log("Error: " + getCommandName() + " " + ex.toString());
			} catch (IOException ex) {
				Logger.getLogger(jIBCTRssReader.class.getName()).log(
						Level.INFO, null, ex);
				bot.log("Info: " + getCommandName() + " " + ex.toString());
			}
		}
		this.startCommandThread();
	}

	@Override
	public void loop() {
		
		try {
			// Here we pretend to be the google bot to fake out User-Agent
			// sniffing programs.
			URLConnection conn = feedURL.openConnection();
			conn.setRequestProperty("User-Agent", jIRCProperties.getInstance()
					.getUserAgentString());

			// Create a feed off of the URL and get the latest news.
			SyndFeedInput input = new SyndFeedInput();
			SyndFeed feed = input.build(new XmlReader(conn));

			// Get the feed's list of entries
			@SuppressWarnings("unchecked")
			List<SyndEntry> entryList = feed.getEntries();
			Collections.sort(entryList, new SyndEntryComparator());

			// Check for new entries.
			List<SyndEntry> tempEntryList = getNewEntries(entryList);

			if (tempEntryList.size() > 0) {
				// If any entries remain, send a message to the channel.
				sendMessage(formatMessage(entryList.get(0)), eMsgTypes.publicMsg);

				lastEntryListSet(entryList);

				// This means the list changed, update the saved file version.
				if (!cacheFile.exists())
					cacheFile.createNewFile();
				if (cacheFile.exists()) {
					Writer writer = new FileWriter(cacheFile, false);
					SyndFeedOutput output = new SyndFeedOutput();
					output.output(feed, writer);
					writer.close();
				}
			}
		} catch (MalformedURLException ex) {
			Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE,
					null, ex);
			bot.log(getCommandName() + " " + ex.toString(), eLogLevel.severe);
		} catch (IllegalArgumentException ex) {
			Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE,
					null, ex);
			bot.log(getCommandName() + " " + ex.toString(), eLogLevel.severe);
		} catch (FeedException ex) {
			Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE,
					null, ex);
			bot.log(getCommandName() + " " + ex.toString(), eLogLevel.severe);
		} catch (IOException ex) {
			Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE,
					null, ex);
			bot.log(getCommandName() + " " + ex.toString(), eLogLevel.severe);
		} catch (Exception ex) {
			Logger.getLogger(jIBCTRssReader.class.getName()).log(Level.SEVERE,
					null, ex);
			bot.log(getCommandName() + " " + ex.toString(), eLogLevel.severe);
		}

	}

	public String getHelp() {
		String cmdName = getSimpleCommandName();
		return "The following are valid uses of this command: !" + cmdName + " help ;" +
				" !" + cmdName + " start ; !" + cmdName + " stop";
	}
	@Override
	public void handleMessage(jIRCBot bot, String channel, String sender,
			String message) {
		if(message.equals("")) {
			bot.sendMessage(sender, getHelp());
		} else {
			// If we did not handle the message, pass it on up.
			super.handleMessage(bot, channel, sender, message);
		}
	}
	
	private List<SyndEntry> getNewEntries(List<SyndEntry> entryList) {
		/*
		 * So here is the deal. We have two lists. 1. newEntryList 2.
		 * lastEntryList
		 * 
		 * Both should be sorted by publishedDate so that the 0 item is the
		 * latest item in that list. If they were integers: 1. newEntryList -
		 * [8, 7, 6, 5, 4, 3, 2] 2. lastEntryList - [6, 5, 4, 3, 2, 1, 0]
		 * 
		 * The result should be: - [8, 7]
		 * 
		 * Go through newEntryList until item 'i' is < lastEntryList[0]. Then
		 * remove all from newEntryList until only.
		 */
		if (lastEntryListSize() <= 0) {
			return entryList;
		}
		List<SyndEntry> resultList = new ArrayList<SyndEntry>();
		SyndEntry lastSyndEntry = lastEntryListGet(0);
		Iterator<SyndEntry> i = entryList.iterator();
		while (i.hasNext()) {
			SyndEntry curEntry = i.next();
			if (curEntry.getPublishedDate().after(
					lastSyndEntry.getPublishedDate())) {
				resultList.add(curEntry);
			} else {
				return resultList;
			}
		}
		return resultList;
	}

	private String formatMessage(SyndEntry entry) {
		String message = formatString;
		message = formatMessageItem(message, formatItems[0],
				getSimpleCommandName());
		message = formatMessageItem(message, formatItems[1], entry.getTitle());
		
		if(!entry.getLink().isEmpty())
		    message = formatMessageItem(message, formatItems[2],
		            jIRCTools.generateShortURL(entry.getLink()));
		
		message = formatMessageItem(message, formatItems[3], entry.getAuthor());

		// Return the formatted message stripped of all non-ASCII characters,
		// extra spaces, and new lines.
		return message.replaceAll("([^\\p{ASCII}]|\\n|  )", "");
	}

	/*
	 * Message: [commandName] - [Title|c60] [Author|r'\(.+\)'] < [Link] >
	 * OutMsg: [commandName] - [Title|c60] (Jim) < [Link] > formatItem: [Author]
	 * --> Author formatItemRep: jo...@gmail.com (Joe Jim)
	 * 
	 * Split into parameters
	 */
	private String formatMessageItem(String message, String formatItem,
			String formatItemReplacement) {
		// Take Message, find \[formatItem[|.+]\]
		// Split on | to see if there are parameters.
		// If split.length > 1
		// loop through and check:
		// first char == c
		// Trim the result to c# characters
		// first char == r
		// run regexReplace and save only characters that match r~
		// \[commandName(\|[^\]]+)?\]
		int maxFormatLength = 512;
		String regex = "\\[" + formatItem + "(\\|[^\\]]+)?\\]";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(message);

		if (m.find()) {
			String foundItem = m.group();
			// Remove the starting [ and trailing ]
			foundItem = foundItem.substring(1, foundItem.length() - 1);

			String splitFoundItems[] = foundItem.split("\\|");
			if (splitFoundItems.length > 1) {
				// We have parameters.
				for (String par : splitFoundItems) {
					if (par.startsWith("c") && par.length() >= 2) {
						maxFormatLength = Integer.parseInt(par.substring(1));
					} else if (par.startsWith("r") && par.length() > 2) {
						regex = par.substring(1);
						p = Pattern.compile(regex);
						m = p.matcher(formatItemReplacement);
						if (m.find()) {
							formatItemReplacement = m.group();
						} else {
							bot.log("RssReader.formatMessageItem( "
									+ message
									+ ", "
									+ formatItem
									+ ", "
									+ formatItemReplacement
									+ ")"
									+ "Regex attempt failed to find anything to replace.",
									eLogLevel.info);
						}
					} else if (par.equals(formatItem)) {
						// Disregard the parameter if it
						// is the formatItem.
					} else {
						bot.log("RssReader.formatMessageItem - unknown parameter: "
								+ par, eLogLevel.warning);
					}
				}
			}
			if (formatItemReplacement.length() > maxFormatLength)
				formatItemReplacement = formatItemReplacement.substring(0,
						maxFormatLength) + "...";

			message = message.replace("[" + foundItem + "]",
					formatItemReplacement);
		} else {
			message = message.replace("[" + formatItem + "]",
					formatItemReplacement);
		}
		return message;
	}

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
			if (lastEntryList_private != null)
				result = (SyndEntry) lastEntryList_private.get(index).clone();
		} catch (CloneNotSupportedException e) {
			// Ok, this is a bit of a cheat, and is not technically threadsafe.
			// We return a pointer to the SyndEntry which could lead to errors
			// if something else causes that pointer to become invalid.
			if (lastEntryList_private != null)
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
			if (lastEntryList_private != null)
				size = lastEntryList_private.size();
		} finally {
			read.unlock();
		}
		return size;
	}

	/**
	 * 
	 * This class is simply here to sort lists of SyndEntries.
	 * 
	 */
	class SyndEntryComparator implements Comparator<SyndEntry> {
		public int compare(SyndEntry o1, SyndEntry o2) {
			int pubDateCompare = o2.getPublishedDate().compareTo(
					o1.getPublishedDate());
			return pubDateCompare;
		}
	}
}
