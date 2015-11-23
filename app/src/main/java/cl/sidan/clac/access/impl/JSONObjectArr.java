package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.Arr;

public class JSONObjectArr implements Arr {
    JSONObject obj;

    public JSONObjectArr(JSONObject arr) {
        this.obj = arr;
    }

    @Override
    public Integer getId() {
        return obj.optInt("Id");
    }

    @Override
    public String getNamn() {
        return obj.optString("Namn");
    }

    @Override
    public String getPlats() {
        return obj.optString("Plats");
    }
    @Override
    public String getDatum() {
        return obj.optString("Datum");
    }

    @Override
    public String getDeltagare() {
        return obj.optString("Deltagare");
    }

    @Override
    public String getHetsade() {
        return obj.optString("Hetsade");
    }

    @Override
    public String getKanske() {
        return obj.optString("Kanske");
    }

    @Override
    public String getHost() {
        return null;
    }

}
