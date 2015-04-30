package ru.neverdark.photohunt.rest.data;

import java.io.Serializable;
import java.util.Locale;

/**
* Created by ufo on 10.04.15.
*/
public class Image implements Serializable {
    public long id;
    public long contest_id;
    public long user_id;
    public String subject;
    public String display_name;
    public int vote_count;
    public boolean is_editable;
    public boolean is_voted;
    public String contest_subject;
    public Exif exif;
    public String avatar;
    public String description;

    @Override
    public String toString() {
        return String
                .format(Locale.US,
                        "Image id = %d, contest_id = %d, user_id = %d, subject = %s, display_name = %s, vote_count = %d, is_editable = %b, is_voted = %b, contest_subject = %s",
                        id, contest_id, user_id, subject, display_name, vote_count, is_editable, is_voted, contest_subject);
    }
}
