package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.projectwebmovie.enums.MovieStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String> {

    boolean existsByMovieNameEnglish(String movieNameEnglish);

    boolean existsByMovieNameVn(String movieNameVn);

    int countByStatus(MovieStatus status);

    @Query("SELECT m FROM Movie m WHERE m.status = 'SHOWING' ORDER BY m.fromDate DESC")
    List<Movie> getMovieListForHomePage();

    List<Movie> findByStatus(MovieStatus status);

    List<Movie> findByMovieNameVnContainingIgnoreCase(String movieNameVn);

    @EntityGraph(attributePaths = { "movieTypes" })
    Page<Movie> findAll(Pageable pageable);

    @Query("""
                SELECT m FROM Movie m
                WHERE
                    LOWER(m.movieNameVn) LIKE LOWER(CONCAT('%', :search, '%')) OR
                    LOWER(m.movieNameEnglish) LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Movie> findBySearch(@Param("search") String search, Pageable pageable);

    // Phương thức để lấy toàn bộ thông tin phim bao gồm giá
    @Query("SELECT m FROM Movie m WHERE m.movieId = :movieId")
    Optional<Movie> findByMovieIdWithPrice(@Param("movieId") String movieId);

    // Phương thức để lấy chỉ giá của phim
    @Query("SELECT m.price FROM Movie m WHERE m.movieId = :movieId")
    Optional<Double> findPriceByMovieId(@Param("movieId") String movieId);
}