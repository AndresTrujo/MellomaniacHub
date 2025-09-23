package com.trujo.mellomaniachub;

import com.trujo.mellomaniachub.models.AlbumResponse;
import com.trujo.mellomaniachub.models.ArtistReponse;

import retrofit2.Call;

public interface TheAudioDBService {
    String BASE_URL = "https://www.theaudiodb.com/api/v1/json/2/";

    @retrofit2.http.GET("search.php")
    Call<ArtistReponse> searchArtists(@retrofit2.http.Query("s") String artistName);

    @retrofit2.http.GET("album.php")
    Call<AlbumResponse> getAlbumsByArtistId(@retrofit2.http.Query("i") String artistId);
}
