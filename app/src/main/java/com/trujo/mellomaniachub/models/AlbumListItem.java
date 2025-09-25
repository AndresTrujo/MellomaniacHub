package com.trujo.mellomaniachub.models;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "album_list_items",
        foreignKeys = @ForeignKey(entity = AlbumList.class,
                                   parentColumns = "listId",
                                   childColumns = "list_id",
                                   onDelete = ForeignKey.CASCADE),
        indices = {@Index("list_id"), @Index("albumApiId")})
public class AlbumListItem {
    @PrimaryKey(autoGenerate = true)
    public long albumListItemId;

    @ColumnInfo(name = "list_id")
    public long listId; // Foreign key to AlbumList

    public String albumApiId; // Corresponds to UserAlbum.idAlbum

    // User-specific review for this album in this list
    public String userReview;

    public AlbumListItem(long listId, String albumApiId, String userReview) {
        this.listId = listId;
        this.albumApiId = albumApiId;
        this.userReview = userReview;
    }

    // Getters and Setters
    public long getAlbumListItemId() {
        return albumListItemId;
    }

    public void setAlbumListItemId(long albumListItemId) {
        this.albumListItemId = albumListItemId;
    }

    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public String getAlbumApiId() {
        return albumApiId;
    }

    public void setAlbumApiId(String albumApiId) {
        this.albumApiId = albumApiId;
    }

    public String getUserReview() {
        return userReview;
    }

    public void setUserReview(String userReview) {
        this.userReview = userReview;
    }
}
