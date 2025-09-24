package com.trujo.mellomaniachub;

import com.trujo.mellomaniachub.models.AlbumResponse;
import com.trujo.mellomaniachub.models.ArtistReponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TheAudioDBService {
    String BASE_URL = "https://www.theaudiodb.com/api/v1/json/123/";

    @GET("search.php")
    Call<ArtistReponse> searchArtists(@Query("s") String artistName);

    @GET("searchalbum.php")
    Call<AlbumResponse> searchAlbumsByArtistName(@Query("s") String artistName);

    @GET("searchalbum.php") // Este es el endpoint que mencionaste
    Call<AlbumResponse> getAlbumsByArtistName(@Query("s") String artistName);

    @GET("searchalbum.php")
    Call<AlbumResponse> fetchAlbumsByArtistQuery(@Query("s") String artistQuery);
}
