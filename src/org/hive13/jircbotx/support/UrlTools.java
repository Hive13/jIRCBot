package org.hive13.jircbotx.support;

import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.info;
import static com.rosaloves.bitlyj.Bitly.shorten;

import java.awt.Image;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hive13.jircbotx.JircBotX;
import org.hive13.jircbotx.JircBotX.eLogLevel;
import org.hive13.jircbotx.support.WebFile.eUserAgent;

public class UrlTools {
   /**
    * Bit.ly API integration to generate a shortened URL. Make sure that the
    * bitlyName and bitlyAPIKey are specified before calling this method.
    * 
    * @param longURL
    *            Long URL to shorten.
    * @return Returns a bit.ly shortened URL
    */
   public static String generateShortURL(String longURL) {
      return generateShortURL(longURL, BotProperties.getInstance()
            .getBitlyName(), BotProperties.getInstance().getBitlyAPIKey());
   }

   /**
    * Uses the Bit.ly API to generate a short URL.
    * 
    * @param longURL
    *            The log URL to shorten.
    * @param username
    *            The Bit.ly username to use.
    * @param apikey
    *            Bit.ly APIKey associated w/ the username.
    * @return Returns a shortened bit.ly URL.
    */
   public static String generateShortURL(String longURL, String username,
         String apikey) {
      if(longURL.isEmpty())
         return "";

      String result = "Username or API key are not initialized";
      if (BotProperties.getInstance().getBitlyName().length() > 0
            && BotProperties.getInstance().getBitlyAPIKey().length() > 0)
         result = as(username, apikey).call(shorten(longURL)).getShortUrl();
      return result;
   }

   /**
    * This method aggressively attempts to find some string description of
    * the passed in URL. This function has multiple stages as it tries 
    * various methods for finding the passed in URL's title.
    * 1. See if bit.ly has cached a title for the page.
    * 2. Determine if the page is text or binary.
    * 2.1. If it is binary, determine the MIME type and return that.
    * 2.2. If it is text, download a copy of the page's text and parse it for <title> tags.
    * 3. Did we fail to find the title via #2? Try to wait for bit.ly to cache the page title. (5 seconds, then time out)
    *
    * @param url The http URL address to find a string description for.
    * @return    A string description of the passed in URL.
    */
   public static String findURLTitle(String url) {
      return findURLTitle(url, generateShortURL(url), null);
   }

   /**
    * This method aggressively attempts to find some string description of
    * the passed in URL. This function has multiple stages as it tries 
    * various methods for finding the passed in URL's title.
    * 1. See if bit.ly has cached a title for the page.
    * 2. Determine if the page is text or binary.
    * 2.1. If it is binary, determine the MIME type and return that.
    * 2.2. If it is text, download a copy of the page's text and parse it for <title> tags.
    * 3. Did we fail to find the title via #2? Try to wait for bit.ly to cache the page title. (5 seconds, then time out)
    *
    * @param url    The http URL address to find a string description for.
    * @param shortURL  The bit.ly short version of the URL passed in.
    * @param bot An optional parameter for status updates for the log.
    * @return    A string description of the passed in URL.
    */
   public static String findURLTitle(String url, String shortURL, JircBotX bot) {
      final boolean USE_BITLY_TITLE = true;     // Should we give Bit.ly a 2nd chance?
      final boolean WAIT_FOR_TITLE = true;      // Given a 2nd chance, should we keep giving it chances until Timeout?
      final int WAIT_FOR_TITLE_TIMEOUT = 5000;  // Given multiple chances, how long before we give up (in ms)

      String urlTitle = "";
      boolean withBot = (bot != null);

      urlTitle = getShortURLTitle(shortURL);
      if(urlTitle.isEmpty()) {
         if(withBot) bot.log("findURLTitle - initial bit.ly failed, trying jIRCTools.getURLTitle", eLogLevel.info);
         try {
            WebFile website = new WebFile(url);
            urlTitle = getURLTitle(website);
            Object content = website.getContent();
            if(content instanceof Image) {
               // We want to resize this image.
               // Image img = (Image)content;

               // Then write this image to a directory.
               // Then write a 'formatedMsg' to the log.
            }
         } catch (MalformedURLException e) {
            if(withBot) bot.log("findURLTitle - WTF! Bit.ly gave us an invalid URL...", eLogLevel.error);
            e.printStackTrace();
         } catch (IOException e) {
            if(withBot) bot.log("findURLTitle - failed (badly) to getURLTitle()", eLogLevel.error);
            e.printStackTrace();
         }
      }

      if(urlTitle.isEmpty() && USE_BITLY_TITLE) { // Are we allowing ourselves to fall back to Bit.ly?
         if(withBot) bot.log("findURLTitle - jIRCTools.getURLTitle failed, waiting for bit.ly to cache title.", eLogLevel.info);
         // The title is not retrieved by bit.ly immediately, we can optionally
         // move on, likely without the title, or we can repeatedly try
         // until we get a response or timeout.
         Date start = new Date();
         long duration = 0;
         while((urlTitle = getShortURLTitle(shortURL)).isEmpty()
               && WAIT_FOR_TITLE && duration < WAIT_FOR_TITLE_TIMEOUT) {
            try {
               Thread.sleep(200);
               duration = (new Date()).getTime() - start.getTime();
               if(withBot) bot.log("Waiting for URL title [ " + duration + " ms ]");
            } catch (InterruptedException e) {
               e.printStackTrace();
            }
         }
      }
      if(urlTitle.isEmpty()) { // Is the title STILL empty?  Lets fall back to the TLD
         if(withBot) bot.log("findURLTitle - Second bit.ly failed, let's just get the Domain...");
         urlTitle = getURLDomain(url);

         if(urlTitle.isEmpty() && withBot) {
            bot.log("findURLTitle - getURLDomain returned a blank result.");
         }//*/
      }
      // Remove ASCII characters, new lines, and excessive spaces.
      urlTitle = urlTitle.replaceAll("[^\\p{ASCII}]", "");
      urlTitle = urlTitle.replaceAll("\\n", "");
      urlTitle = urlTitle.replaceAll("  ", "");
      return urlTitle;
   }


   /**
    * Calls on the Bit.ly API to find the title of an already shortened URL.
    * Make sure that the bitlyName and bitlyAPIKey are specified before calling
    * this method.
    * 
    * @param shortURL
    *            An already shortened Bit.ly URL.
    * @return Returns the title of the page the Bit.ly URL links too.
    */
   public static String getShortURLTitle(String shortURL) {
      return getShortURLTitle(shortURL, BotProperties.getInstance()
            .getBitlyName(), BotProperties.getInstance().getBitlyAPIKey());
   }

   /**
    * Calls on the Bit.ly API to find the title of an already shortened URL.
    * 
    * @param shortURL
    *            An already shortened Bit.ly URL.
    * @param username
    *            The Bit.ly username to use.
    * @param apikey
    *            Bit.ly APIKey associated w/ the username.
    * @return Returns the title of the page the Bit.ly URL links too.
    */
   public static String getShortURLTitle(String shortURL, String username,
         String apikey) {
      String result = "Username or API key are not initialized";
      if (BotProperties.getInstance().getBitlyName().length() > 0
            && BotProperties.getInstance().getBitlyAPIKey().length() > 0)
         result = as(username, apikey).call(info(shortURL)).getTitle();
      return result;
   }

   /**
    * When passed the URL for a webpage this function attempts to determine the
    * title of the webpage if it is an HTML page, if it is not an HTML page it
    * determines the MIME datatype and returns that.
    * 
    * @return Returns the title of the webpage.
    */
   public static String getURLTitle(WebFile website) {
      String result = "";
      String type = website.getMIMEType();

      String rgxIsHTML = "(text/x?html|application/xhtml+xml)";
      Pattern p = Pattern.compile(rgxIsHTML);
      Matcher m = p.matcher(type);
      if (!m.find()) {
         result = type; // It is not a webpage.
      } else {
         Object content = website.getContent();
         if (content instanceof String) {
            String sContent = ((String) content).replaceAll("[\\n\\r]", "");
            // Case insensitive search for the <title> tags.
            String rgxFindTitle = "<[tT][iI][tT][lL][eE][^>]*>(.*?)</[tT][iI][tT][lL][eE]>";
            p = Pattern.compile(rgxFindTitle);
            m = p.matcher(sContent);
            if (m.find()) {
               result = m.group();
               result = result.substring(result.indexOf('>') + 1,
                     result.lastIndexOf('<')).trim();
            }
         }

      }
      return result;
   }

   public static Object getUrlContent(String url) throws MalformedURLException, IOException
   {
      return getUrlContent(url, "", "", eUserAgent.fake);
   }

   public static Object getUrlContent(String url, String username, String password, eUserAgent fakeUserAgent) throws MalformedURLException, IOException {
      WebFile website = new WebFile(url, username, password, fakeUserAgent);
      return website.getContent();
   }

   public static String getURLDomain(String sURL) {
      String result = "";
      try {
         URL url = new URL(sURL);
         result = url.getHost();
      } catch (MalformedURLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }

      return result;
   }
}