package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.User;

public class JSONObjectUser implements User {
    JSONObject obj;

    public JSONObjectUser(JSONObject arr) {
        this.obj = arr;
    }

    @Override
    public String getSignature() {
        String nummer = obj.optString("Number");
        if(!nummer.startsWith("#")) {
            nummer = "#" + nummer;
        }
        return nummer;
    }

    @Override
    public String getName() {
        return obj.optString("Name");
    }

    @Override
    public String getIm() {
        return obj.optString("Im");
    }

    @Override
    public String getTitle() {
        return obj.optString("Title");
    }

    @Override
    public String getPhone() {
        return obj.optString("Phone");
    }

    @Override
    public String getEmail() {
        return obj.optString("Email");
    }

    @Override
    public Boolean isValid() {
        return obj.optBoolean("IsValid");
    }
}
