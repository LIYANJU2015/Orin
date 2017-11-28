package com.alium.orin.lyrics;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.alium.orin.lyrics.all.AZLyrics;
import com.alium.orin.lyrics.all.Bollywood;
import com.alium.orin.lyrics.all.Genius;
import com.alium.orin.lyrics.all.JLyric;
import com.alium.orin.lyrics.all.Lololyrics;
import com.alium.orin.lyrics.all.LyricWiki;
import com.alium.orin.lyrics.all.Lyrics;
import com.alium.orin.lyrics.all.LyricsMania;
import com.alium.orin.lyrics.all.MetalArchives;
import com.alium.orin.lyrics.all.PLyrics;
import com.alium.orin.lyrics.all.UrbanLyrics;
import com.alium.orin.lyrics.all.ViewLyrics;

import java.util.ArrayList;
import java.util.Arrays;

public class DownloadThread extends AsyncTask<Void, Void, Lyrics> {

    private static final String[] mainProviders =
            {

                    "AZLyrics",
                    "Genius",
                    "LyricWiki",
                    "LyricsMania",
                    "Bollywood"
            };

    private static ArrayList<String> providers = new ArrayList<>(Arrays.asList(mainProviders));

    private Lyrics.Callback callback;
    private boolean positionAvailable;
    private String parms[];

    public DownloadThread(final Lyrics.Callback callback, boolean positionAvailable, final String... params) {
        this.callback = callback;
        this.positionAvailable = positionAvailable;
        this.parms = params;
    }


    @Override
    protected Lyrics doInBackground(Void... voids) {
        return run(parms);
    }

    @Override
    protected void onPostExecute(Lyrics lyrics) {
        super.onPostExecute(lyrics);
        if (lyrics != null) {
            callback.onLyricsDownloaded(lyrics);
        }
    }

    public static void setProviders(Iterable<String> providers) {
        DownloadThread.providers = new ArrayList<>(Arrays.asList(mainProviders));
        for (String provider : providers) {
            if (provider.equals("ViewLyrics"))
                DownloadThread.providers.add(0, provider);
            else
                DownloadThread.providers.add(provider);
        }
    }

    public Lyrics download(String url, String artist, String title) {
        Lyrics lyrics = null;
        for (String provider : providers) {
            switch (provider) {
                case "AZLyrics":
                    lyrics = AZLyrics.fromURL(url, artist, title);
                    break;
                case "Bollywood":
                    lyrics = Bollywood.fromURL(url, artist, title);
                    break;
                case "Genius":
                    lyrics = Genius.fromURL(url, artist, title);
                    break;
                case "JLyric":
                    lyrics = JLyric.fromURL(url, artist, title);
                    break;
                case "Lololyrics":
                    lyrics = Lololyrics.fromURL(url, artist, title);
                    break;
                case "LyricsMania":
                    lyrics = LyricsMania.fromURL(url, artist, title);
                    break;
                case "LyricWiki":
                    lyrics = LyricWiki.fromURL(url, artist, title);
                    break;
                case "MetalArchives":
                    lyrics = MetalArchives.fromURL(url, artist, title);
                    break;
                case "PLyrics":
                    lyrics = PLyrics.fromURL(url, artist, title);
                    break;
                case "UrbanLyrics":
                    lyrics = UrbanLyrics.fromURL(url, artist, title);
                    break;
                case "ViewLyrics":
                    lyrics = ViewLyrics.fromURL(url, artist, title);
                    break;
            }
            if (lyrics.isLRC() && !positionAvailable)
                continue;
            if (lyrics != null && lyrics.getFlag() == Lyrics.POSITIVE_RESULT) {
                lyrics.saveLyrics();
                return lyrics;
            }
        }
        return new Lyrics(Lyrics.NO_RESULT);
    }

    public Lyrics download(String artist, String title) {
        Lyrics lyrics = new Lyrics(Lyrics.NO_RESULT);
        for (String provider : providers) {
            switch (provider) {
                case "AZLyrics":
                    lyrics = AZLyrics.fromMetaData(artist, title);
                    break;
                case "Bollywood":
                    lyrics = Bollywood.fromMetaData(artist, title);
                    break;
                case "Genius":
                    lyrics = Genius.fromMetaData(artist, title);
                    break;
                case "JLyric":
                    lyrics = JLyric.fromMetaData(artist, title);
                    break;
                case "Lololyrics":
                    lyrics = Lololyrics.fromMetaData(artist, title);
                    break;
                case "LyricsMania":
                    lyrics = LyricsMania.fromMetaData(artist, title);
                    break;
                case "LyricWiki":
                    lyrics = LyricWiki.fromMetaData(artist, title);
                    break;
                case "MetalArchives":
                    lyrics = MetalArchives.fromMetaData(artist, title);
                    break;
                case "PLyrics":
                    lyrics = PLyrics.fromMetaData(artist, title);
                    break;
                case "UrbanLyrics":
                    lyrics = UrbanLyrics.fromMetaData(artist, title);
                    break;
                case "ViewLyrics":
                    try {
                        lyrics = ViewLyrics.fromMetaData(artist, title);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
            if (lyrics.isLRC() && !positionAvailable)
                continue;
            if (lyrics != null && lyrics.getFlag() == Lyrics.POSITIVE_RESULT)
                return lyrics;
        }
        return lyrics;
    }

    public Lyrics run(String params[]) {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        Lyrics lyrics;
        String artist = null;
        String title = null;
        String url = null;
        switch (params.length) {
            case 3: // URL + tags
                artist = params[1];
                title = params[2];
            case 1: // URL
                url = params[0];
                lyrics = download(url, artist, title);
                break;
            default: // just tags
                artist = params[0];
                title = params[1];
                lyrics = download(params[0], params[1]);
        }
        if (lyrics.getFlag() != Lyrics.POSITIVE_RESULT) {
            String[] correction = correctTags(artist, title);
            if (!(correction[0].equals(artist) && correction[1].equals(title)) || url != null) {
                lyrics = download(correction[0], correction[1]);
                lyrics.setOriginalArtist(artist);
                lyrics.setOriginalTitle(title);
            }
        }
        if (lyrics.getArtist() == null) {
            if (artist != null) {
                lyrics.setArtist(artist);
                lyrics.setTitle(title);
            } else {
                lyrics.setArtist("");
                lyrics.setTitle("");
            }
        }
        return lyrics;
    }

    public static String[] correctTags(String artist, String title) {
        if (artist == null || title == null)
            return new String[]{"", ""};
        String correctedArtist = artist.replaceAll("\\(.*\\)", "")
                .replaceAll(" \\- .*", "").trim();
        String correctedTrack = title.replaceAll("\\(.*\\)", "")
                .replaceAll("\\[.*\\]", "").replaceAll(" \\- .*", "").trim();
        String[] separatedArtists = correctedArtist.split(", ");
        correctedArtist = separatedArtists[separatedArtists.length - 1];
        return new String[]{correctedArtist, correctedTrack};
    }
}