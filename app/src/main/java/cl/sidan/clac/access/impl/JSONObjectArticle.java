package cl.sidan.clac.access.impl;

import android.util.Log;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cl.sidan.clac.access.interfaces.Article;

/**
 * Created by max.gabrielsson on 2014-10-14.
 */
public class JSONObjectArticle implements Article {
    JSONObject obj;

    private static final String DATE_FORMAT = "yyyy";


    public JSONObjectArticle(JSONObject article) {
        this.obj = article;
    }

    @Override
    public String getId() {
        return obj.optString("Id");
    }

    @Override
    public String getHeader() {
        return obj.optString("Header");
    }

    @Override
    public String getBody() {
        return obj.optString("Body");
    }

    @Override
    public Date getDate() {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT);

        try {
            return format.parse(obj.optString("Date"));
        } catch (ParseException e) {
            Log.e("Error", "Cannot parse date: " + e.getMessage());
        }

        return null;
    }
}
