package org.hive13.jircbotx.support;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gdata.util.common.util.Base64;



/**
 * Get a web file.
 * 
 * This class is totally hijacked from here:
 * http://nadeausoftware.com/node/73
 * 
 * I have made very limited changes, if any to this class.
 */
public final class WebFile {
   public enum eUserAgent
   {
      real, // This uses our 'real' user agent string.
      fake, // This uses a fake one (probably google)
      none  // This leaves it blank.
   }

   // Saved response.
   private java.util.Map<String,java.util.List<String>> responseHeader = null;
   private java.net.URL responseURL = null;
   private int responseCode = -1;
   private String MIMEtype  = null;
   private String charset   = null;
   private Object content   = null;

   public WebFile( String urlString ) throws MalformedURLException, IOException
   {
      this(urlString, "", "", eUserAgent.fake);
   }

   /** Open a web file. */
   public WebFile( String urlString, String username, String password, eUserAgent userAgent)
         throws java.net.MalformedURLException, java.io.IOException {
      // Open a URL connection.
      final java.net.URL url = new java.net.URL( urlString );
      final java.net.URLConnection uconn = url.openConnection( );
      if ( !(uconn instanceof java.net.HttpURLConnection) )
         throw new java.lang.IllegalArgumentException(
               "URL protocol must be HTTP." );
      final java.net.HttpURLConnection conn =
            (java.net.HttpURLConnection)uconn;

      // Set up a request.
      conn.setConnectTimeout( 10000 );    // 10 sec
      conn.setReadTimeout( 10000 );       // 10 sec
      conn.setInstanceFollowRedirects( true );
      if(eUserAgent.fake == userAgent)
         conn.setRequestProperty( "User-agent", BotProperties.getInstance().getUserAgentString() );
      else if(eUserAgent.real == userAgent)
         conn.setRequestProperty( "User-agent", "HiveBot" );

      if(!username.isEmpty())
      {
         String encoded = Base64.encode((username + ":" + password).getBytes());
         conn.setRequestProperty("Authorization", "Basic "+encoded);
      }
      // Send the request.
      conn.connect( );

      // Get the response.
      responseHeader    = conn.getHeaderFields( );
      responseCode      = conn.getResponseCode( );
      responseURL       = conn.getURL( );
      final int length  = conn.getContentLength( );
      final String type = conn.getContentType( );
      if ( type != null ) {
         final String[] parts = type.split( ";" );
         MIMEtype = parts[0].trim( );

         String rgxIsHTML = "(text/x?html|application/xhtml+xml)";
         Pattern p = Pattern.compile(rgxIsHTML);
         Matcher m = p.matcher(MIMEtype);
         if(m.find()) {
            charset = "UTF-8";  // Default to this, if we are getting text.
            for ( int i = 1; i < parts.length; i++ ) {
               final String t  = parts[i].trim( );
               final int index = t.toLowerCase( ).indexOf( "charset=" );
               if ( index != -1 )
                  charset = t.substring( index+8 );
            }
         }


      }

      // Get the content.
      final java.io.InputStream stream = conn.getErrorStream( );
      if ( stream != null )
         content = readStream( length, stream );
      else if ( (content = conn.getContent( )) != null &&
            content instanceof java.io.InputStream )
         content = readStream( length, (java.io.InputStream)content );
      conn.disconnect( );
   }

   /** Read stream bytes and transcode. */
   private Object readStream( int length, java.io.InputStream stream )
         throws java.io.IOException {
      final int buflen = Math.max( 1024, Math.max( length, stream.available() ) );
      byte[] buf   = new byte[buflen];;
      byte[] bytes = null;

      for ( int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf) ) {
         if ( bytes == null ) {
            bytes = buf;
            buf   = new byte[buflen];
            continue;
         }
         final byte[] newBytes = new byte[ bytes.length + nRead ];
         System.arraycopy( bytes, 0, newBytes, 0, bytes.length );
         System.arraycopy( buf, 0, newBytes, bytes.length, nRead );
         bytes = newBytes;
      }

      if ( charset == null )
         return bytes;
      try {
         if(bytes != null)
            return new String( bytes, charset );
      }
      catch ( java.io.UnsupportedEncodingException e ) { }
      return bytes;
   }

   /** Get the content. */
   public Object getContent( ) {
      return content;
   }

   /** Get the response code. */
   public int getResponseCode( ) {
      return responseCode;
   }

   /** Get the response header. */
   public java.util.Map<String,java.util.List<String>> getHeaderFields( ) {
      return responseHeader;
   }

   /** Get the URL of the received page. */
   public java.net.URL getURL( ) {
      return responseURL;
   }

   /** Get the MIME type. */
   public String getMIMEType( ) {
      return MIMEtype;
   }
}
