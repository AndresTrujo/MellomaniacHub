package com.trujo.mellomaniachub.models;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AlbumDao {
    // Inserta si es nuevo, reemplaza si ya existe (basado en la PrimaryKey de UserAlbum)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAlbum(UserAlbum album);

    @Query("SELECT * FROM user_albums WHERE status = 'to-listen'")
    List<UserAlbum> getAlbumsToListen();

    @Query("SELECT * FROM user_albums WHERE status = 'listened'")
    List<UserAlbum> getListenedAlbums();

    @Query("SELECT * FROM user_albums WHERE idAlbum = :albumId LIMIT 1")
    UserAlbum getAlbumById(String albumId);

    // Actualiza campos específicos. Considera si upsertAlbum cubre tus necesidades o si este sigue siendo útil.
    @Query("UPDATE user_albums SET userRating = :rating, userReview = :review, status = :status WHERE idAlbum = :albumId")
    void updateAlbumDetails(String albumId, float rating, String review, String status);

    @Query("DELETE FROM user_albums WHERE idAlbum = :albumId")
    void deleteAlbum(String albumId);
}
