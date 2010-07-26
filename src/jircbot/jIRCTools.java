package jircbot;

import static com.rosaloves.bitlyj.Bitly.*;

public class jIRCTools {
	public static String bitlyName = "";
	public static String bitlyAPIKey = "";
	
	public static String generateShortURL(String longURL) {
		return generateShortURL(longURL, bitlyName, bitlyAPIKey);
	}
	public static String generateShortURL(String longURL, String username, String apikey) {
		String result = "Username or API key are not initialized";
		if(bitlyName.length() > 0 && bitlyAPIKey.length() > 0)
			result = as(username, apikey).call(shorten(longURL)).getShortUrl();
		return result;
	}
}
