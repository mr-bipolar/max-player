package com.aaronmaxlab.maxplayer.models;

public class PlaylistModel {

    private long id;
    private String name;
    private String url;
    private int count;

    public PlaylistModel(long id, String name, String url, int count) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.count = count;
    }

    public long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public int getCount() { return count; }
}


