package com.trujo.mellomaniachub.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlbumDao {
    @Insert
    void insertAlbum(UserAlbum album);
    @Query("SELECT * FROM user_albums WHERE status = 'to-listen'")
    List<UserAlbum> getAlbumsToListen();

    @Query("SELECT * FROM user_albums WHERE status = 'listened'")
    List<UserAlbum> getListenedAlbums();

    @Query("SELECT * FROM user_albums WHERE idAlbum = :albumId LIMIT 1")
    UserAlbum getAlbumById(String albumId);

    @Query("UPDATE user_albums SET userRating = :rating, userReview = :review WHERE idAlbum = :albumId")
    void updateAlbum(String albumId, float rating, String review);

    @Query("DELETE FROM user_albums WHERE idAlbum = :albumId")
    void deleteAlbum(String albumId);
}
