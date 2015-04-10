package ru.neverdark.photohunt.rest.data;

/**
* Created by ufo on 10.04.15.
*/
public class Message {
    public final static int UNSENT = 0;
    public final static int SENT = 1;
    public final static int READ = 2;

    public long id;
    public long from_user_id;
    public long to_user_id;
    public String from;
    public String to;
    public String date;
    public String title;
    public String message;
    public int status;
    public String from_avatar;
    public String to_avatar;
}
