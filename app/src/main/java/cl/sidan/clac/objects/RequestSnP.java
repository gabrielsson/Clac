package cl.sidan.clac.objects;

import cl.sidan.clac.access.interfaces.SnP;

public class RequestSnP implements SnP {
    private Integer id;
    private String status;
    private Integer number;
    private String name;
    private String phone;
    private String email;
    private String history;

    public RequestSnP(String status, Integer number) {
        this.status = status;
        this.number = number;
    }

    public final Integer getId() {
        return id;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public Integer getNumber() {
        return number;
    }

    @Override
    public String getSignature() {
        return status + number;
    }

    @Override
    public String getName() {
        return name;
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
    public String getHistory() {
        return history;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setHistory(String history) {
        this.history = history;
    }
}
