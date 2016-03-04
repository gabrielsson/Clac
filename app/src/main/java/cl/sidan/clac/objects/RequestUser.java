package cl.sidan.clac.objects;

import cl.sidan.clac.access.interfaces.User;

public class RequestUser implements User {
    private String signature = "";
    private String name = "";
    private String im = "";
    private String title = "";
    private String phone = "";
    private String email = "";
    private Boolean isIgnored = false;

    RequestUser(String signature, String name, String im, String title){
        this.signature = signature;
        this.name = name;
        this.im = im;
        this.title = title;
    }

    public RequestUser(String signature) {
        if( signature.startsWith("#") ) {
            this.signature = signature;
        } else {
            this.signature = "#" + signature;
        }
    }

    @Override
    public String getSignature() {
        return signature;
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
    public String getPhone(){
        return phone;
    }

    @Override
    public String getEmail(){
        return email;
    }

    @Override
    public Boolean isIgnored(){
        return isIgnored;
    }
}
