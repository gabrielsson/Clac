package cl.sidan.clac.access.impl;

import org.json.JSONObject;

import cl.sidan.clac.access.interfaces.Poll;

public class JSONObjectPoll implements Poll {
    JSONObject obj;

    public JSONObjectPoll(JSONObject poll) {
        this.obj = poll;
    }

    @Override
    public Integer getId() {
        return obj.optInt("Id");
    }

    @Override
    public String getQuestion() {
        return obj.optString("Question");
    }

    @Override
    public String getYae() {
        return obj.optString("Yae");
    }
    @Override
    public String getNay() {
        return obj.optString("Nay");
    }

    @Override
    public String getDatum() {
        return obj.optString("Date");
    }

    @Override
    public Integer getNrYay() {
        return obj.optInt("NrYay");
    }

    @Override
    public Integer getNrNay() {
        return obj.optInt("NrNay");
    }
}
