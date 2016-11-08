package io.chino.models;

public class Message {

        private String time;
        private String date;
        private String id;
        private String text;
        private String name;
        private String photoUrl;

        public Message() {
        }

        public Message(String text, String name, String photoUrl, String time, String date) {
            this.text = text;
            this.name = name;
            this.photoUrl = photoUrl;
            this.setDate(date);
            this.setTime(time);
        }

    public String getId() {
            return id;
        }

    public void setId(String id) {
            this.id = id;
        }

    public String getText() {
            return text;
        }

    public void setText(String text) {
            this.text = text;
        }

    public String getName() {
            return name;
        }

    public void setName(String name) {
            this.name = name;
        }

    public String getPhotoUrl() {
            return photoUrl;
        }

    public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}

