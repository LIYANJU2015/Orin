package com.alium.orin.soundcloud;

import android.os.Parcel;
import android.text.TextUtils;

import com.alium.orin.model.Song;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liyanju on 2017/11/18.
 */

public class HomeSound implements Serializable{

    private int id;
    private String name;
    private String style;
    private int data_type;
    private int type;
    private List<ContentsBeanX> contents = new ArrayList<>();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getData_type() {
        return data_type;
    }

    public void setData_type(int data_type) {
        this.data_type = data_type;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<ContentsBeanX> getContents() {
        return contents;
    }

    public void setContents(List<ContentsBeanX> contents) {
        this.contents = contents;
    }


    public static class ContentsBean2 implements Serializable{
        private int id;
        private String name;
        private String style;
        private int data_type;
        private int type;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public int getData_type() {
            return data_type;
        }

        public void setData_type(int data_type) {
            this.data_type = data_type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        private ArrayList<Song> contents;

        public ArrayList<Song> getContents() {
            return contents;
        }

        public void setContents(ArrayList<Song> contents) {
            this.contents = contents;
        }
    }

    public static class ContentsBeanX implements Serializable {


        private int id;
        private String name;
        private String style;
        private int data_type;
        private int type;
        public ArrayList<Song> contents = new ArrayList<>();

        public ArrayList<ContentsBean2> contents2 = new ArrayList<>();


        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getStyle() {
            return style;
        }

        public void setStyle(String style) {
            this.style = style;
        }

        public int getData_type() {
            return data_type;
        }

        public void setData_type(int data_type) {
            this.data_type = data_type;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }


        public static class ContentsBean extends Song {

            public ContentsBean() {
            }

            public ContentsBean(int id, String title, int trackNumber, int year, long duration, String data, long dateModified, int albumId, String albumName, int artistId, String artistName) {
                super(id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName);
            }

            /**
             * id : 76312
             * source_id : 200302
             * song_name : XXXTENTACION - Fuck Love  (feat. Trippie Redd)
             * singer : XXXTENTACION
             * album_name :
             * album_images : https://i1.sndcdn.com/artworks-Gp90YQmWXXFN-0-large.jpg
             * album_local_images : http://resource.gomocdn.com/soft/micro/module/music/BL2huJ8hwb.jpg
             * song_play_time : 146611
             * song_download_url : https://api.soundcloud.com/tracks/339318387/stream
             * license_url :
             * style : listView
             * reference_id : 294a68cbbcdb8b006c05645625a830b3
             * update_time_in_mills : 1505232985000
             */
            public int position;

            public String song_play_time;
            public int source_id;
            public String album_images;
            public String album_local_images;
            public String song_download_url;
            public String license_url;
            public String style;
            public String reference_id;

            @Override
            public String getAlbum_images() {
                return album_images;
            }

            @Override
            public long getDuration() {
                try {
                    if (!TextUtils.isEmpty(song_play_time)) {
                        return Long.parseLong(song_play_time);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            public String getPath() {
                return song_download_url;
            }

            @Override
            public boolean isLocalSong() {
                return false;
            }

            @Override
            public int getPosition() {
                return position;
            }

            public ContentsBean(Parcel in) {
                super(in);
                source_id = in.readInt();
                album_images = in.readString();
                album_local_images = in.readString();
                song_download_url = in.readString();
                license_url = in.readString();
                style = in.readString();
                reference_id = in.readString();
                song_play_time = in.readString();
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                super.writeToParcel(dest, flags);
                dest.writeInt(source_id);
                dest.writeString(album_images);
                dest.writeString(album_local_images);
                dest.writeString(song_download_url);
                dest.writeString(license_url);
                dest.writeString(style);
                dest.writeString(reference_id);
                dest.writeString(song_play_time);
            }

            @Override
            public boolean equals(Object o) {
                if (super.equals(o)) {
                    Song song = (Song) o;
                    if (!getPath().equals(song.getPath())) {
                        return false;
                    }
                    return true;
                }
                return false;
            }
        }
    }
}
