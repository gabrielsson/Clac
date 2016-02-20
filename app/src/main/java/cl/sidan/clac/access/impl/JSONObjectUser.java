package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.Arr;
import cl.sidan.clac.access.interfaces.User;

public class JSONObjectUser implements User {
    JSONObject obj;

    public JSONObjectUser(JSONObject arr) {
        this.obj = arr;
    }

    @Override
    public String getSignature() {
        String nummer = obj.optString("SideKickId");
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
        return null;
    }

    @Override
    public String Title() {
        return null;
    }
}
