package cl.sidan.clac.objects;

import cl.sidan.clac.access.interfaces.User;

public class RequestUser implements User {
    private String number;
    private String name;
    private String im;
    private String title;
    private String phone;
    private String email;
    private String address;
    private boolean valid;

    public RequestUser(String number) {
        this.number = number;
    }

    @Override
    public String getSignature() {
        return number;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getIm() {
        return im;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public Boolean isValid() {
        return valid;
    }
}
