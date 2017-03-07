package io.chino.utils;

import io.chino.api.user.User;

public class DoctorItem {
    private String name;
    private String lastName;
    private String email;
    private User user;

    public DoctorItem(){

    }

    public DoctorItem(String name, String lastName, String email, User user){
        this.name = name;
        this.lastName = lastName;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
