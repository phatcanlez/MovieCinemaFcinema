package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.AdminViewMovieListDTO;
import com.example.projectwebmovie.dto.MovieDTO;
import com.example.projectwebmovie.dto.SearchDTO;
import com.example.projectwebmovie.enums.MovieStatus;
import com.example.projectwebmovie.enums.MovieVersion;
import com.example.projectwebmovie.mapper.MovieMapper;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.CinemaRoom;
import com.example.projectwebmovie.model.Employee;
import com.example.projectwebmovie.model.Movie;
import com.example.projectwebmovie.model.MovieType;
import com.example.projectwebmovie.model.MovieTypeId;
import com.example.projectwebmovie.model.Type;
import com.example.projectwebmovie.repository.CinemaRoomRepository;
import com.example.projectwebmovie.repository.MovieRepository;
import com.example.projectwebmovie.repository.MovieScheduleRepository;
import com.example.projectwebmovie.repository.MovieTypeRepository;
import com.example.projectwebmovie.repository.TypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class MovieService {
    @Autowired
    private MovieRepository movieRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private TypeRepository typeRepository;
    @Autowired
    private MovieTypeRepository movieTypeRepository;
    @Autowired
    private CinemaRoomRepository cinemaRoomRepository;
    @Autowired
    private MovieScheduleRepository movieScheduleRepository;
    @Autowired
    private IdGenerateService idGenerateService;

    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleMovieStatusUpdate() {
        updateMovieStatusesBasedOnDate();
    }

    public MovieService(CloudinaryService cloudinaryService,
            TypeRepository typeRepository,
            MovieRepository movieRepository,
            MovieTypeRepository movieTypeRepository) {
        this.cloudinaryService = cloudinaryService;
        this.typeRepository = typeRepository;
        this.movieRepository = movieRepository;
        this.movieTypeRepository = movieTypeRepository;
    }

    public Page<AdminViewMovieListDTO> getAllInAdminPage(Pageable pageable, String search) {
        Page<AdminViewMovieListDTO> list;
        if (search == null || search.trim().isEmpty()) {
            list = movieRepository.findAll(pageable).map(MovieMapper::toAdminViewMovieListDTO);
        } else {
            list = movieRepository.findBySearch(search, pageable).map(MovieMapper::toAdminViewMovieListDTO);
        }
        return list;
    }

    public List<CinemaRoom> getAllCinemaRooms() {
        return cinemaRoomRepository.findAll();
    }

    public static String extractVideoId(String url) {
        if (url == null)
            return null;

        String regex = "^(?:https?:\\/\\/)?(?:www\\.|m\\.)?(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/)([\\w-]{11})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        }

        if (url.matches("^[\\w-]{11}$")) {
            return url;
        }

        return null;
    }

    public void addMovieWithTypes(Movie movie,
            List<Integer> selectedTypeIds,
            MultipartFile bannerImageFile,
            MultipartFile posterImageFile) throws IOException {

        if (movieRepository.existsByMovieNameEnglish(movie.getMovieNameEnglish())
                || movieRepository.existsByMovieNameVn(movie.getMovieNameVn())) {
            throw new IllegalArgumentException("Tên Phim đã tồn tại!");
        }
        if (extractVideoId(movie.getTrailerId()) != null) {
            movie.setTrailerId(extractVideoId(movie.getTrailerId()));
        } else {
            throw new IllegalArgumentException("Đường dẫn YouTube không hợp lệ!");
        }

        String movieId = idGenerateService.generateStringId("MOV", Movie.class, "movieId");

        if (movieId == null || movieId.isBlank()) {
            throw new IllegalStateException("IdGenerateService is hông được");
        } else {
            movie.setMovieId(movieId);
        }
        if (movie.getFromDate().isAfter(LocalDate.now())) {
            movie.setStatus(MovieStatus.UPCOMING);
        } else if (!movie.getFromDate().isAfter(LocalDate.now()) && !movie.getToDate().isBefore(LocalDate.now())) {
            movie.setStatus(MovieStatus.SHOWING);
        } else if (movie.getToDate().isBefore(LocalDate.now())) {
            movie.setStatus(MovieStatus.ENDED);
        }
        movie.setLargeImage(cloudinaryService.uploadImage(bannerImageFile));
        movie.setSmallImage(cloudinaryService.uploadImage(posterImageFile));

        movieRepository.save(movie);

        List<MovieType> movieTypes = new ArrayList<>();
        for (Integer typeId : selectedTypeIds) {
            MovieType movieType = new MovieType();
            movieType.setId(new MovieTypeId(movie.getMovieId(), typeId));
            movieType.setMovie(movie);
            movieType.setType(typeRepository.findById(typeId).orElseThrow());
            movieType.setPrimaryType(false);
            movieTypes.add(movieType);
        }
        movieTypeRepository.saveAll(movieTypes);
    }

    public void updateMovieStatusesBasedOnDate() {
        List<Movie> movies = movieRepository.findAll();

        LocalDate today = LocalDate.now();

        for (Movie movie : movies) {
            MovieStatus newStatus;

            if (movie.getFromDate().isAfter(today)) {
                newStatus = MovieStatus.UPCOMING;
            } else if (!movie.getFromDate().isAfter(today) && !movie.getToDate().isBefore(today)) {
                newStatus = MovieStatus.SHOWING;
            } else {
                newStatus = MovieStatus.ENDED;
            }

            if (movie.getStatus() != newStatus) {
                movie.setStatus(newStatus);
                movieRepository.save(movie);
            }
        }
    }

    public void updateMovieFromDto(Movie movie, AdminViewMovieListDTO dto,
            MultipartFile bannerImageFile, MultipartFile posterImageFile) {

        movie.setMovieNameVn(dto.getMovieNameVn().toUpperCase());
        movie.setMovieNameEnglish(dto.getMovieNameEnglish().toUpperCase());
        movie.setContent(dto.getContent());
        movie.setActor(dto.getActor());
        movie.setDirector(dto.getDirector());
        movie.setDuration(dto.getDuration());
        movie.setVersion(MovieVersion.valueOf(dto.getVersion()));
        movie.setMovieProductionCompany(dto.getMovieProductionCompany());
        movie.setPrice(Double.valueOf(dto.getPrice() != null ? dto.getPrice() : 0)); // Added price update

        // 🔍 So sánh ngày
        LocalDate oldFromDate = movie.getFromDate();
        LocalDate oldToDate = movie.getToDate();
        LocalDate newFromDate = dto.getFromDate();
        LocalDate newToDate = dto.getToDate();

        // 🔁 Nếu fromDate/toDate có thay đổi → kiểm tra lịch chiếu
        if (!Objects.equals(oldFromDate, newFromDate) || !Objects.equals(oldToDate, newToDate)) {
            boolean isValid = movieScheduleRepository.findShowDatesByMovieId(dto.getMovieId())
                    .stream()
                    .anyMatch(showDate -> showDate.isBefore(newFromDate) || showDate.isAfter(newToDate));
            if (isValid) {
                throw new IllegalArgumentException("Phim đang có lịch chiếu nằm ngoài khoảng thời gian mới.");
            }
            // Nếu hợp lệ, mới cập nhật ngày
            movie.setFromDate(newFromDate);
            movie.setToDate(newToDate);
        }

        // 🔗 Trailer ID
        String trailerId = dto.getTrailerId();
        if (trailerId != null && !trailerId.isBlank()) {
            String extractedId = extractVideoId(trailerId);
            if (extractedId != null) {
                movie.setTrailerId(extractedId);
            } else {
                throw new IllegalArgumentException("Đường dẫn YouTube không hợp lệ!");
            }
        }

        // ☁️ Upload ảnh nếu có
        try {
            if (bannerImageFile != null && !bannerImageFile.isEmpty()) {
                movie.setLargeImage(cloudinaryService.uploadImage(bannerImageFile));
            }

            if (posterImageFile != null && !posterImageFile.isEmpty()) {
                movie.setSmallImage(cloudinaryService.uploadImage(posterImageFile));
            }
        } catch (Exception ee) {
            throw new RuntimeException("Lỗi khi upload ảnh", ee);
        }

        // 🎬 Cập nhật trạng thái dựa trên ngày đã được (có thể) cập nhật
        LocalDate today = LocalDate.now();
        LocalDate from = movie.getFromDate();
        LocalDate to = movie.getToDate();

        if (from.isAfter(today)) {
            movie.setStatus(MovieStatus.UPCOMING);
        } else if (!from.isAfter(today) && !to.isBefore(today)) {
            movie.setStatus(MovieStatus.SHOWING);
        } else if (to.isBefore(today)) {
            movie.setStatus(MovieStatus.ENDED);
        }
    }

    @Transactional
    public void updateMovieTypes(AdminViewMovieListDTO movieListDTO,
            List<Integer> selectedTypeIds,
            MultipartFile bannerImageFile,
            MultipartFile posterImageFile) {

        if (selectedTypeIds == null || selectedTypeIds.isEmpty()) {
            System.out.println("Danh sách loại phim rỗng.");
            return;
        }

        Movie movie = movieRepository.findById(movieListDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với ID: " + movieListDTO.getMovieId()));

        updateMovieFromDto(movie, movieListDTO, bannerImageFile, posterImageFile);
        movieRepository.save(movie);

        List<MovieType> existingTypes = movieTypeRepository.findByMovie_MovieId(movie.getMovieId());
        Set<Integer> existingTypeIds = existingTypes.stream()
                .map(mt -> mt.getType().getTypeId())
                .collect(Collectors.toSet());

        Set<Integer> newTypeIds = new HashSet<>(selectedTypeIds);

        List<MovieType> typesToRemove = existingTypes.stream()
                .filter(mt -> !newTypeIds.contains(mt.getType().getTypeId()))
                .collect(Collectors.toList());
        if (!typesToRemove.isEmpty()) {
            movieTypeRepository.deleteAll(typesToRemove);
        }

        Map<Integer, Type> typeMap = typeRepository.findAllByIdIn(newTypeIds).stream()
                .collect(Collectors.toMap(Type::getTypeId, Function.identity()));

        List<MovieType> typesToAdd = newTypeIds.stream()
                .filter(typeId -> !existingTypeIds.contains(typeId))
                .map(typeId -> {
                    Type type = typeMap.get(typeId);
                    if (type == null) {
                        throw new RuntimeException("Không tìm thấy thể loại với ID: " + typeId);
                    }
                    MovieType mt = new MovieType();
                    mt.setId(new MovieTypeId(movie.getMovieId(), typeId));
                    mt.setMovie(movie);
                    mt.setType(type);
                    mt.setPrimaryType(false);
                    return mt;
                })
                .collect(Collectors.toList());

        if (!typesToAdd.isEmpty()) {
            movieTypeRepository.saveAll(typesToAdd);
        }
    }

    public void deleteById(String id) {
        movieRepository.deleteById(id);
    }

    public int getTotalMovie() {
        int total = movieRepository.countByStatus(MovieStatus.SHOWING)
                + movieRepository.countByStatus(MovieStatus.UPCOMING);
        return total;
    }

    public List<MovieDTO> getShowingMoviesforHomepage() {
        List<Movie> movies = movieRepository.getMovieListForHomePage();
        return movies.stream().map(MovieMapper::toMovieDTO).collect(Collectors.toList());
    }

    public List<MovieDTO> getMoviesByStatus(MovieStatus status) {
        List<Movie> movies = movieRepository.findByStatus(status);
        return movies.stream().map(MovieMapper::toMovieDTO).collect(Collectors.toList());
    }

    public MovieDTO getMovieById(String id) {
        Optional<Movie> movieOptional = movieRepository.findById(id);
        if (movieOptional.isPresent()) {
            return MovieMapper.toMovieDTO(movieOptional.get());
        } else {
            return null;
        }
    }

    public List<SearchDTO> searchMovies(String title) {
        return movieRepository.findByMovieNameVnContainingIgnoreCase(title)
                .stream()
                .map(m -> new SearchDTO(m.getMovieId(), m.getMovieNameVn(), m.getSmallImage()))
                .collect(Collectors.toList());
    }
}
