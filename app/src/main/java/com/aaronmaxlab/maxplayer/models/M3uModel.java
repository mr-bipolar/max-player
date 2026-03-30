package com.aaronmaxlab.maxplayer.models;

import java.util.HashMap;
import java.util.Map;

public class M3uModel {
    String channelName, channelUrl, channelLogo;
    int   playlistIndex;
    private Map<String, String> subtitles;
    public M3uModel(String channelName, String channelUrl, String channelLogo, int playlistIndex) {
        this.channelName = channelName;
        this.channelUrl = channelUrl;
        this.channelLogo = channelLogo;
        this.subtitles = new HashMap<>();
        this.playlistIndex = playlistIndex;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelUrl() {
        return channelUrl;
    }

    public String getChannelLogo() {
        return channelLogo;
    }

    public  int getPlaylistIndex() {return playlistIndex;}

    public Map<String, String> getSubtitles() {
        return subtitles;
    }

    public void addSubtitle(String lang, String url) {
        subtitles.put(lang, url);
    }

    public boolean hasSubtitles() {
        return !subtitles.isEmpty();
    }

    public M3uModel() {
        subtitles = new HashMap<>();
    }
}
