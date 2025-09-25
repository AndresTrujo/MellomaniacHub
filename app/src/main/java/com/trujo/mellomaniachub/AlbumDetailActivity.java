package com.trujo.mellomaniachub;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.trujo.mellomaniachub.fragments.AddToListDialogFragment; // Import the dialog
import com.trujo.mellomaniachub.models.AppDatabase;
import com.trujo.mellomaniachub.models.UserAlbum;

public class AlbumDetailActivity extends AppCompatActivity {

    private ImageView albumImageDetail;
    private TextView albumTitleDetail;
    private TextView artistNameDetail;
    private TextView albumReleaseYearDetail;
    private RatingBar userRatingBar;
    private EditText reviewEditText;
    private Button saveButton;

    private AppDatabase db;
    private UserAlbum currentAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_detail_activity);

        albumImageDetail = findViewById(R.id.album_image_detail);
        albumTitleDetail = findViewById(R.id.album_title_detail);
        artistNameDetail = findViewById(R.id.artist_name_detail);
        albumReleaseYearDetail = findViewById(R.id.album_release_year_detail);
        userRatingBar = findViewById(R.id.user_rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        saveButton = findViewById(R.id.save_button);

        db = AppDatabase.getDatabase(this);

        if (getIntent().getSerializableExtra("album") != null) {
            currentAlbum = (UserAlbum) getIntent().getSerializableExtra("album");
            // Load existing details if available from DB, otherwise use passed details
            new LoadAlbumFromDbTask().execute(currentAlbum.idAlbum);
        } else {
            Toast.makeText(this, "Error al cargar los detalles del álbum.", Toast.LENGTH_SHORT).show();
            finish();
        }

        saveButton.setOnClickListener(v -> saveDetailsAndShowListDialog());
    }

    private void displayAlbumDetails(UserAlbum album) {
        if (album == null) {
            Toast.makeText(this, "Error: Álbum no encontrado.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentAlbum = album; // Update currentAlbum with the one from DB or initial one

        albumTitleDetail.setText(album.albumName);
        artistNameDetail.setText(album.artistName);

        if (album.yearReleased != null && !album.yearReleased.isEmpty()) {
            albumReleaseYearDetail.setText(album.yearReleased);
        } else {
            albumReleaseYearDetail.setText("Año Desconocido");
        }

        userRatingBar.setRating(album.userRating); // This will be 0 if new from search, or actual if from DB
        reviewEditText.setText(album.userReview); // Empty if new, or actual if from DB

        if (album.albumThumb != null && !album.albumThumb.isEmpty()) {
            Picasso.get().load(album.albumThumb).placeholder(R.drawable.ic_launcher_background).into(albumImageDetail);
        } else {
            albumImageDetail.setImageResource(R.drawable.ic_launcher_background);
        }
    }

    private void saveDetailsAndShowListDialog() {
        if (currentAlbum == null) {
            Toast.makeText(this, "Error: No hay álbum para guardar.", Toast.LENGTH_SHORT).show();
            return;
        }

        currentAlbum.userRating = userRatingBar.getRating();
        currentAlbum.userReview = reviewEditText.getText().toString().trim();
        // Determine status based on interaction
        if (currentAlbum.userRating > 0 || (currentAlbum.userReview != null && !currentAlbum.userReview.isEmpty())) {
            currentAlbum.status = "listened"; // Or "rated", "reviewed"
        } else if (currentAlbum.status == null || currentAlbum.status.isEmpty() || currentAlbum.status.equals("to-listen")){
            // Keep "to-listen" if no rating/review, or set a default if completely new
             currentAlbum.status = "to-listen"; // Or some other default if preferred 
        }

        new UpsertAlbumAndShowDialogTask().execute(currentAlbum);
    }

    private class LoadAlbumFromDbTask extends AsyncTask<String, Void, UserAlbum> {
        @Override
        protected UserAlbum doInBackground(String... ids) {
            UserAlbum albumFromDb = db.albumDao().getAlbumById(ids[0]);
            if (albumFromDb != null) {
                return albumFromDb;
            }
            return currentAlbum; // Return the one passed in intent if not in DB (first time from search)
        }

        @Override
        protected void onPostExecute(UserAlbum album) {
            displayAlbumDetails(album);
        }
    }

    private class UpsertAlbumAndShowDialogTask extends AsyncTask<UserAlbum, Void, UserAlbum> {
        @Override
        protected UserAlbum doInBackground(UserAlbum... userAlbums) {
            UserAlbum albumToUpsert = userAlbums[0];
            db.albumDao().upsertAlbum(albumToUpsert);
            // It's good practice to fetch the album again from DB to ensure all fields are fresh,
            // especially if DB triggers or defaults modify it.
            // UserAlbum persistedAlbum = db.albumDao().getAlbumById(albumToUpsert.idAlbum);
            // return persistedAlbum != null ? persistedAlbum : albumToUpsert;
            return albumToUpsert; // Return the same for simplicity, assuming upsert doesn't change it significantly beyond input.
        }

        @Override
        protected void onPostExecute(UserAlbum savedAlbum) {
            if (savedAlbum != null) {
                Toast.makeText(AlbumDetailActivity.this, "Detalles del álbum guardados.", Toast.LENGTH_SHORT).show();
                FragmentManager fm = getSupportFragmentManager();
                AddToListDialogFragment dialogFragment = AddToListDialogFragment.newInstance(savedAlbum);
                dialogFragment.show(fm, "AddToListDialogFragment");
                // No longer finishing the activity here, user will dismiss the dialog or add to list.
            } else {
                Toast.makeText(AlbumDetailActivity.this, "Error al guardar detalles del álbum.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
