package cl.sidan.clac.objects;


import cl.sidan.clac.access.interfaces.Arr;

public class RequestArr implements Arr {
    String arr_namn = "";
    String datum = "";
    String plats = "";
    String host = "";
    String deltagare = "";
    String hetsade = "";
    String kanske = "";
    Integer id = -1;

    public final String getNamn() {
        return arr_namn;
    }

    public final void setNamn(String namn) {
        arr_namn = namn;
    }

    public final String getDeltagare() {
        return deltagare;
    }

    public final void setDeltagare(String deltagare) {
        this.deltagare = deltagare;
    }

    public final String getHetsade() {
        return hetsade;
    }

    public final void setHetsade(String hetsade) {
        this.hetsade = hetsade;
    }

    public final String getKanske() {
        return kanske;
    }

    public final void setKanske(String kanske) {
        this.kanske = kanske;
    }

    public final String getDatum() {
        return datum;
    }

    public final void setDatum(String datum) {
        this.datum = datum;
    }

    public final String getPlats() {
        return plats;
    }

    public final void setPlats(String plats) {
        this.plats = plats;
    }

    public final Integer getId() {
        return id;
    }

    public final void setId(Integer id) {
        this.id = id;
    }

    public final String getHost() {
        return host;
    }

    public final void setHost(String host) {
        this.host = host;
    }
}
