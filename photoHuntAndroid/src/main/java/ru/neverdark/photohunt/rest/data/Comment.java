package ru.neverdark.photohunt.rest.data;

public class Comment {
    public long id;
    public long user_id;
    public String display_name;
    public String datetime;
    public String comment;
    public String avatar;
    public boolean is_can_deleted;

    public Comment() {

    }

    public Comment(String comment) {
        this.comment = comment;
    }
}
