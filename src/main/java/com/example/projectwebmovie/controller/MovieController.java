package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.AdminViewMovieListDTO;
import com.example.projectwebmovie.dto.SearchDTO;
import com.example.projectwebmovie.model.Movie;
import com.example.projectwebmovie.model.MovieType;
import com.example.projectwebmovie.model.MovieTypeId;
import com.example.projectwebmovie.repository.MovieRepository;
import com.example.projectwebmovie.repository.MovieTypeRepository;
import com.example.projectwebmovie.repository.TypeRepository;
import com.example.projectwebmovie.service.CloudinaryService;
import com.example.projectwebmovie.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Controller
public class MovieController {
    private final CloudinaryService cloudinaryService;
    private final TypeRepository typeRepository;
    private final MovieRepository movieRepository;
    private final MovieTypeRepository movieTypeRepository;
    private final MovieService movieService;

    public MovieController(CloudinaryService cloudinaryService, TypeRepository typeRepository,
            MovieRepository movieRepository, MovieTypeRepository movieTypeRepository, MovieService movieService) {
        this.cloudinaryService = cloudinaryService;
        this.typeRepository = typeRepository;
        this.movieRepository = movieRepository;
        this.movieTypeRepository = movieTypeRepository;
        this.movieService = movieService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    @PostMapping("admin/movies/add")
    public String addMovie(@Valid @ModelAttribute Movie movie,
            BindingResult bindingResult,
            @RequestParam("selectedTypeIds") List<Integer> selectedTypeIds,
            @RequestParam("bannerImageFile") MultipartFile bannerImageFile,
            @RequestParam("posterImageFile") MultipartFile posterImageFile,
            Model model,
            RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Dữ liệu không hợp lệ: " + bindingResult.getAllErrors().toString());
            return "redirect:/admin/movie-management";
        }

        try {
            movieService.addMovieWithTypes(movie, selectedTypeIds, bannerImageFile, posterImageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Phim đã được thêm thành công!");
            return "redirect:/admin/movie-management";
        } catch (IllegalArgumentException i) {
            redirectAttributes.addFlashAttribute("errorMessage", i.getMessage());
            return "redirect:/admin/movie-management";
        } catch (IOException e) {
            model.addAttribute("errorMessage", "Lỗi khi upload ảnh: " + e.getMessage());
            return "redirect:/admin/movie-management";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
            return "redirect:/admin/movie-management";
        }
    }

    @PostMapping("/admin/movies/edit")
    public String editMovie(@Valid @ModelAttribute AdminViewMovieListDTO movieDTO,
            BindingResult bindingResult,
            @RequestParam("bannerImageFile") MultipartFile bannerImageFile,
            @RequestParam("posterImageFile") MultipartFile posterImageFile,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Dữ liệu không hợp lệ: " + bindingResult.getAllErrors().toString());
            return "redirect:/admin/movie-management";
        }

        try {
            movieService.updateMovieTypes(movieDTO, movieDTO.getSelectedTypeIds(), bannerImageFile, posterImageFile);
            redirectAttributes.addFlashAttribute("successMessage", "Phim đã được cập nhật thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }
        return "redirect:/admin/movie-management";
    }

    @PostMapping("admin/movies/delete/{movieId}")
    public String deleteMovie(@PathVariable("movieId") String movieId) {
        movieService.deleteById(movieId);
        return "redirect:/admin/movie-management";
    }

    @GetMapping("/movies/search")
    @ResponseBody
    public List<SearchDTO> searchMovies(@RequestParam("query") String searchTerm) {
        return movieService.searchMovies(searchTerm);
    }
}