package cl.sidan.clac;

import junit.framework.Assert;

import org.junit.Test;

import java.util.HashMap;

import cl.sidan.clac.adapters.AdapterEntries;

public class TestUrlParser {
    /*
     * uris contains the parsable message before and after transformation.
     * i.e.
     *  key: http://www.example.com/
     *  value: <a href="http://www.example.com/">http://www.example.com/</a>
     */
    HashMap<String, String> uris = new HashMap<>();

    public TestUrlParser() {
        // Replace HTTP
        uris.put("http://www.example.com/",
                "<a href='http://www.example.com/'>http://www.example.com/</a>");

        // Replace HTTPS
        uris.put("https://www.example.com",
                "<a href='https://www.example.com'>https://www.example.com</a>");

        // Replace FTP
        uris.put("ftp://www.example.com/",
                "<a href='ftp://www.example.com/'>ftp://www.example.com/</a>");

        // Replace www-addresses with link
        uris.put("www.example.com",
                "<a href='www.example.com'>www.example.com</a>");

        // Don't disturb already resolved URI.
        uris.put("<a href=www.example.com>link</a>",
                "<a href='www.example.com'>link</a>");

        // Don't disturb already resolved URI starting with '
        uris.put("<a href='www.example.com'>link</a>",
                "<a href='www.example.com'>link</a>");

        // Don't disturb already resolved URI starting with "
        uris.put("<a href=\"www.example.com\">link</a>",
                "<a href=\"www.example.com\">link</a>");

        // Replace inmailat/ with absolute URI
        uris.put("<img src=inmailat/pics.jpg />",
                "<img src='http://sidan.cl/inmailat/pics.jpg' />");

        // Actual failed URIs
        uris.put("www.meetup.com/Javaforum-Goteborg/members/195180612/",
                "<a href=\'www.meetup.com/Javaforum-Goteborg/members/195180612/\'>www.meetup.com/Javaforum-Goteborg/members/195180612/</a>");

        uris.put("<a href=inmailat/160914153613/Capsning.pdf>inmailat/160914153613/Capsning.pdf</a>",
                "<a href='http://sidan.cl/inmailat/160914153613/Capsning.pdf'>inmailat/160914153613/Capsning.pdf</a>");

        uris.put("<a href='inmailat/160920191347/Screenshot_2016-09-20-11-58-00.png'>Screenshot_2016-09-20-11-58-00.png</a><br>#56: kan du inhysa tjejen ovan, hon vill ha ett sovrum bara!",
                "<a href='http://sidan.cl/inmailat/160920191347/Screenshot_2016-09-20-11-58-00.png'>Screenshot_2016-09-20-11-58-00.png</a><br>#56: kan du inhysa tjejen ovan, hon vill ha ett sovrum bara!");

        uris.put("<img src=http://www.gifs.net/Animation11/Jobs_and_People/Police/Siren_2.gif>",
                "<img src='http://www.gifs.net/Animation11/Jobs_and_People/Police/Siren_2.gif'>");
    }


    @Test
    public void replaceURIs() {
        System.out.println("Testing " + uris.size() + " different URIs.");
        for (String key : uris.keySet()) {
            Assert.assertEquals(uris.get(key), AdapterEntries.replaceURIs(key));
        }
    }

}
