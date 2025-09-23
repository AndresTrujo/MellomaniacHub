package com.trujo.mellomaniachub.models;
import com.google.gson.annotations.SerializedName;

public class Album {
    @SerializedName("idAlbum")
    private String idAlbum;

    @SerializedName("strAlbum")
    private String albumName;

    @SerializedName("strGenre")
    private String genre;

    @SerializedName("intYearReleased")
    private String yearReleased;

    @SerializedName("strArtist")
    private String artistName;

    @SerializedName("strAlbumThumb")
    private String albumThumb;

    // Getters
    public String getIdAlbum(){
        return idAlbum;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getYearReleased() {
        return yearReleased;
    }

    public String getAlbumThumb() {
        return albumThumb;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
