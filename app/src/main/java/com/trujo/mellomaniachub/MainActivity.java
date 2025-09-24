package com.trujo.mellomaniachub;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
import com.trujo.mellomaniachub.models.Artist;
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
    private TheAudioDBService service;
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
        service = retrofit.create(TheAudioDBService.class);

        // Inicializar la base de datos de Room
        db = AppDatabase.getDatabase(this);

        // Configurar listener del botón de búsqueda
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String artistName = searchEditText.getText().toString().trim();
                if (!artistName.isEmpty()) {
                    searchArtist(artistName);
                } else {
                    Toast.makeText(MainActivity.this, "Por favor, ingresa un artista", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void searchArtist(String artistName) {
        service.searchArtists(artistName).enqueue(new Callback<ArtistReponse>() {
            @Override
            public void onResponse(Call<ArtistReponse> call, Response<ArtistReponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artists = response.body().getArtists();
                    if (artists != null && !artists.isEmpty()) {
                        Artist artist = artists.get(0);
                        // Log para depuración, puedes quitarlo si quieres
                        Log.d("MainActivity", "Artista encontrado: " + artist.getName());
                        getAlbumsForArtist(artist.getName()); // Usamos el ID del artista para buscar sus álbumes
                    } else {
                        Log.d("MainActivity", "Artista no encontrado.");
                        Toast.makeText(MainActivity.this, "Artista no encontrado", Toast.LENGTH_SHORT).show();
                        albumsAdapter.setAlbums(new ArrayList<>()); // Limpiar resultados anteriores
                    }
                } else {
                    Log.e("MainActivity", "Error en la respuesta de búsqueda de artista: " + response.code());
                    Toast.makeText(MainActivity.this, "Error al buscar artista", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ArtistReponse> call, Throwable t) {
                Log.e("MainActivity", "Error de red en búsqueda de artista: " + t.getMessage());
                Toast.makeText(MainActivity.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAlbumsForArtist(String artistQuery) { // Cambié el nombre del parámetro para reflejar que es una query (nombre o id según la API)
        Log.d("MainActivity", "Buscando álbumes para la consulta: " + artistQuery);

        // Usaremos fetchAlbumsByArtistQuery que definimos en la interfaz
        service.fetchAlbumsByArtistQuery(artistQuery).enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Album> albums = response.body().getAlbums(); // Esto debería ahora tener la lista completa
                    if (albums != null && !albums.isEmpty()) {
                        Log.d("MainActivity", "Álbumes encontrados: " + albums.size());
                        for (Album album : albums) {
                            Log.d("MainActivity", "Álbum: " + album.getAlbumName() + " - Artista: " + album.getArtistName());
                        }
                        albumsAdapter.setAlbums(albums);
                    } else {
                        Log.d("MainActivity", "No se encontraron álbumes para este artista o la lista está vacía.");
                        Toast.makeText(MainActivity.this, "No se encontraron álbumes", Toast.LENGTH_SHORT).show();
                        albumsAdapter.setAlbums(new ArrayList<>());
                    }
                } else {
                    Log.e("MainActivity", "Error al obtener los álbumes, código: " + response.code() + ", mensaje: " + response.message());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("MainActivity", "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e("MainActivity", "Error al leer errorBody", e);
                    }
                    Toast.makeText(MainActivity.this, "Error al obtener álbumes", Toast.LENGTH_SHORT).show();
                    albumsAdapter.setAlbums(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<AlbumResponse> call, Throwable t) {
                Log.e("MainActivity", "Error de red al obtener álbumes: " + t.getMessage(), t);
                Toast.makeText(MainActivity.this, "Error de red al obtener álbumes", Toast.LENGTH_SHORT).show();
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
            this.albums.clear();
            if (newAlbums != null) {
                this.albums.addAll(newAlbums);
            }
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
                // Considera usar un placeholder más genérico o específico para álbumes sin carátula
                holder.albumImage.setImageResource(R.drawable.ic_launcher_background);
            }

            // Listener para agregar el álbum a la base de datos
            holder.addButton.setOnClickListener(v -> {
                UserAlbum userAlbum = new UserAlbum();
                userAlbum.idAlbum = album.getIdAlbum();
                userAlbum.albumName = album.getAlbumName();
                userAlbum.artistName = album.getArtistName();
                userAlbum.genre = album.getGenre();
                userAlbum.yearReleased = album.getYearReleased();
                userAlbum.albumThumb = album.getAlbumThumb();
                userAlbum.status = "to-listen"; // O el estado inicial que desees

                new InsertAlbumTask().execute(userAlbum);
            });

            // Listener para abrir la vista de detalle
            holder.itemView.setOnClickListener(v -> {
                UserAlbum userAlbum = new UserAlbum();
                userAlbum.idAlbum = album.getIdAlbum();
                userAlbum.albumName = album.getAlbumName();
                userAlbum.artistName = album.getArtistName();
                userAlbum.genre = album.getGenre();
                userAlbum.yearReleased = album.getYearReleased();
                userAlbum.albumThumb = album.getAlbumThumb();

                Intent intent = new Intent(MainActivity.this, AlbumDetailActivity.class);
                intent.putExtra("album", userAlbum); // Asegúrate que UserAlbum sea Parcelable o Serializable
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