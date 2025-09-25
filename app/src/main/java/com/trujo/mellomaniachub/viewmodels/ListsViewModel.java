package com.trujo.mellomaniachub.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.trujo.mellomaniachub.models.AlbumList;
import com.trujo.mellomaniachub.models.AlbumListDao;
import com.trujo.mellomaniachub.models.AppDatabase;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListsViewModel extends AndroidViewModel {

    private AlbumListDao albumListDao;
    private LiveData<List<AlbumList>> allLists;
    private ExecutorService executorService;

    public ListsViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        albumListDao = db.albumListDao();
        allLists = albumListDao.getAllAlbumLists();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<AlbumList>> getAllLists() {
        return allLists;
    }

    public void createList(AlbumList albumList) {
        executorService.execute(() -> {
            albumListDao.insertAlbumList(albumList);
        });
    }

    public void deleteList(AlbumList albumList) { // Nuevo método
        executorService.execute(() -> {
            albumListDao.deleteAlbumList(albumList);
        });
    }

    // Podrías agregar un método para update si es necesario
    // public void updateList(AlbumList albumList) { ... }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
