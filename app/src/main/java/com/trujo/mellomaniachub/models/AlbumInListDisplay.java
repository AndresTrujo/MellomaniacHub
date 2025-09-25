package com.trujo.mellomaniachub.models;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

// POJO to combine UserAlbum with AlbumListItem details for display in a list.
public class AlbumInListDisplay {

    @Embedded
    public UserAlbum userAlbum; // Contains all general album details, including the general userRating.

    // ID of the AlbumListItem entry in the database.
    @ColumnInfo(name = "list_item_id")
    public long listItemId;

    // The list_id this item specifically belongs to.
    @ColumnInfo(name = "list_specific_list_id")
    public long listSpecificListId;

    // The review specific to this album within this particular list.
    @ColumnInfo(name = "list_specific_user_review")
    public String listSpecificUserReview;

    // Helper getters for easier access in adapters.
    public String getAlbumName() {
        return userAlbum != null ? userAlbum.albumName : "N/A";
    }

    public String getArtistName() {
        return userAlbum != null ? userAlbum.artistName : "N/A";
    }

    public String getYearReleased() {
        return userAlbum != null ? userAlbum.yearReleased : "N/A";
    }

    public String getAlbumThumb() {
        return userAlbum != null ? userAlbum.albumThumb : null;
    }

    // Returns the general rating of the album from UserAlbum.
    public float getUserRating() {
        return userAlbum != null ? userAlbum.userRating : 0.0f;
    }

    // Returns the review specific to this album in this list.
    public String getUserReview() {
        return listSpecificUserReview;
    }

    public long getListItemId() {
        return listItemId;
    }

    public String getAlbumApiId() {
        return userAlbum != null ? userAlbum.idAlbum : null;
    }
}
