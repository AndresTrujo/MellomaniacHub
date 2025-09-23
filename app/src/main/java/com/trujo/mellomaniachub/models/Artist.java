package com.trujo.mellomaniachub.models;
import com.google.gson.annotations.SerializedName;

public class Artist {
    @SerializedName("idArtist")
    private String idArtist;

    @SerializedName("strArtist")
    private String name;

    @SerializedName("strStyle")
    private String style;

    @SerializedName("strBiography")
    private String biography;

    // Getters
    public String getName() {
        return name;
    }

    public String getIdArtist() {
        return idArtist;
    }

    public String getStyle() {
        return style;
    }

    public String getBiography() {
        return biography;
    }
}
