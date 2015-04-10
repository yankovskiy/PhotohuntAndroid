package ru.neverdark.photohunt.rest.data;

/**
* Created by ufo on 10.04.15.
*/
public class Item {
    public final static String EXTRA_PHOTO = "extra_photo";
    public final static String AVATAR = "avatar";
    public final static String EXTRA_CONTEST = "extra_contest";
    public final static String PREMIUM7 = "premium7";
    public final static String PREMIUM30 = "premium30";

    public long id;
    public String name;
    public String service_name;
    public String description;
    public int count;
    public int auto_use;

    public Item() {

    }

    public Item(Goods goods) {
        name = goods.name;
        description = goods.description;
        auto_use = goods.auto_use;
        service_name = goods.service_name;
        count = 1;
    }
}
