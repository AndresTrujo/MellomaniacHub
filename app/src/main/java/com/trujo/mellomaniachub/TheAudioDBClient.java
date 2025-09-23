package com.trujo.mellomaniachub;

import com.trujo.mellomaniachub.models.Album;
import com.trujo.mellomaniachub.models.AlbumResponse;
import com.trujo.mellomaniachub.models.Artist;
import com.trujo.mellomaniachub.models.ArtistReponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TheAudioDBClient {

    private static TheAudioDBService service;

    public static void main(String[] args) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheAudioDBService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(TheAudioDBService.class);
    }

    public static void searchArtist(String artistName) {
        service.searchArtists(artistName).enqueue(new Callback<ArtistReponse>() {
            @Override
            public void onResponse(Call<ArtistReponse> call, Response<ArtistReponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artists = response.body().getArtists();
                    if (!artists.isEmpty()) {
                        Artist artist = artists.get(0);
                        System.out.println("Artista encontrado: " + artist.getName());
                        getAlbumsForArtist(artist.getName()); // Usamos el nombre para buscar de nuevo
                    } else {
                        System.out.println("Artista no encontrado.");
                    }
                } else {
                    System.err.println("Error en la respuesta: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ArtistReponse> call, Throwable t) {
                System.err.println("Error de red: " + t.getMessage());
            }
        });
    }

    /**
     * Obtiene los álbumes de un artista por nombre (usando el mismo endpoint, es un workaround).
     * Nota: La API no tiene un endpoint para obtener los álbumes de un artista por nombre de forma directa,
     * pero la búsqueda de artistas a menudo regresa el ID del artista, que se usaría para el endpoint de álbumes.
     * En este ejemplo simple, simulamos una segunda búsqueda.
     */
    public static void getAlbumsForArtist(String artistName) {
        service.searchArtists(artistName).enqueue(new Callback<ArtistReponse>() {
            @Override
            public void onResponse(Call<ArtistReponse> call, Response<ArtistReponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Artist> artists = response.body().getArtists();
                    if (!artists.isEmpty()) {
                        String artistId = artists.get(0).getIdArtist(); // Puede dar error
                        System.out.println("Buscando álbumes para el artista con ID: " + artistId);

                        // Llamada al endpoint para obtener los álbumes del artista por ID
                        service.getAlbumsByArtistId(artistId).enqueue(new Callback<AlbumResponse>() {
                            @Override
                            public void onResponse(Call<AlbumResponse> call, Response<AlbumResponse> albumResponse) {
                                if (albumResponse.isSuccessful() && albumResponse.body() != null) {
                                    List<Album> albums = albumResponse.body().getAlbums();
                                    System.out.println("Álbumes encontrados:");
                                    for (Album album : albums) {
                                        System.out.println("- " + album.getAlbumName() + " (" + album.getYearReleased() + ") " + album.getAlbumThumb());
                                    }
                                } else {
                                    System.err.println("Error al obtener los álbumes: " + albumResponse.code());
                                }
                            }

                            @Override
                            public void onFailure(Call<AlbumResponse> call, Throwable t) {
                                System.err.println("Error de red al obtener álbumes: " + t.getMessage());
                            }
                        });
                    }
                }
            }

            @Override
            public void onFailure(Call<ArtistReponse> call, Throwable t) {
                System.err.println("Error en la búsqueda del artista para obtener su ID: " + t.getMessage());
            }
        });
    }
}