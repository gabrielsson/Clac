package cl.sidan.clac.access.impl;

import android.util.Log;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cl.sidan.clac.access.interfaces.Arr;
import cl.sidan.clac.access.interfaces.Article;
import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.Poll;
import cl.sidan.clac.access.interfaces.SidanAccess;
import cl.sidan.clac.access.interfaces.Stats;
import cl.sidan.clac.access.interfaces.UpdateInfo;
import cl.sidan.clac.access.interfaces.User;
import cl.sidan.clac.access.util.JsbJSONInvoker;

public class JSONParserSidanAccess implements SidanAccess {

    private static final String BASE_URL = "http://sidan.cl:9000/jsb-url/Sandbox/";

    private String signature;
    private String password;

    public JSONParserSidanAccess (String signature, String password) {
        this.signature = signature;
        this.password = password;
    }

    @Override
    public final List<Arr> readArr(String date) {
        List<Arr> list = new ArrayList<Arr>();

        JSONObject obj = invoke("ReadArr", "fromDate=" + date);
        JSONArray arr = new JSONArray();

        if(obj != null) {
            arr = obj.optJSONArray("Arr");
        }

        if( arr != null ) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject article = arr.optJSONObject(i);
                list.add(new JSONObjectArr(article));
            }
        }

        return list;
    }

    @Override
    public final List<Article> readArticles(int skip, int take) {
        List<Article> list = new ArrayList<Article>();

        JSONObject obj = invoke("ReadArticles", "Skip=" + skip + "&Take=" + take);
        JSONArray arr = new JSONArray();

        if(obj != null) {
            arr = obj.optJSONArray("Articles");
        }

        if( arr != null ) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject article = arr.optJSONObject(i);
                list.add(new JSONObjectArticle(article));
            }
        }

        return list;
    }

    @Override
    public final List<Entry> readEntries(int skip, int take) {
        List<Entry> list = new ArrayList<Entry>();
        JSONObject obj = invoke("ReadEntries", "Skip=" + skip + "&Take=" + take);
        JSONArray arr = new JSONArray();

        if(obj != null) {
            arr = obj.optJSONArray("Entries");
        }

        if( arr != null ) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject entry = arr.optJSONObject(i);
                list.add(new JSONObjectEntry(entry));
            }
        }

        return list;
    }

    @Override
    public final List<Entry> searchEntries(String searchString, int skip, int take) {
        List<Entry> list = new ArrayList<Entry>();
        JSONObject obj = invoke("SearchEntries", "SearchString=" + searchString + "&Skip=" + skip + "&Take=" + take);
        JSONArray arr = new JSONArray();

        if(obj != null) {
            arr = obj.optJSONArray("Entries");
        }

        if( arr != null ) {
            for (int i = 0; i < arr.length(); i++) {
                JSONObject entry = arr.optJSONObject(i);
                list.add(new JSONObjectEntry(entry));
            }
        }

        return list;
    }

    @Override
    public final List<Entry> readEntriesFromId(int take, Integer id) {
        return null;
    }

    @Override
    public final List<Entry> readEntriesToId(int take, Integer id) {
        return null;
    }

    @Override
    public final Entry readEntry(Integer id) {
        return null;
    }

    @Override
    public final void editEntry(Integer id, String message) {

    }

    @Override
    public final void deleteEntry(Integer id) {

    }

    @Override
    public final boolean createEntry(String message, BigDecimal latitude, BigDecimal longitude,
                                     Integer enheter, Integer status, String host, Boolean secret,
                                     String base64Image, String fileName, List<User> sideKicks) {
        if(message == null) {
            message = "";
        }
        // Initial capacity is just set arbitrary big: 300 characters.
        // The default is 16 characters, but then we always need to grow.
        // We got a bug report on OutOfMemoryError, which happened because of String builder
        // enlargement.
        StringBuilder sb = new StringBuilder(300 + message.length());
        try {
            if(message.isEmpty()) {
                sb.append("Message=");
            } else {
                // Base64 encode
                sb.append("Message=").append(URLEncoder.encode(message,"UTF-8"));
            }
            if(latitude != null) sb.append("&Latitude=").append(latitude);
            if(longitude != null) sb.append("&Longitude=").append(longitude);
            if(enheter != null) sb.append("&Enheter=").append(enheter);
            if(status != null) sb.append("&Status=").append(status);

            if(host != null && !host.isEmpty()) {
                sb.append("&Host=").append(URLEncoder.encode(host, "UTF-8"));
            }
            if(secret != null && secret) {
                sb.append("&Secret=True");
            }
            if(fileName != null && !fileName.isEmpty()) {
                sb.append("&FileName=").append(fileName);
            }
            if(base64Image != null && !base64Image.isEmpty()) {
                sb.append("&Base64Image=").append(base64Image);
            }

            if(sideKicks != null && !sideKicks.isEmpty()) {
                JSONArray ja = new JSONArray();
                sb.append("&SideKicks=[");
                try {
                    String user = "";
                    for( User u : sideKicks ) {
                        user = u.getSignature().substring(1);
                        JSONObject jo = new JSONObject();
                        jo.put("Signature", user);
                        ja.put(jo);
                    }
                    sb.append(ja.join(",")).append("]");
                } catch (JSONException err) {
                    Log.e("JSONERROR", "Error parsing JSON: " + err.getMessage());
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        //notify invoke("Notify....");
        sendNotification(message);

        return invoke("CreateEntry", sb.toString()) != null;
    }

    protected void sendNotification(String message) {
        List<String> allMatches = getAllSignaturesInMessage(message);

        String csvSignatures = listToCsv(allMatches);

        Map<String, String> map = getGCMRecipients(csvSignatures);

        for(Map.Entry<String, String> e: map.entrySet()) {
            StringBuilder sb = new StringBuilder(300 + message.length());
            try {
                sb.append("Message=").append(URLEncoder.encode(message, "UTF-8"));
            } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            }
            sb.append("&RegId=").append(e.getKey());



            invoke("Notify", sb.toString());
        }
    }

    private String listToCsv(List<String> allMatches) {
        return TextUtils.join(",", allMatches);
    }


    protected List<String> getAllSignaturesInMessage(String message) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile("#\\d\\d?")
                .matcher(message);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches;
    }

    @Override
    public final void createLike(Integer id, String uniqueIdentifier) {
        invoke("CreateLike", "Id=" + id + "&Host=" + uniqueIdentifier);
    }

    @Override
    public final void createOrUpdateArr(Integer id, String namn, String plats, String datum) {

        if(id != null) {
            invoke("UpdateArr", "Id=" + id + "&Name=" + namn +
                    "&Place=" + plats + "&Date=" + datum);
        } else {
            invoke("CreateArr", "Name=" + namn +
                    "&Place=" + plats + "&Date=" + datum);
        }

    }

    @Override
    public final void registerArr(Integer id) {
        invoke("RegisterArr", "Id=" + id);
    }

    @Override
    public final void unregisterArr(Integer id) {
        invoke("UnregisterArr", "Id=" + id);
    }

    @Override
    public final void lurpassaOnArr(Integer id) {
        invoke("LurpassaArr", "Id=" + id);
    }

    @Override
    public final void lurpassaOffArr(Integer id) {
        invoke("LurpassaOffArr", "Id=" + id);
    }

    @Override
    public final boolean votePoll(Integer id, Integer votedOnYay) {
        invoke("VotePoll", "Id=" + id + "&" + "VotedOn=" + votedOnYay);
        return true;
    }

    @Override
    public final Poll readPoll() {
        JSONObject obj = invoke("ReadPoll", "");

        List<Poll> list = new ArrayList<Poll>();
        JSONArray arr = new JSONArray();

        if(obj != null) {
            arr = obj.optJSONArray("Poll");
        }

        for (int i = 0; i < arr.length(); i++) {
            JSONObject entry = arr.optJSONObject(i);
            list.add(new JSONObjectPoll(entry));
        }
        if( list.isEmpty() ) {
            return null;
        }
        return list.get(0);
    }

    @Override
    public final void registerGCM(String regId, String deviceId) {
        if( regId == null || regId.trim().isEmpty() ){
            return;
        }

        StringBuilder sb = new StringBuilder(60);
        try {
            sb.append("RegId=").append(URLEncoder.encode(regId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            sb.append("RegId=").append(regId);
        }
        try {
            sb.append("&DeviceId=").append(URLEncoder.encode(deviceId, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            sb.append("&DeviceId=").append(deviceId);
        }
        invoke("GCM_Register", sb.toString());
    }

    @Override
    public final void unregisterGCM(String regId) {
        if( regId == null || regId.trim().isEmpty() ){
            return;
        }

        StringBuilder sb = new StringBuilder();
        try {
            sb.append("RegId=").append(URLEncoder.encode(regId,"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            sb.append("RegId=").append(regId);
        }
        invoke("GCM_Unregister", sb.toString());
    }

    @Override
    public final Map<String, String> getGCMRecipients(String csvSigs) {
        if( csvSigs == null || csvSigs.trim().isEmpty() ){
            return Collections.EMPTY_MAP;
        }

        StringBuilder sb = new StringBuilder();
        try {
            sb.append("CsvSigs=").append(URLEncoder.encode(csvSigs, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            sb.append("CsvSigs=").append(csvSigs.replace("#", "%23"));
        }
        String req = sb.toString();

        JSONObject obj = invoke("GCM_GetRecipients", req);
        JSONArray entriesArray = new JSONArray();
        if(obj != null) {
            entriesArray = obj.optJSONArray("Entries");
        }

        // It is something with timing here. One hypothesis is that obj!=null, but obj.optJSONArray
        // somehow returns null.
        // Let's check entriesArray; if there is too much problem with this abrupt return, then we
        // should investigate this further.
        if( entriesArray == null || entriesArray.length() == 0 ){
            return Collections.EMPTY_MAP;
        }

        Map<String,String> retMap = new TreeMap<>();
        for (int i = 0; i < entriesArray.length(); i++) {
            JSONObject temp = entriesArray.optJSONObject(i);
            try {
                retMap.put(temp.getString("RegId"), temp.getString("Signature"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return retMap;
    }

    @Override
    public final String getGCMRegIdFromDeviceId(String deviceId) {
        JSONObject obj = invoke("GCM_GetRegId", "DeviceId=" + deviceId);

        String regId = null;
        if( obj != null ) {
            try {
                regId = obj.getString("RegId");
                Log.e("GCM", "Got registered id from device id: " + regId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return regId;
    }

    @Override
    public final boolean authenticateUser() {
        JSONObject json = null;
        try {
            json = invoke("AuthenticateTest", "");
        } catch (Exception e) {
            return false;
        }
        return json!=null;
    }

    @Override
    public List<User> readMembers(boolean onlyValidUsers) {
        String request = "";
        if (onlyValidUsers) {
            request="OnlyValidUsers=true";
        }

        JSONObject json = invoke("GetKumpaner", request);
        JSONArray array = new JSONArray();
        if(json != null) {
            array = json.optJSONArray("Kumpaner");
        }

        List<User> result = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                result.add(new JSONObjectUser(array.optJSONObject(i)));
            }
        }
        return result;
    }

    @Override
    public List<Stats> readStats(String type) {
        int take = 5;
        String request = "Take="+take;

        JSONObject json = invoke("GetStats"+type, request);
        JSONArray array = new JSONArray();
        if(json != null) {
            array = json.optJSONArray("Stats");
        }

        List<Stats> result = new ArrayList<>();
        if (array != null) {
            for (int i = 0; i < array.length(); i++) {
                result.add(new JSONObjectStats(array.optJSONObject(i)));
            }
        }
        return result;
    }

    @Override
    public boolean updatePassword(String forSignature, String password, String admin) {
        JSONObject jsonObject = invoke("ChangePassword", "User=" + forSignature + "&Password=" + password + "&Admin=" + admin);
        return true;
    }

    @Override
    public UpdateInfo checkForUpdates() {
        JSONObject obj = invoke("CheckForUpdate", "Type=ANDROID");

        return new JSONObjectUpdateInfo(obj);
    }

    private JSONObject invoke(String function, String requestString) {
        String fullUrl = BASE_URL + function + "/json";
        Log.d(getClass().getCanonicalName(), "URL (" + fullUrl + ") requested with parameters (" +
                requestString + ") by " + signature);
        return JsbJSONInvoker.invoke(fullUrl, requestString, signature, password);

    }
}
