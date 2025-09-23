package com.trujo.mellomaniachub.models;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class AlbumResponse {
    @SerializedName("album")
    private List<Album> albums;

    public List<Album> getAlbums() {
        return albums != null ? albums : Collections.emptyList();
    }
}
