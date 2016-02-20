package cl.sidan.clac.fragments;

import cl.sidan.clac.access.interfaces.User;

public class RequestUser implements User {
    private String signature = "";
    private String name = "";
    private String im = "";
    private String title = "";

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
}
