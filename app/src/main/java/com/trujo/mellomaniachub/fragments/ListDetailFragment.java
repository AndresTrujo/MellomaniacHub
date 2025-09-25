package com.trujo.mellomaniachub.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trujo.mellomaniachub.AlbumDetailActivity;
import com.trujo.mellomaniachub.R;
import com.trujo.mellomaniachub.adapters.AlbumInListAdapter;
import com.trujo.mellomaniachub.models.AlbumInListDisplay;
import com.trujo.mellomaniachub.models.UserAlbum;
import com.trujo.mellomaniachub.viewmodels.ListDetailViewModel;

import java.util.ArrayList;

public class ListDetailFragment extends Fragment implements AlbumInListAdapter.OnAlbumInListInteractionListener {

    private static final String ARG_LIST_ID = "list_id";
    private static final String ARG_LIST_NAME = "list_name";

    private long listId;
    private String initialListName;

    private RecyclerView listDetailRecyclerView;
    private TextView listNameHeaderTextView;
    private ListDetailViewModel listDetailViewModel;
    private AlbumInListAdapter albumInListAdapter;

    public static ListDetailFragment newInstance(long listId, String listName) {
        ListDetailFragment fragment = new ListDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_LIST_ID, listId);
        args.putString(ARG_LIST_NAME, listName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listId = getArguments().getLong(ARG_LIST_ID);
            initialListName = getArguments().getString(ARG_LIST_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_list_detail, container, false);

        listNameHeaderTextView = view.findViewById(R.id.list_detail_name_header);
        listDetailRecyclerView = view.findViewById(R.id.list_detail_recycler_view);

        listNameHeaderTextView.setText(initialListName != null ? initialListName : "Detalle de Lista");
        setupRecyclerView();
        setupViewModel();

        return view;
    }

    private void setupRecyclerView() {
        listDetailRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        albumInListAdapter = new AlbumInListAdapter(new ArrayList<>(), this);
        listDetailRecyclerView.setAdapter(albumInListAdapter);
    }

    private void setupViewModel() {
        if (getActivity() != null) {
            ListDetailViewModel.ListDetailViewModelFactory factory = new ListDetailViewModel.ListDetailViewModelFactory(getActivity().getApplication(), listId);
            listDetailViewModel = new ViewModelProvider(this, factory).get(ListDetailViewModel.class);

            listDetailViewModel.getAlbumList().observe(getViewLifecycleOwner(), albumList -> {
                if (albumList != null) {
                    listNameHeaderTextView.setText(albumList.getListName());
                }
            });

            listDetailViewModel.getAlbumsInList().observe(getViewLifecycleOwner(), albums -> {
                if (albums != null) {
                    albumInListAdapter.setAlbums(albums);
                }
            });
        }
    }

    @Override
    public void onItemClicked(AlbumInListDisplay albumDisplay) {
        if (getContext() == null || albumDisplay.userAlbum == null) return;
        // The UserAlbum object (albumDisplay.userAlbum) contains the general album details,
        // including its general rating and review, used by AlbumDetailActivity.
        UserAlbum userAlbumForDetail = albumDisplay.userAlbum;
        Intent intent = new Intent(getContext(), AlbumDetailActivity.class);
        intent.putExtra("album", userAlbumForDetail);
        startActivity(intent);
    }

    @Override
    public void onRemoveClicked(AlbumInListDisplay albumDisplay) {
        new AlertDialog.Builder(getContext())
            .setTitle("Quitar Álbum")
            .setMessage("¿Estás seguro de que quieres quitar '" + albumDisplay.getAlbumName() + "' de esta lista?")
            .setPositiveButton("Quitar", (dialog, which) -> {
                listDetailViewModel.removeAlbumFromList(albumDisplay.getAlbumApiId());
                Toast.makeText(getContext(), albumDisplay.getAlbumName() + " quitado de la lista.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    // Displays a dialog to edit the review specific to this album within this list.
    // This method could be invoked by a dedicated "edit review" button in the list item layout if desired.
    private void showEditReviewDialog(AlbumInListDisplay albumDisplay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Editar Reseña para " + albumDisplay.getAlbumName() + " (en esta lista)");

        final EditText inputReview = new EditText(getContext());
        inputReview.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        inputReview.setHint("Escribe tu reseña aquí (para esta lista)...");
        inputReview.setText(albumDisplay.getUserReview()); // Displays the current list-specific review.
        inputReview.setMinLines(3);
        inputReview.setHorizontallyScrolling(true);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);
        layout.addView(inputReview);
        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String reviewText = inputReview.getText().toString().trim();
            if (listDetailViewModel != null && albumDisplay != null) {
                listDetailViewModel.updateAlbumReviewInList(albumDisplay.getAlbumApiId(), reviewText);
                Toast.makeText(getContext(), "Reseña para esta lista guardada.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
