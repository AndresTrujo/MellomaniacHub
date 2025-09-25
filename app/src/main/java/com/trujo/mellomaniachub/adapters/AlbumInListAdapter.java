package com.trujo.mellomaniachub.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import com.trujo.mellomaniachub.R;
import com.trujo.mellomaniachub.models.AlbumInListDisplay;
import java.util.List;

public class AlbumInListAdapter extends RecyclerView.Adapter<AlbumInListAdapter.AlbumInListViewHolder> {

    private List<AlbumInListDisplay> albumsInList;
    private final OnAlbumInListInteractionListener listener;

    // Interface for handling interactions with items in the list.
    public interface OnAlbumInListInteractionListener {
        void onItemClicked(AlbumInListDisplay album); // Called when an item view is clicked.
        void onRemoveClicked(AlbumInListDisplay album); // Called when the remove button is clicked.
    }

    public AlbumInListAdapter(List<AlbumInListDisplay> albumsInList, OnAlbumInListInteractionListener listener) {
        this.albumsInList = albumsInList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumInListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album_in_list, parent, false);
        return new AlbumInListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumInListViewHolder holder, int position) {
        AlbumInListDisplay currentAlbumItem = albumsInList.get(position);

        holder.albumNameTextView.setText(currentAlbumItem.getAlbumName());
        holder.artistNameTextView.setText(currentAlbumItem.getArtistName());
        holder.yearTextView.setText(currentAlbumItem.getYearReleased());

        if (currentAlbumItem.getAlbumThumb() != null && !currentAlbumItem.getAlbumThumb().isEmpty()) {
            Picasso.get().load(currentAlbumItem.getAlbumThumb()).placeholder(R.drawable.ic_launcher_background).error(R.drawable.ic_launcher_background).into(holder.albumCoverImageView);
        } else {
            holder.albumCoverImageView.setImageResource(R.drawable.ic_launcher_background);
        }

        // RatingBar is an indicator; it displays the general album rating.
        holder.ratingBar.setRating(currentAlbumItem.getUserRating());

        // Displays the review specific to this album in this list.
        holder.reviewTextView.setText(currentAlbumItem.getUserReview());
        if (currentAlbumItem.getUserReview() == null || currentAlbumItem.getUserReview().isEmpty()) {
            holder.reviewTextView.setVisibility(View.GONE);
        } else {
            holder.reviewTextView.setVisibility(View.VISIBLE);
        }

        // Click listener for the entire item view.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked(currentAlbumItem);
            }
        });

        // Click listener for the remove button.
        holder.removeButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveClicked(currentAlbumItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumsInList == null ? 0 : albumsInList.size();
    }

    public void setAlbums(List<AlbumInListDisplay> newAlbumsInList) {
        this.albumsInList = newAlbumsInList;
        notifyDataSetChanged(); // For simplicity; DiffUtil is preferred for performance.
    }

    static class AlbumInListViewHolder extends RecyclerView.ViewHolder {
        ImageView albumCoverImageView;
        TextView albumNameTextView, artistNameTextView, yearTextView, reviewTextView;
        RatingBar ratingBar;
        ImageButton removeButton;

        public AlbumInListViewHolder(@NonNull View itemView) {
            super(itemView);
            albumCoverImageView = itemView.findViewById(R.id.album_cover_in_list_image_view);
            albumNameTextView = itemView.findViewById(R.id.album_name_in_list_text_view);
            artistNameTextView = itemView.findViewById(R.id.artist_name_in_list_text_view);
            yearTextView = itemView.findViewById(R.id.album_year_in_list_text_view);
            ratingBar = itemView.findViewById(R.id.album_rating_in_list_rating_bar);
            reviewTextView = itemView.findViewById(R.id.album_review_in_list_text_view);
            removeButton = itemView.findViewById(R.id.remove_album_from_list_button);
        }
    }
}
