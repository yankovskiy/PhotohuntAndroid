package ru.neverdark.photohunt.rest.data;

import java.util.Locale;

/**
* Created by ufo on 10.04.15.
*/
public class Contest {
    public final static int MAX_VOTE_COUNT = 3;
    public final static int STATUS_CLOSE = 0;
    public final static int STATUS_OPEN = 1;
    public final static int STATUS_VOTES = 2;

    public long id;
    public String subject;
    public int rewards;
    public String open_date;
    public String close_date;
    public int status;
    public long user_id;
    public String display_name;
    public int works;
    public long prev_id;
    public String avatar;

    @Override
    public String toString() {
        return String
                .format(Locale.US,
                        "Contest id = %d, subject = %s, rewards = %d, open_date = %s, close_date = %s, status = %d, user_id = %d, display_name = %s, prev_id = %d",
                        id, subject, rewards, open_date, close_date, status, user_id, display_name, prev_id);
    }
}
