package com.aaronmaxlab.maxplayer.subclass;

import com.aaronmaxlab.maxplayer.models.M3uModel;

public interface ChannelSelect {
    void onChannelClicked(M3uModel m3uModel, int position);
    void onSubClicked(M3uModel m3uModel);
}
