package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.SnP;

public class JSONObjectSnP implements SnP {
    JSONObject obj;

    public JSONObjectSnP(JSONObject arr) {
        this.obj = arr;
    }

    @Override
    public Integer getId() {
        return obj.optInt("Id");
    }

    @Override
    public String getStatus() {
        String status = obj.optString("Status");
        return status;
    }

    @Override
    public Integer getNumber() {
        return obj.optInt("Number");
    }

    @Override
    public String getSignature() {
        return getStatus() + getNumber();
    }

    @Override
    public String getName() {
        return obj.optString("Name");
    }

    @Override
    public String getEmail() {
        return obj.optString("Email");
    }

    @Override
    public String getPhone() {
        return obj.optString("Phone");
    }

    @Override
    public String getHistory() {
        return obj.optString("History");
    }
}
