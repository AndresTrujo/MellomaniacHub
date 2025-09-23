package com.trujo.mellomaniachub.models;

import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;

public class ArtistReponse {
    @SerializedName("artists")
    private List<Artist> artists;

    public List<Artist> getArtists() {
        return artists != null ? artists : Collections.emptyList();
    }
}
