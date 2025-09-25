package com.trujo.mellomaniachub.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.trujo.mellomaniachub.R;
import com.trujo.mellomaniachub.adapters.AlbumListAdapter;
import com.trujo.mellomaniachub.models.AlbumList;
import com.trujo.mellomaniachub.viewmodels.ListsViewModel;

import java.util.ArrayList;

public class ListsFragment extends Fragment implements AlbumListAdapter.OnListLongClickListener { // Implementar la nueva interfaz

    private RecyclerView listsRecyclerView;
    private FloatingActionButton fabAddList;
    private ListsViewModel listsViewModel;
    private AlbumListAdapter albumListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lists, container, false);

        listsRecyclerView = view.findViewById(R.id.lists_recycler_view);
        fabAddList = view.findViewById(R.id.fab_add_list);

        setupRecyclerView();
        setupViewModel();

        fabAddList.setOnClickListener(v -> showCreateListDialog());

        return view;
    }

    private void setupRecyclerView() {
        listsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        // Pasar 'this' como el OnListLongClickListener ya que la clase ahora lo implementa
        albumListAdapter = new AlbumListAdapter(new ArrayList<>(), this::onListClicked, this);
        listsRecyclerView.setAdapter(albumListAdapter);
    }

    private void setupViewModel() {
        listsViewModel = new ViewModelProvider(this).get(ListsViewModel.class);
        listsViewModel.getAllLists().observe(getViewLifecycleOwner(), lists -> {
            if (lists != null) {
                albumListAdapter.setLists(lists);
            }
        });
    }

    private void showCreateListDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Crear Nueva Lista");

        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setHint("Nombre de la lista");
        builder.setView(input);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String listName = input.getText().toString().trim();
            if (!listName.isEmpty()) {
                listsViewModel.createList(new AlbumList(listName));
                Toast.makeText(getContext(), "Lista '" + listName + "' creada.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "El nombre de la lista no puede estar vacío", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void onListClicked(AlbumList albumList) {
        if (getActivity() != null) {
            ListDetailFragment detailFragment = ListDetailFragment.newInstance(albumList.getListId(), albumList.getListName());
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, detailFragment, "ListDetailFragment");
            fragmentTransaction.addToBackStack("ListsFragmentToListDetailFragment");
            fragmentTransaction.commit();
        }
    }

    // Nueva implementación del método de la interfaz OnListLongClickListener
    @Override
    public void onListLongClick(AlbumList albumList) {
        new AlertDialog.Builder(getContext())
            .setTitle("Eliminar Lista")
            .setMessage("¿Estás seguro de que quieres eliminar la lista '" + albumList.getListName() + "'? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar", (dialog, which) -> {
                listsViewModel.deleteList(albumList);
                Toast.makeText(getContext(), "Lista '" + albumList.getListName() + "' eliminada.", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancelar", null)
            .show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
