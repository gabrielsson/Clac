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
        return obj.optString("LatestVersion", "0");
    }

    @Override
    public String getURL() {
        return obj.optString("URL", "http://sidan.cl/clacen/clac-release.apk");
    }

    @Override
    public String getNews() {
        return obj.optString("News", "Something wrong.. I could try to download from the standard place.. But I'm not sure it will work. Maybe try later?");
    }
}
