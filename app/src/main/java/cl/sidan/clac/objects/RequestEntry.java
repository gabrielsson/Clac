package cl.sidan.clac.objects;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cl.sidan.clac.access.interfaces.Entry;
import cl.sidan.clac.access.interfaces.User;

public class RequestEntry implements Entry, Comparable<Entry> {
    String signature = "";
    ArrayList<User> kumpaner = new ArrayList<>();
    BigDecimal latitude = null;
    BigDecimal longitude = null;
    String message = "";
    String time = "";
    /* java.util.Date is mutable, stor date as long */
    long date = 0;
    long dateTime = 0;
    Integer enheter = 0;
    Integer status = 0;
    Integer likes = 0;
    Integer id = 0;
    String host = "";
    String image = "";
    Boolean secret = false;
    String fileName = "";

    public final String getImage() { return image; }

    public final void setImage(String image) { this.image = image; }

    public final String getSignature() {
        return signature;
    }

    public final List<User> getKumpaner() {
        return kumpaner.isEmpty() ? null : kumpaner;
    }

    public final void setKumpaner(List<User> kumpaner) {
        this.kumpaner.addAll(kumpaner);
    }

    public final void setSignature(String signature) {
        this.signature = signature;
    }

    public final BigDecimal getLatitude() {
        return latitude;
    }

    public final void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public final BigDecimal getLongitude() {
        return longitude;
    }

    public final void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public final String getMessage() {
        return message;
    }

    public final void setMessage(String message) {
        this.message = message;
    }

    public final Date getDate() {  return new Date(date);    }

    public final void setDate(Date date) {
        this.date = date.getTime();
    }

    public final String getTime() {
        return time;
    }

    public final Date getDateTime() {
        return new Date(dateTime);
    }

    public final void setDateTime(Date dateTime) {
        this.dateTime = dateTime.getTime();
    }

    public final void setTime(String time) {
        this.time = time;
    }

    public final Integer getEnheter() {
        return enheter;
    }

    public final void setEnheter(Integer enheter) {
        this.enheter = enheter;
    }

    public final Integer getStatus() {
        return status;
    }

    public final void setStatus(Integer status) {
        this.status = status;
    }

    public final Integer getLikes() {
        return likes;
    }

    public final void setLikes(Integer likes) {
        this.likes = likes;
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

    public final Boolean getSecret() {
        return secret;
    }

    @Override
    public final Boolean getPersonalSecret() {
        return null;
    }

    public final void setSecret(Boolean secret) {
        this.secret = secret;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    @Override
    public int compareTo(Entry another) {
        if (null == another || another.getId() < getId()) {
            return -1;
        } else if (another.getId() > getId()) {
            return 1;
        }
        return 0;
    }
}
