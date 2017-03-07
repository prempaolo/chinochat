package io.chino.models;

public class Request {

    private String name;
    private String lastName;
    private String email;
    private String role;

    public Request(){

    }

    public Request(String name, String lastName, String role){
        this.name = name;
        this.lastName = lastName;
        this.role = role;
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

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
