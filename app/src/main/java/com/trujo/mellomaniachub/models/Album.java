package com.trujo.mellomaniachub.models;
import com.google.gson.annotations.SerializedName;

public class Album {
    @SerializedName("idAlbum")
    private String idAlbum;

    @SerializedName("strAlbum")
    private String strAlbum; // Nombre del álbum

    @SerializedName("strArtist")
    private String strArtist; // Nombre del artista

    @SerializedName("intYearReleased")
    private String intYearReleased; // Año de lanzamiento

    @SerializedName("strGenre")
    private String strGenre; // Género

    @SerializedName("strAlbumThumb")
    private String strAlbumThumb; // URL de la miniatura del álbum

    // Getters
    public String getIdAlbum() { return idAlbum; }
    public String getAlbumName() { return strAlbum; } // Método renombrado para claridad
    public String getArtistName() { return strArtist; } // Método renombrado para claridad
    public String getYearReleased() { return intYearReleased; } // Método renombrado para claridad
    public String getGenre() { return strGenre; }
    public String getAlbumThumb() { return strAlbumThumb; }


}
