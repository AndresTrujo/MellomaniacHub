package com.trujo.mellomaniachub;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import com.squareup.picasso.Picasso;
import com.trujo.mellomaniachub.models.AppDatabase;
import com.trujo.mellomaniachub.models.UserAlbum;

public class AlbumDetailActivity extends AppCompatActivity {

    private ImageView albumImageDetail;
    private TextView albumTitleDetail;
    private TextView artistNameDetail;
    private RatingBar userRatingBar;
    private EditText reviewEditText;
    private Button saveButton;

    private AppDatabase db;
    private UserAlbum currentAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.album_detail_activity);

        // Inicializar vistas
        albumImageDetail = findViewById(R.id.album_image_detail);
        albumTitleDetail = findViewById(R.id.album_title_detail);
        artistNameDetail = findViewById(R.id.artist_name_detail);
        userRatingBar = findViewById(R.id.user_rating_bar);
        reviewEditText = findViewById(R.id.review_edit_text);
        saveButton = findViewById(R.id.save_button);

        // Inicializar la base de datos
        db = AppDatabase.getDatabase(this);

        // Obtener el objeto Album del Intent
        if (getIntent().getSerializableExtra("album") != null) {
            currentAlbum = (UserAlbum) getIntent().getSerializableExtra("album");
            displayAlbumDetails(currentAlbum);
        } else {
            Toast.makeText(this, "Error al cargar los detalles del álbum.", Toast.LENGTH_SHORT).show();
            finish(); // Cierra la actividad si no se recibió un álbum
        }

        // Listener del botón de guardar
        saveButton.setOnClickListener(v -> saveAlbumReview());
    }

    private void displayAlbumDetails(UserAlbum album) {
        // Mostrar los detalles del álbum en las vistas
        albumTitleDetail.setText(album.albumName);
        artistNameDetail.setText(album.artistName);
        userRatingBar.setRating(album.userRating);
        reviewEditText.setText(album.userReview);

        if (album.albumThumb != null && !album.albumThumb.isEmpty()) {
            Picasso.get().load(album.albumThumb).into(albumImageDetail);
        } else {
            albumImageDetail.setImageResource(R.drawable.ic_launcher_background); // Placeholder
        }
    }

    private void saveAlbumReview() {
        // Actualizar el objeto con la puntuación y reseña del usuario
        currentAlbum.userRating = userRatingBar.getRating();
        currentAlbum.userReview = reviewEditText.getText().toString().trim();
        currentAlbum.status = "listened"; // Marcar como escuchado

        // Guardar en la base de datos en un hilo de fondo
        new UpdateAlbumTask().execute(currentAlbum);
    }

    // AsyncTask para actualizar la base de datos
    private class UpdateAlbumTask extends AsyncTask<UserAlbum, Void, Void> {
        @Override
        protected Void doInBackground(UserAlbum... userAlbums) {
            UserAlbum albumToUpdate = userAlbums[0];
            db.albumDao().updateAlbum(albumToUpdate.idAlbum, albumToUpdate.userRating, albumToUpdate.userReview);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(AlbumDetailActivity.this, "¡Reseña guardada con éxito!", Toast.LENGTH_SHORT).show();
            finish(); // Vuelve a la actividad anterior
        }
    }
}