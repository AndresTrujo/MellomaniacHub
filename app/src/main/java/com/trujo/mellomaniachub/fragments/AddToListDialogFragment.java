package com.trujo.mellomaniachub.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trujo.mellomaniachub.R;
import com.trujo.mellomaniachub.adapters.AlbumListAdapter;
import com.trujo.mellomaniachub.models.AlbumList;
import com.trujo.mellomaniachub.models.AlbumListItem;
import com.trujo.mellomaniachub.models.AppDatabase;
import com.trujo.mellomaniachub.models.UserAlbum;
import com.trujo.mellomaniachub.viewmodels.ListsViewModel;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddToListDialogFragment extends DialogFragment {

    private static final String ARG_USER_ALBUM = "user_album";

    private UserAlbum userAlbumToAdd;
    private RecyclerView existingListsRecyclerView;
    private AlbumListAdapter albumListAdapter;
    private ListsViewModel listsViewModel;
    private AppDatabase db;
    private ExecutorService executorService;

    public static AddToListDialogFragment newInstance(UserAlbum userAlbum) {
        AddToListDialogFragment fragment = new AddToListDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_USER_ALBUM, userAlbum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userAlbumToAdd = (UserAlbum) getArguments().getSerializable(ARG_USER_ALBUM);
        }
        // It's generally better to get context in/after onCreateView for DialogFragments when interacting with UI or needing a fully initialized Context.
        // However, for db and executorService initialization, onCreate is acceptable.
        db = AppDatabase.getDatabase(requireContext()); 
        executorService = Executors.newSingleThreadExecutor();
        listsViewModel = new ViewModelProvider(requireActivity()).get(ListsViewModel.class);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_to_list, null);

        TextView albumNameTextView = view.findViewById(R.id.album_to_add_name_text_view);
        existingListsRecyclerView = view.findViewById(R.id.existing_lists_recycler_view);
        Button createNewListButton = view.findViewById(R.id.button_create_new_list_and_add);
        Button cancelButton = view.findViewById(R.id.button_cancel_add_to_list);

        if (userAlbumToAdd != null) {
            albumNameTextView.setText("Álbum: " + userAlbumToAdd.albumName);
        } else {
            albumNameTextView.setText("Error: No se especificó el álbum.");
            createNewListButton.setEnabled(false);
        }

        setupRecyclerView();
        observeLists();

        createNewListButton.setOnClickListener(v -> {
            if (userAlbumToAdd == null) return;
            showCreateNewListDialog();
        });

        cancelButton.setOnClickListener(v -> dismiss());

        builder.setView(view);
        return builder.create();
    }

    private void setupRecyclerView() {
        existingListsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // The third lambda for onListLongClicked is empty as it's not used here.
        albumListAdapter = new AlbumListAdapter(new ArrayList<>(), this::onListSelected, albumList -> {}); 
        existingListsRecyclerView.setAdapter(albumListAdapter);
    }

    private void observeLists() {
        listsViewModel.getAllLists().observe(this, lists -> {
            if (lists != null) {
                albumListAdapter.setLists(lists);
            }
        });
    }

    // Called when an existing list is selected from the RecyclerView.
    private void onListSelected(AlbumList selectedList) {
        if (userAlbumToAdd == null) {
            Toast.makeText(getContext(), "Error: No hay álbum para añadir.", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }
        // Adds the album to the selected list, using the album's general review as the initial list-specific review.
        addAlbumToSpecificList(selectedList.getListId(), selectedList.getListName(), userAlbumToAdd, userAlbumToAdd.userReview);
    }

    // Shows a dialog to create a new list and add the current album to it.
    private void showCreateNewListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Crear Nueva Lista");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nombre de la nueva lista");
        builder.setView(input);

        builder.setPositiveButton("Crear y Añadir Álbum", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            if (!listName.isEmpty()) {
                AlbumList newList = new AlbumList(listName);
                executorService.execute(() -> {
                    long newListId = db.albumListDao().insertAlbumList(newList);
                    if (newListId > 0 && userAlbumToAdd != null) {
                        // Adds the album to the newly created list.
                        addAlbumToSpecificList(newListId, listName, userAlbumToAdd, userAlbumToAdd.userReview);
                    } else {
                         requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error al crear la nueva lista o álbum no disponible.", Toast.LENGTH_SHORT).show());
                    }
                });
            } else {
                Toast.makeText(getContext(), "El nombre de la lista no puede estar vacío.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    // Adds the specified album to the specified list with the given review.
    // Checks for duplicates before inserting.
    private void addAlbumToSpecificList(long listId, String listName, UserAlbum album, String review) {
        executorService.execute(() -> {
            AlbumListItem existingItem = db.albumListItemDao().getAlbumListItemByApiIdSync(listId, album.idAlbum);
            if (existingItem != null) {
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "'" + album.albumName + "' ya existe en la lista '" + listName + "'.", Toast.LENGTH_LONG).show();
                });
            } else {
                AlbumListItem newListItem = new AlbumListItem(listId, album.idAlbum, review);
                db.albumListItemDao().insertAlbumListItem(newListItem);
                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(getContext(), "'" + album.albumName + "' añadido a '" + listName + "'.", Toast.LENGTH_LONG).show();
                    dismiss(); 
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
