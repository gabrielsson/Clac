package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.UpdateInfo;

public class JSONObjectUpdateInfo implements UpdateInfo {
    JSONObject obj;

    public JSONObjectUpdateInfo(JSONObject info) {
        this.obj = info;
    }

    @Override
    public String getLatestVersion() {
        String DEFAULT = "0";
        if (null == obj) {
            return DEFAULT;
        }
        return obj.optString("LatestVersion", DEFAULT);
    }

    @Override
    public String getURL() {
        String DEFAULT = "http://sidan.cl/clac/app-release.apk";
        if (null == obj) {
            return DEFAULT;
        }
        return obj.optString("URL", DEFAULT);
    }

    @Override
    public String getNews() {
        String DEFAULT = "Something wrong.. I could try to download from the standard place.. But I'm not sure it will work. Maybe try later?";
        if (null == obj) {
            return DEFAULT;
        }
        return obj.optString("News", DEFAULT);
    }
}
