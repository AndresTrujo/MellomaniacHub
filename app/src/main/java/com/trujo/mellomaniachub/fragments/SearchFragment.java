package com.trujo.mellomaniachub.fragments;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;
import com.trujo.mellomaniachub.R;
import com.trujo.mellomaniachub.TheAudioDBService;
import com.trujo.mellomaniachub.models.Album;
import com.trujo.mellomaniachub.models.AlbumResponse;
import com.trujo.mellomaniachub.models.AppDatabase;
import com.trujo.mellomaniachub.models.Artist;
import com.trujo.mellomaniachub.models.ArtistReponse;
import com.trujo.mellomaniachub.models.UserAlbum;
import com.trujo.mellomaniachub.AlbumDetailActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment {

    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView albumsRecyclerView;
    private TheAudioDBService service;
    private AlbumsAdapter albumsAdapter;
    private AppDatabase db;
    private ExecutorService executorService; // For background tasks

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheAudioDBService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(TheAudioDBService.class);

        db = AppDatabase.getDatabase(requireContext());
        executorService = Executors.newSingleThreadExecutor();

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        albumsRecyclerView = view.findViewById(R.id.albums_recycler_view);

        albumsAdapter = new AlbumsAdapter(new ArrayList<>());
        albumsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        albumsRecyclerView.setAdapter(albumsAdapter);

        searchButton.setOnClickListener(v -> {
            String artistName = searchEditText.getText().toString().trim();
            if (!artistName.isEmpty()) {
                searchArtist(artistName);
            } else {
                Toast.makeText(getContext(), "Por favor, ingresa un artista", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchArtist(String artistName) {
        service.searchArtists(artistName).enqueue(new Callback<ArtistReponse>() {
            @Override
            public void onResponse(@NonNull Call<ArtistReponse> call, @NonNull Response<ArtistReponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artists = response.body().getArtists();
                    if (artists != null && !artists.isEmpty()) {
                        Artist artist = artists.get(0);
                        Log.d("SearchFragment", "Artista encontrado: " + artist.getName());
                        getAlbumsForArtist(artist.getName());
                    } else {
                        Log.d("SearchFragment", "Artista no encontrado.");
                        Toast.makeText(getContext(), "Artista no encontrado", Toast.LENGTH_SHORT).show();
                        albumsAdapter.setAlbums(new ArrayList<>());
                    }
                } else {
                    Log.e("SearchFragment", "Error en la respuesta de búsqueda de artista: " + response.code());
                    Toast.makeText(getContext(), "Error al buscar artista", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ArtistReponse> call, @NonNull Throwable t) {
                Log.e("SearchFragment", "Error de red en búsqueda de artista: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAlbumsForArtist(String artistQuery) {
        service.fetchAlbumsByArtistQuery(artistQuery).enqueue(new Callback<AlbumResponse>() {
            @Override
            public void onResponse(@NonNull Call<AlbumResponse> call, @NonNull Response<AlbumResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Album> albums = response.body().getAlbums();
                    if (albums != null && !albums.isEmpty()) {
                        albumsAdapter.setAlbums(albums);
                    } else {
                        Toast.makeText(getContext(), "No se encontraron álbumes", Toast.LENGTH_SHORT).show();
                        albumsAdapter.setAlbums(new ArrayList<>());
                    }
                } else {
                    Log.e("SearchFragment", "Error al obtener los álbumes, código: " + response.code());
                    Toast.makeText(getContext(), "Error al obtener álbumes", Toast.LENGTH_SHORT).show();
                    albumsAdapter.setAlbums(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(@NonNull Call<AlbumResponse> call, @NonNull Throwable t) {
                Log.e("SearchFragment", "Error de red al obtener álbumes: " + t.getMessage());
                Toast.makeText(getContext(), "Error de red al obtener álbumes", Toast.LENGTH_SHORT).show();
                albumsAdapter.setAlbums(new ArrayList<>());
            }
        });
    }

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
            Album albumFromApi = albums.get(position);
            holder.albumName.setText(albumFromApi.getAlbumName());
            holder.artistName.setText(albumFromApi.getArtistName());
            holder.yearReleased.setText(albumFromApi.getYearReleased());
            if (albumFromApi.getAlbumThumb() != null && !albumFromApi.getAlbumThumb().isEmpty()) {
                Picasso.get().load(albumFromApi.getAlbumThumb()).placeholder(R.drawable.ic_launcher_background).into(holder.albumImage);
            } else {
                holder.albumImage.setImageResource(R.drawable.ic_launcher_background);
            }

            holder.addButton.setOnClickListener(v -> {
                UserAlbum userAlbum = new UserAlbum();
                userAlbum.idAlbum = albumFromApi.getIdAlbum();
                userAlbum.albumName = albumFromApi.getAlbumName();
                userAlbum.artistName = albumFromApi.getArtistName();
                userAlbum.genre = albumFromApi.getGenre();
                userAlbum.yearReleased = albumFromApi.getYearReleased();
                userAlbum.albumThumb = albumFromApi.getAlbumThumb();
                // Campos como userRating, userReview, status pueden inicializarse a valores por defecto o dejarse nulos/vacíos
                userAlbum.userRating = 0;
                userAlbum.userReview = "";
                userAlbum.status = ""; // O un estado inicial como "none" o "discovered"

                // Upsert UserAlbum en segundo plano y luego mostrar el diálogo
                new UpsertAndShowDialogTask(userAlbum).execute();
            });

            holder.itemView.setOnClickListener(v -> {
                UserAlbum userAlbum = new UserAlbum();
                userAlbum.idAlbum = albumFromApi.getIdAlbum();
                userAlbum.albumName = albumFromApi.getAlbumName();
                userAlbum.artistName = albumFromApi.getArtistName();
                userAlbum.genre = albumFromApi.getGenre();
                userAlbum.yearReleased = albumFromApi.getYearReleased();
                userAlbum.albumThumb = albumFromApi.getAlbumThumb();
                // Podrías obtener el UserAlbum de la BD aquí para pasar datos más completos si ya existe.

                Intent intent = new Intent(getActivity(), AlbumDetailActivity.class);
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
            TextView albumName, artistName, yearReleased;
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

    private class UpsertAndShowDialogTask extends AsyncTask<Void, Void, UserAlbum> {
        private UserAlbum userAlbumToUpsert;

        UpsertAndShowDialogTask(UserAlbum userAlbum) {
            this.userAlbumToUpsert = userAlbum;
        }

        @Override
        protected UserAlbum doInBackground(Void... voids) {
            db.albumDao().upsertAlbum(userAlbumToUpsert);
            // Opcional: Podrías volver a obtener el álbum de la BD aquí 
            // si upsertAlbum no devuelve el objeto o si quieres asegurar que tienes la última versión.
            // UserAlbum upsertedAlbum = db.albumDao().getAlbumById(userAlbumToUpsert.idAlbum);
            // return upsertedAlbum != null ? upsertedAlbum : userAlbumToUpsert;
            return userAlbumToUpsert; // Devolver el mismo objeto por simplicidad, asumiendo que los campos están correctos.
        }

        @Override
        protected void onPostExecute(UserAlbum resultAlbum) {
            if (resultAlbum != null && getActivity() != null) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                AddToListDialogFragment dialogFragment = AddToListDialogFragment.newInstance(resultAlbum);
                dialogFragment.show(fm, "AddToListDialogFragment");
            } else {
                Toast.makeText(getContext(), "Error al procesar el álbum.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
