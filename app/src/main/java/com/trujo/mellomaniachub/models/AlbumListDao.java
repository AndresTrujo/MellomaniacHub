package com.trujo.mellomaniachub.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlbumListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertAlbumList(AlbumList albumList);

    @Update
    void updateAlbumList(AlbumList albumList);

    @Delete
    void deleteAlbumList(AlbumList albumList);

    @Query("SELECT * FROM album_lists ORDER BY listName ASC")
    LiveData<List<AlbumList>> getAllAlbumLists();

    @Query("SELECT * FROM album_lists WHERE listId = :listId")
    LiveData<AlbumList> getAlbumListById(long listId);
}
