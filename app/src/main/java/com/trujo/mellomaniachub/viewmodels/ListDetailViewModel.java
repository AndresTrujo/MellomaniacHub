package com.trujo.mellomaniachub.viewmodels;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.trujo.mellomaniachub.models.AlbumInListDisplay;
import com.trujo.mellomaniachub.models.AlbumList;
import com.trujo.mellomaniachub.models.AlbumListDao;
import com.trujo.mellomaniachub.models.AlbumListItemDao;
import com.trujo.mellomaniachub.models.AppDatabase;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ListDetailViewModel extends AndroidViewModel {

    private final AlbumListItemDao albumListItemDao;
    private final LiveData<AlbumList> albumList; // For displaying list name and other list details.
    private final LiveData<List<AlbumInListDisplay>> albumsInList; // Albums to be displayed in the list.
    private final ExecutorService executorService;
    private final long listId; // ID of the current list being detailed.

    public ListDetailViewModel(@NonNull Application application, long listId) {
        super(application);
        this.listId = listId;
        AppDatabase db = AppDatabase.getDatabase(application);
        this.albumListItemDao = db.albumListItemDao();
        AlbumListDao localAlbumListDao = db.albumListDao(); // For fetching AlbumList details.
        executorService = Executors.newSingleThreadExecutor();

        albumList = localAlbumListDao.getAlbumListById(listId);
        albumsInList = albumListItemDao.getAlbumInListDisplayItems(listId);
    }

    public LiveData<AlbumList> getAlbumList() {
        return albumList;
    }

    public LiveData<List<AlbumInListDisplay>> getAlbumsInList() {
        return albumsInList;
    }

    // Updates the list-specific review for an album in the current list.
    public void updateAlbumReviewInList(String albumApiId, String newReview) {
        executorService.execute(() -> {
            albumListItemDao.updateReviewForListItem(listId, albumApiId, newReview);
        });
    }

    // Removes an album from the current list.
    public void removeAlbumFromList(String albumApiId) {
        executorService.execute(() -> {
            albumListItemDao.deleteAlbumListItemByApiId(listId, albumApiId);
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // Factory for creating ListDetailViewModel with parameters.
    public static class ListDetailViewModelFactory implements ViewModelProvider.Factory {
        private final Application application;
        private final long listId;

        public ListDetailViewModelFactory(Application application, long listId) {
            this.application = application;
            this.listId = listId;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(ListDetailViewModel.class)) {
                return (T) new ListDetailViewModel(application, listId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
