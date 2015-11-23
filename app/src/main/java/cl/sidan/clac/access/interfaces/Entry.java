package cl.sidan.clac.access.interfaces;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface Entry {
    String getSignature();
    List<User> getKumpaner();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    String getMessage();
    Date getDate();
    String getTime();
    Date getDateTime();
    Integer getEnheter();
    Integer getStatus(); //    1 = politics    2 = complaints    3 = noshow    4 = nerd    5 = NSFW    blank = default
    Integer getLikes(); //Sum of all likes
    Integer getId(); // Identifier
    String getHost();
    Boolean getSecret();
    Boolean getPersonalSecret();
    String getImage();
    String getFileName();
}
