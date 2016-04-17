package cl.sidan.clac.access.impl;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.User;

public class JSONObjectEntry implements Entry, Parcelable {
    private JSONObject obj;

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";

    public JSONObjectEntry(JSONObject entry) {
        obj = entry;
    }

    @Override
    public String getSignature() {
        return obj.optString("Signature");
    }

    @Override
    public List<User> getKumpaner() {
        JSONArray kumpanString = null;
        if(obj != null) {
            kumpanString = obj.optJSONArray("SideKicks");
        }

        List<User> kumpaner = new ArrayList<>();
        if( kumpanString != null ) {
            for (int i = 0; i < kumpanString.length(); i++) {
                JSONObject kumpan = kumpanString.optJSONObject(i);
                kumpaner.add(new JSONObjectUser(kumpan));
            }
        }
        return kumpaner;
    }

    @Override
    public BigDecimal getLatitude() {
        return JSONObjectUtil.getBigDecimal(obj.optString("Latitude"));
    }

    @Override
    public BigDecimal getLongitude() {
        return JSONObjectUtil.getBigDecimal(obj.optString("Longitude"));
    }

    @Override
    public String getMessage() {
        return obj.optString("Message");
    }

    @Override
    public Date getDate() {
        DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());

        try {
            return format.parse(obj.optString("Date"));
        } catch (ParseException e) {
            Log.e("Error", "Cannot parse date: " + e.getMessage());
        }

        return null;
    }

    @Override
    public String getTime() {
        return obj.optString("Time");
    }

    @Override
    public Date getDateTime() {
        try {
            DateFormat format = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("GMT+1"));
            Long date = format.parse(obj.optString("Date")).getTime();

            format = new SimpleDateFormat(TIME_FORMAT, Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("GMT+1"));
            Long time = format.parse(obj.optString("Time")).getTime();

            return new Date(date + time);
        } catch (ParseException e) {
            Log.e("Error", "Cannot parse date: " + e.getMessage());
        }

        return null;
    }

    @Override
    public Integer getEnheter() {
        return obj.optInt("Enheter");
    }

    @Override
    public Integer getStatus() {
        return obj.optInt("Status");
    }

    @Override
    public Integer getLikes() {
        return obj.optInt("Likes");
    }

    @Override
    public Integer getId() {
        return obj.optInt("Id");
    }

    @Override
    public String toString() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDate());
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        String beers = "";

        for (int i=0; i< getEnheter(); i++) {
            beers += "Þ";
        }

        return getMessage() + "\n" + getSignature() + " | " + year + "-"+month + "-" + day + " " + getTime() + " |+" + getLikes() + " | " + beers ;
    }

    @Override
    public String getHost() {
        return obj.optString("Host");
    }

    @Override
    public Boolean getSecret() {
        return obj.optBoolean("Secret");
    }

    @Override
    public Boolean getPersonalSecret() {
        return obj.optBoolean("PersonalSecret");
    }

    @Override
    public String getImage() { return obj.optString("Base64Image"); }

    @Override
    public String getFileName() { return obj.optString("FileName"); }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(obj.toString());
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Creator<JSONObjectEntry> CREATOR = new Creator<JSONObjectEntry>() {
        public JSONObjectEntry createFromParcel(Parcel in) {
            return new JSONObjectEntry(in);
        }

        public JSONObjectEntry[] newArray(int size) {
            return new JSONObjectEntry[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private JSONObjectEntry(Parcel in) {
        try {
            obj = new JSONObject(in.readString());
        } catch (JSONException e) {
            Log.e("Error...", "Cannot serialize Entry");
        }
    }

    @Override
    public int compareTo(Entry another) {
        if (null == another || another.getId() < getId()) {
            return -1;
        } else if (another.getId() > getId()) {
            return 1;
        }
        return 0;
    }
}
