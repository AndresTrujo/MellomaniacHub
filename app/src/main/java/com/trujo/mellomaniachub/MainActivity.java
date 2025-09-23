package com.trujo.mellomaniachub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.trujo.mellomaniachub.models.Album;
import com.trujo.mellomaniachub.models.AlbumResponse;
import com.trujo.mellomaniachub.models.AppDatabase;
import com.trujo.mellomaniachub.models.ArtistReponse;
import com.trujo.mellomaniachub.models.UserAlbum;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView albumsRecyclerView;
    private TheAudioDBService apiService;
    private AlbumsAdapter albumsAdapter;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);
        albumsRecyclerView = findViewById(R.id.albums_recycler_view);

        // Configurar RecyclerView
        albumsAdapter = new AlbumsAdapter(new ArrayList<>());
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        albumsRecyclerView.setAdapter(albumsAdapter);

        // Configurar Retrofit para la API
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheAudioDBService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(TheAudioDBService.class);

        // Inicializar la base de datos de Room
        db = AppDatabase.getDatabase(this);

        // Configurar listener del botón de búsqueda
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String artistName = searchEditText.getText().toString().trim();
                if (!artistName.isEmpty()) {
                    searchArtists(artistName);
                } else {
                    Toast.makeText(MainActivity.this, "Por favor, ingresa un artista", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchArtists(String artistName) {
        // Llamar a la API para buscar artistas
        apiService.searchArtists(artistName).enqueue(new Callback<ArtistReponse>() {
            @Override
            public void onResponse(Call<ArtistReponse> call, Response<ArtistReponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getArtists() != null && !response.body().getArtists().isEmpty()) {
                    String artistId = response.body().getArtists().get(0).getIdArtist();
                    getAlbumsByArtistId(artistId);
                } else {
                    Toast.makeText(MainActivity.this, "Artista no encontrado", Toast.LENGTH_SHORT).show();
                    albumsAdapter.setAlbums(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ArtistReponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error de red: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                albumsAdapter.setAlbums(new ArrayList<>());
            }
        });
    }

    private void getAlbumsByArtistId(String artistId) {
        // Llamar a la API para obtener los álbumes del artista
        apiService.getAlbumsByArtistId(artistId).enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getAlbums() != null && !response.body().getAlbums().isEmpty()) {
                    albumsAdapter.setAlbums(response.body().getAlbums());
                } else {
                    Toast.makeText(MainActivity.this, "No se encontraron álbumes para este artista.", Toast.LENGTH_SHORT).show();
                    albumsAdapter.setAlbums(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<AlbumResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Error al obtener los álbumes: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                albumsAdapter.setAlbums(new ArrayList<>());
            }
        });
    }

    // Adaptador para el RecyclerView
    private class AlbumsAdapter extends RecyclerView.Adapter<AlbumsAdapter.AlbumViewHolder> {
        private List<Album> albums;

        public AlbumsAdapter(List<Album> albums) {
            this.albums = albums;
        }

        public void setAlbums(List<Album> newAlbums) {
            this.albums = newAlbums;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
            return new AlbumViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
            Album album = albums.get(position);
            holder.albumName.setText(album.getAlbumName());
            holder.artistName.setText(album.getArtistName());
            holder.yearReleased.setText(album.getYearReleased());
            if (album.getAlbumThumb() != null && !album.getAlbumThumb().isEmpty()) {
                Picasso.get().load(album.getAlbumThumb()).into(holder.albumImage);
            } else {
                holder.albumImage.setImageResource(R.drawable.ic_launcher_background); // Placeholder
            }

            // Listener para agregar el álbum a la base de datos
            holder.addButton.setOnClickListener(v -> {
                // Convertir el objeto de la API a una entidad de Room
                UserAlbum userAlbum = new UserAlbum();
                userAlbum.idAlbum = album.getIdAlbum();
                userAlbum.albumName = album.getAlbumName();
                userAlbum.artistName = album.getArtistName();
                userAlbum.genre = album.getGenre();
                userAlbum.yearReleased = album.getYearReleased();
                userAlbum.albumThumb = album.getAlbumThumb();
                userAlbum.status = "to-listen";

                // Insertar en la base de datos en un hilo de fondo
                new InsertAlbumTask().execute(userAlbum);
            });

            // Listener para abrir la vista de detalle
            holder.itemView.setOnClickListener(v -> {
                // Prepara el objeto UserAlbum para la vista de detalle
                UserAlbum userAlbum = new UserAlbum();
                userAlbum.idAlbum = album.getIdAlbum();
                userAlbum.albumName = album.getAlbumName();
                userAlbum.artistName = album.getArtistName();
                userAlbum.genre = album.getGenre();
                userAlbum.yearReleased = album.getYearReleased();
                userAlbum.albumThumb = album.getAlbumThumb();
                userAlbum.status = "to-listen";

                Intent intent = new Intent(MainActivity.this, AlbumDetailActivity.class);
                intent.putExtra("album", userAlbum);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return albums.size();
        }

        class AlbumViewHolder extends RecyclerView.ViewHolder {
            ImageView albumImage;
            TextView albumName;
            TextView artistName;
            TextView yearReleased;
            Button addButton;

            public AlbumViewHolder(@NonNull View itemView) {
                super(itemView);
                albumImage = itemView.findViewById(R.id.album_image);
                albumName = itemView.findViewById(R.id.album_name);
                artistName = itemView.findViewById(R.id.artist_name);
                yearReleased = itemView.findViewById(R.id.year_released);
                addButton = itemView.findViewById(R.id.add_button);
            }
        }
    }

    // AsyncTask para insertar en la base de datos
    private class InsertAlbumTask extends AsyncTask<UserAlbum, Void, Void> {
        @Override
        protected Void doInBackground(UserAlbum... userAlbums) {
            UserAlbum album = userAlbums[0];
            db.albumDao().insertAlbum(album);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Toast.makeText(MainActivity.this, "Álbum agregado a tu lista!", Toast.LENGTH_SHORT).show();
        }
    }
}