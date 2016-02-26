package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.Stats;

public class JSONObjectStats implements Stats {
    JSONObject obj;

    public JSONObjectStats(JSONObject stats) {
        this.obj = stats;
    }

    @Override
    public String getSignature() {
        return obj.optString("Signature");
    }

    @Override
    public Integer getTotal() {
        return obj.optInt("Stat");
    }
}
