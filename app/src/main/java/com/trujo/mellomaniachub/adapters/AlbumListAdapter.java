package com.trujo.mellomaniachub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.trujo.mellomaniachub.R;
import com.trujo.mellomaniachub.models.AlbumList;
import java.util.List;

public class AlbumListAdapter extends RecyclerView.Adapter<AlbumListAdapter.AlbumListViewHolder> {

    private List<AlbumList> albumLists;
    private final OnListClickListener clickListener;
    private final OnListLongClickListener longClickListener; // Nuevo listener

    public interface OnListClickListener {
        void onListClick(AlbumList albumList);
    }

    public interface OnListLongClickListener { // Nueva interfaz
        void onListLongClick(AlbumList albumList);
    }

    public AlbumListAdapter(List<AlbumList> albumLists, OnListClickListener clickListener, OnListLongClickListener longClickListener) {
        this.albumLists = albumLists;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener; // Asignar nuevo listener
    }

    @NonNull
    @Override
    public AlbumListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_custom_list, parent, false);
        return new AlbumListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumListViewHolder holder, int position) {
        AlbumList currentList = albumLists.get(position);
        holder.listNameTextView.setText(currentList.getListName());
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onListClick(currentList);
            }
        });
        holder.itemView.setOnLongClickListener(v -> { // Nuevo listener de pulsación larga
            if (longClickListener != null) {
                longClickListener.onListLongClick(currentList);
                return true; // Indica que el evento ha sido consumido
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return albumLists == null ? 0 : albumLists.size();
    }

    public void setLists(List<AlbumList> newAlbumLists) {
        this.albumLists = newAlbumLists;
        notifyDataSetChanged(); // Se podría optimizar con DiffUtil si las listas son muy grandes
    }

    static class AlbumListViewHolder extends RecyclerView.ViewHolder {
        TextView listNameTextView;

        public AlbumListViewHolder(@NonNull View itemView) {
            super(itemView);
            listNameTextView = itemView.findViewById(R.id.list_name_text_view);
        }
    }
}
