package cl.sidan.clac.access.interfaces;

public interface User {
    String getSignature();
    String getName();
    String getIm();
    String getTitle();
    String getPhone();
    String getEmail();
    Boolean isValid();
}
