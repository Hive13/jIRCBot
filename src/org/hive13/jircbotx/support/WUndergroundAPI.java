package org.hive13.jircbotx.support;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class WUndergroundAPI {
   public static final int DAY_FLOOD_LIMIT = 450;
   public static final int MINUTE_FLOOD_LIMIT = 2;
   
   public static final int REQUEST_FAIL_FLOOD_LIMIT = -1024;
   public static final int REQUEST_FAIL_NO_RESULT = -1025;
   public static final int REQUEST_FAIL_INVALID_API_KEY = -1026;
   public static final int REQUEST_FAIL_UNINITIALIZED = -1027;
   
   public static String LOCATION_ID = "45223";
   
   private static Queue<Long> floodPrevention = new LinkedList<Long>();
   
   private static double savedTemperature = REQUEST_FAIL_UNINITIALIZED;
   
   /**
    * Checks if we are currently in a flooded state with regard to requests.
    * 
    * If not, it adds a request event to the flood prevention queue.
    * 
    * @return  True we successfully added the current event to the flood prevention queue
    *           and are good to make our request.
    *          False means we are currently flooded and should not make any requests to
    *          the Weather Underground API.
    */
   private static boolean logRequestEvent() {
      boolean canAddFloodEvent = checkFloodStatus();
      if(canAddFloodEvent)
         floodPrevention.add(Calendar.getInstance().getTimeInMillis());
      return canAddFloodEvent;
   }
   
   /**
    * Checks the request flood queue to see if we have flooded the Weather Underground API.
    * 
    * @return  True, we are not currently in a "flooded" state.
    *          False, we are currently in a flooded state.
    */
   public static boolean checkFloodStatus() {
      // Find current time
      Calendar calNow = Calendar.getInstance();
      long now = calNow.getTimeInMillis();
      // Find 1 day ago in milliseconds
      long dayAgo = now - 86400000;
      // Find 1 minute ago in milliseconds.
      long minuteAgo = now - 60000;
      
      if(floodPrevention.size() == 0) return true;
      
      // Remove events older than 24 hours.
      while(floodPrevention.peek() != null && floodPrevention.peek() < dayAgo ) floodPrevention.remove();
      
      // Check to see if we have more than the flood limit
      if(floodPrevention.size() >= DAY_FLOOD_LIMIT) return false;
      
      int minuteCount = 0;
      Iterator<Long> it = floodPrevention.iterator();
      while(it.hasNext()) {
         if(it.next() > minuteAgo) minuteCount++;
      }
      if(minuteCount >= MINUTE_FLOOD_LIMIT) return false;
      
      return true;
   }
   
   /**
    * Test the flood prevention queue to make sure it works by calling this function.
    */
   public static boolean dummyFloodTest() {
      return logRequestEvent();
   }
   /**
    * Returns the temperature for the currently set LOCATION_ID.
    * 
    * If it fails to determine the temperature for some reason (flood limit, invalid station)
    * it will return a value <= -1024, you can compare this value to the REQUEST_FAIL_* constants
    * defined in this class to determine the exact failure.
    * 
    * @return  Returns the current temperature or a failure code (failure codes are less than
    *          or equal to -1024)
    */
   public static double getTemperature() {
      
      double dResult = REQUEST_FAIL_FLOOD_LIMIT;
      
      // Check if we have hit our flood limit for the day // minute.
      if(logRequestEvent()) {
         dResult = REQUEST_FAIL_NO_RESULT;
         DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
           domFactory.setNamespaceAware(true);
           DocumentBuilder builder;
         try {
            builder = domFactory.newDocumentBuilder();
              Document doc = builder.parse("http://api.wunderground.com/api/" + BotProperties.getInstance().getWundergroundAPIKey() + "/conditions/q/" + LOCATION_ID + ".xml");
              
              XPath xpath = XPathFactory.newInstance().newXPath();
              
              XPathExpression expr = xpath.compile("/response/current_observation/temp_f");
              
              dResult = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
              savedTemperature = dResult;
         } catch (ParserConfigurationException e) {
            e.printStackTrace();
         } catch (SAXException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } catch (XPathExpressionException e) {
            e.printStackTrace();
         }
      } else if(savedTemperature != REQUEST_FAIL_UNINITIALIZED) {
         dResult = savedTemperature;
      } else {
         dResult = REQUEST_FAIL_FLOOD_LIMIT;
      }
        
        return  dResult;
   }

}
