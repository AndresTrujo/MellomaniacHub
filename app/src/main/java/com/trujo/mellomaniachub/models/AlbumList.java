package com.trujo.mellomaniachub.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "album_lists")
public class AlbumList {
    @PrimaryKey(autoGenerate = true)
    public long listId;

    public String listName;

    // Constructor vacío requerido por Room
    public AlbumList() {}

    public AlbumList(String listName) {
        this.listName = listName;
    }

    // Getters y Setters (Room puede acceder a campos públicos, pero es buena práctica tenerlos)
    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }
}
