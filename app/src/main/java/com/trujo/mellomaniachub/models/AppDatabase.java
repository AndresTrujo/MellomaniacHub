package com.trujo.mellomaniachub.models;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UserAlbum.class, AlbumList.class, AlbumListItem.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract AlbumDao albumDao();
    public abstract AlbumListDao albumListDao();
    public abstract AlbumListItemDao albumListItemDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "music_database")
                            .fallbackToDestructiveMigration() // Destroys and recreates database on version upgrade if no migration found
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
