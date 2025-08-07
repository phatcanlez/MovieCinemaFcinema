package com.example.projectwebmovie.mapper;

import com.example.projectwebmovie.dto.AdminViewMovieListDTO;
import com.example.projectwebmovie.dto.MovieDTO;
import com.example.projectwebmovie.model.Movie;
import com.example.projectwebmovie.model.Type;

import java.util.List;
import java.util.stream.Collectors;

public class MovieMapper {

    public static AdminViewMovieListDTO toAdminViewMovieListDTO(Movie movie) {
        if (movie == null) {
            return null;
        }
        AdminViewMovieListDTO dto = new AdminViewMovieListDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setDuration(movie.getDuration());
        dto.setFromDate(movie.getFromDate());
        dto.setToDate(movie.getToDate());
        dto.setActor(movie.getActor());
        dto.setContent(movie.getContent());
        dto.setDirector(movie.getDirector());
        dto.setMovieNameEnglish(movie.getMovieNameEnglish());
        dto.setMovieNameVn(movie.getMovieNameVn());
        dto.setSmallImage(movie.getSmallImage());
        dto.setLargeImage(movie.getLargeImage());
        dto.setMovieProductionCompany(movie.getMovieProductionCompany());
        dto.setTrailerId(movie.getTrailerId());
        dto.setRating(movie.getRating());
        dto.setPrice(movie.getPrice());
        dto.setStatus(movie.getStatus().getDisplayName());
        dto.setVersion(
                movie.getVersion() != null ? movie.getVersion().getValue() : null);
        dto.setSelectedTypeIds(
                movie.getMovieTypes()
                        .stream()
                        .map(mt -> mt.getId().getTypeId())
                        .collect(Collectors.toList()));
        return dto;
    }

    public static MovieDTO toMovieDTO(Movie movie) {
        if (movie == null) {
            return null;
        }
        MovieDTO dto = new MovieDTO();
        dto.setMovieId(movie.getMovieId());
        dto.setDuration(movie.getDuration());
        dto.setFromDate(movie.getFromDate().toString());
        dto.setToDate(movie.getToDate().toString());
        dto.setActor(movie.getActor());
        dto.setContent(movie.getContent());
        dto.setDirector(movie.getDirector());
        dto.setMovieNameEnglish(movie.getMovieNameEnglish());
        dto.setMovieNameVn(movie.getMovieNameVn());
        dto.setSmallImage(movie.getSmallImage());
        dto.setLargeImage(movie.getLargeImage());
        dto.setRating(movie.getRating());
        dto.setMovieProductionCompany(movie.getMovieProductionCompany());
        dto.setTrailerId(movie.getTrailerId());
        dto.setPrice(movie.getPrice());
        dto.setStatus(movie.getStatus().getDisplayName());
        dto.setVersion(
                movie.getVersion() != null ? movie.getVersion().getValue() : null);
        List<String> types = movie.getMovieTypes().stream()
                .map(mt -> mt.getType().getTypeName())
                .collect(Collectors.toList());
        dto.setTypes(types);
        return dto;
    }
}
