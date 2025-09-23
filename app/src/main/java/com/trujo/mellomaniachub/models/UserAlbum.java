package com.trujo.mellomaniachub.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "user_albums")
public class UserAlbum implements Serializable {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    public String idAlbum = "";
    public String albumName;
    public String artistName;
    public String genre;
    public String yearReleased;
    public String albumThumb;

    public float userRating = 0.0f; // Puntuación del usuario de 0.0 a 5.0
    public String userReview = ""; // Reseña del usuario
    public String status = "to-listen"; // 'to-listen' o 'listened'

    @NonNull
    public String getIdAlbum() {
        return idAlbum;
    }

    public String getAlbumName() {
        return albumName;
    }

    public String getArtistName() {
        return artistName;
    }

    public String getGenre() {
        return genre;
    }

    public String getYearReleased() {
        return yearReleased;
    }

    public String getAlbumThumb() {
        return albumThumb;
    }

    public float getUserRating() {
        return userRating;
    }

    public String getUserReview() {
        return userReview;
    }

    public String getStatus() {
        return status;
    }
}
