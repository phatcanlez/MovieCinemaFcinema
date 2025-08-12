package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.ComboDTO;
import com.example.projectwebmovie.dto.MovieDTO;
import com.example.projectwebmovie.dto.ShowtimeDTO;
import com.example.projectwebmovie.enums.MovieStatus;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.MovieSchedule;
import com.example.projectwebmovie.service.AccountService;
import com.example.projectwebmovie.service.ComboService;
import com.example.projectwebmovie.service.MovieScheduleService;
import com.example.projectwebmovie.service.MovieService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {
    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    AccountService accountService;

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieScheduleService movieScheduleService;

    @Autowired
    private ComboService comboService;

    @GetMapping({ "/home", "/" })
    public String showHomePage(Model model,
                               @RequestParam(value = "newGoogleUser", required = false) String newGoogleUser) {
        List<MovieDTO> movieList = movieService.getShowingMoviesforHomepage();
        model.addAttribute("movieList", movieList);

        // Kiểm tra người dùng có cần hoàn thiện thông tin không
        Account currentAccount = accountService.getCurrentAccount();
        if (currentAccount != null && !currentAccount.getProfileCompleted()) {
            model.addAttribute("needsProfileCompletion", true);
            if (newGoogleUser != null) {
                model.addAttribute("isNewGoogleUser", true);
            }
        }
        return "home/home";
    }

    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        logger.info("User details: {}", principal.getAttributes());
        return principal.getAttributes();
    }

    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "homeLayout/privacy-policy";
    }

    @GetMapping("/recruitment")
    public String recruitmentPage() {
        return "homeLayout/recruitment";
    }

    @GetMapping("/faq")
    public String faqPage() {
        return "homeLayout/faq";
    }

    @GetMapping("/about-theater")
    public String aboutTheater() {
        return "homeLayout/about-theater";
    }

    @GetMapping("/utility")
    public String utilityPage() {
        return "homeLayout/utility";
    }

    @GetMapping("/partner")
    public String partnerPage() {
        return "homeLayout/partner";
    }

    @GetMapping("/terms")
    public String termsPage() {
        return "homeLayout/terms";
    }

    @GetMapping("/security")
    public String securityPage() {
        return "homeLayout/security";
    }

    @GetMapping("/guide")
    public String guidePage() {
        return "homeLayout/guide";
    }

    @GetMapping("/contact")
    public String contactPage() {
        return "homeLayout/contact";
    }

    @GetMapping("/movie-list")
    public String showMovieList(Model model) {
        System.out.println("Route /movie-list được gọi");
        List<MovieDTO> showingMovieList = movieService.getMoviesByStatus(MovieStatus.SHOWING);
        List<MovieDTO> upcomingMovieList = movieService.getMoviesByStatus(MovieStatus.UPCOMING);

        // Kiểm tra và xử lý trước khi đưa vào model
        for (MovieDTO movie : showingMovieList) {
            // Đảm bảo trường types không null
            if (movie.getTypes() == null) {
                movie.setTypes(List.of("Chưa phân loại"));
            }
        }

        for (MovieDTO movie : upcomingMovieList) {
            // Đảm bảo trường types không null
            if (movie.getTypes() == null) {
                movie.setTypes(List.of("Chưa phân loại"));
            }
        }

        model.addAttribute("showingMovieList", showingMovieList);
        model.addAttribute("upcomingMovieList", upcomingMovieList);
        return "home/movie-list";
    }

    @GetMapping("/api/showtimes")
    @ResponseBody
    public List<ShowtimeDTO> getShowtimes(@RequestParam("movieId") String movieId,
            @RequestParam("date") String date) {
        try {
            LocalDate showDate = LocalDate.parse(date);
            List<MovieSchedule> schedules = movieScheduleService.getSchedulesByMovieAndDate(movieId, showDate);
            return schedules.stream()
                    .map(schedule -> new ShowtimeDTO(
                            schedule.getSchedule().getScheduleTime().toString(),
                            schedule.getSchedule().getScheduleTime().format("hh:mm a"),
                            schedule.getCinemaRoom().getCinemaRoomName(),
                            schedule.getCinemaRoom().getCinemaRoomId(),
                            schedule.getShowDates().getShowDateId(),
                            schedule.getSchedule().getScheduleId()))
                    .sorted(Comparator.comparing(ShowtimeDTO::getTime))
                    .collect(Collectors.toList());
        } catch (DateTimeParseException e) {
            return Collections.emptyList();
        }
    }

    @GetMapping("/movie-detail/{id}")
    public String showMovieDetail(@PathVariable("id") String id, Model model) {
        MovieDTO movie = movieService.getMovieById(id);
        if (movie == null) {
            return "error/404";
        }
        if (movie.getTypes() == null) {
            movie.setTypes(List.of("Chưa phân loại"));
        }
        model.addAttribute("movie", movie);
        return "home/movie-detail";
    }

    @GetMapping("/seat-map/{cinemaRoomId}")
    public String showSeatMap(@PathVariable Integer cinemaRoomId,
            @RequestParam("movieId") String movieId,
            @RequestParam("showDateId") Integer showDateId,
            @RequestParam("scheduleId") Integer scheduleId,
            Model model) {
        MovieSchedule schedule = movieScheduleService.getScheduleByDetails(movieId, cinemaRoomId, showDateId,
                scheduleId);

        return "home/booking/seat-selection";
    }

    @GetMapping("/selection-seat")
    public String showSeatSelection(@RequestParam("movieId") String movieId,
            @RequestParam("scheduleId") Integer scheduleId,
            @RequestParam("showDateId") Integer showDateId,
            Model model) {

        return "home/booking/seat-selection";
    }

    @GetMapping("/combo-selection")
    public String showComboSelection() {
        return "home/booking/combo-selection";
    }

    @GetMapping("/payment")
    public String processPayment() {
        return "home/booking/payment";
    }

}