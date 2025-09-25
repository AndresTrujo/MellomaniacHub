package com.trujo.mellomaniachub.models;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface AlbumListItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAlbumListItem(AlbumListItem albumListItem);

    // General update method for an AlbumListItem. Currently updates the entire entity.
    @Update
    void updateAlbumListItem(AlbumListItem albumListItem);

    @Delete
    void deleteAlbumListItem(AlbumListItem albumListItem);

    // Retrieves all AlbumListItems for a specific list, ordered by their ID.
    @Query("SELECT * FROM album_list_items WHERE list_id = :listId ORDER BY albumListItemId ASC")
    LiveData<List<AlbumListItem>> getAlbumListItemsByListId(long listId);

    // Retrieves a specific AlbumListItem by listId and albumApiId, as LiveData for observation.
    @Query("SELECT * FROM album_list_items WHERE list_id = :listId AND albumApiId = :albumApiId LIMIT 1")
    LiveData<AlbumListItem> getAlbumListItemByApiId(long listId, String albumApiId);

    // Synchronous version of getAlbumListItemByApiId for direct checks (e.g., in background threads).
    @Query("SELECT * FROM album_list_items WHERE list_id = :listId AND albumApiId = :albumApiId LIMIT 1")
    AlbumListItem getAlbumListItemByApiIdSync(long listId, String albumApiId);

    // Deletes an AlbumListItem by its listId and albumApiId.
    @Query("DELETE FROM album_list_items WHERE list_id = :listId AND albumApiId = :albumApiId")
    void deleteAlbumListItemByApiId(long listId, String albumApiId);

    // Retrieves combined album details (from UserAlbum) and list-specific details (review from AlbumListItem)
    // for display within a specific list.
    @Transaction
    @Query("SELECT ua.*, " +
           "ali.albumListItemId AS list_item_id, " +
           "ali.list_id AS list_specific_list_id, " +
           "ali.userReview AS list_specific_user_review " +
           "FROM album_list_items ali " +
           "JOIN user_albums ua ON ali.albumApiId = ua.idAlbum " +
           "WHERE ali.list_id = :listId " +
           "ORDER BY ua.albumName ASC")
    LiveData<List<AlbumInListDisplay>> getAlbumInListDisplayItems(long listId);

    // Updates only the userReview for a specific AlbumListItem.
    @Query("UPDATE album_list_items SET userReview = :review WHERE list_id = :listId AND albumApiId = :albumApiId")
    void updateReviewForListItem(long listId, String albumApiId, String review);
}
