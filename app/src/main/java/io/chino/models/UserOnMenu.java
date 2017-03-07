package io.chino.models;

/**
 * Created by Paolo on 1/11/2017.
 */

public class UserOnMenu {

    private String user_id;
    private String email;
    private String chat_id;

    public UserOnMenu(){

    }

    public UserOnMenu(String user_id, String email, String chat_id){
        this.chat_id = chat_id;
        this.email = email;
        this.user_id = user_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }
}
