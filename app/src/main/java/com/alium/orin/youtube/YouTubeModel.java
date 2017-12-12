package com.alium.orin.youtube;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by liyanju on 2017/12/12.
 */

public class YouTubeModel implements Serializable {


    public ArrayList<Object> youTubeItems = new ArrayList<>();

    public static class YouTubeContent implements Serializable {
        public String style;
        public String name;
        public int data_type;
        public String extra;
        public int type;
        public int id;
        public String icon;
    }

    public static class Title implements Serializable {

        public Title(String style, String name) {
            this.style = style;
            this.name = name;
        }

        public String style;

        public String name;
    }
}
