package ru.neverdark.photohunt.rest.data;

/**
* Created by ufo on 10.04.15.
*/
public class FavoriteUser {
    public long fid;
    public String display_name;
    public String avatar;

    public FavoriteUser(long fid, String display_name, String avatar) {
        this.fid = fid;
        this.display_name = display_name;
        this.avatar = avatar;
    }

    public FavoriteUser() {

    }
}
