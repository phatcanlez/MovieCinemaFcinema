package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.*;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Booking statistics API")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/top-customers")
    @Operation(summary = "Get top 5 customers by total spending")
    public ResponseEntity<?> getTop5CustomersBySpending() {
        try {
            List<TopCustomerDTO> topCustomers = statisticsService.getTop5CustomersBySpending();

            Map<String, Object> response = new HashMap<>();
            response.put("count", topCustomers.size());
            response.put("topCustomers", topCustomers);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/top-movies")
    @Operation(summary = "Get top 5 movies by total bookings and revenue")
    public ResponseEntity<?> getTop5MoviesByBookings() {
        try {
            List<TopMovieDTO> topMovies = statisticsService.getTop5MoviesByBookings();

            Map<String, Object> response = new HashMap<>();
            response.put("count", topMovies.size());
            response.put("topMovies", topMovies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/customers")
    @Operation(summary = "Get customer statistics")
    public ResponseEntity<CustomerStatisticsDTO> getCustomerStatistics() {
        try {
            CustomerStatisticsDTO statistics = statisticsService.getCustomerStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/movies")
    @Operation(summary = "Get movie statistics")
    public ResponseEntity<MovieStatisticsDTO> getMovieStatistics() {
        try {
            MovieStatisticsDTO statistics = statisticsService.getMovieStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/bookings")
    @Operation(summary = "Get booking statistics")
    public ResponseEntity<BookingStatisticsDTO> getBookingStatistics() {
        try {
            BookingStatisticsDTO statistics = statisticsService.getBookingStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revenue/date")
    @Operation(summary = "Get revenue by specific date")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueByDate(
            @Parameter(description = "Date in format YYYY-MM-DD", example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            RevenueStatisticsDTO revenue = statisticsService.getRevenueByDate(date);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revenue/month")
    @Operation(summary = "Get revenue by month")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueByMonth(
            @Parameter(description = "Year", example = "2024")
            @RequestParam int year,
            @Parameter(description = "Month (1-12)", example = "1")
            @RequestParam int month) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().build();
            }
            RevenueStatisticsDTO revenue = statisticsService.getRevenueByMonth(year, month);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revenue/year")
    @Operation(summary = "Get revenue by year")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueByYear(
            @Parameter(description = "Year", example = "2024")
            @RequestParam int year) {
        try {
            RevenueStatisticsDTO revenue = statisticsService.getRevenueByYear(year);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/revenue/range")
    @Operation(summary = "Get revenue between dates")
    public ResponseEntity<RevenueStatisticsDTO> getRevenueBetweenDates(
            @Parameter(description = "Start date in format YYYY-MM-DD", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date in format YYYY-MM-DD", example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().build();
            }
            RevenueStatisticsDTO revenue = statisticsService.getRevenueBetweenDates(startDate, endDate);
            return ResponseEntity.ok(revenue);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/bookings/date")
    @Operation(summary = "Get booking count by specific date")
    public ResponseEntity<?> getBookingsByDate(
            @Parameter(description = "Date in format YYYY-MM-DD", example = "2024-01-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            Long count = statisticsService.getBookingsCountByDate(date);

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("date", date.toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/bookings/month")
    @Operation(summary = "Get booking count by month")
    public ResponseEntity<?> getBookingsByMonth(
            @Parameter(description = "Year", example = "2024")
            @RequestParam int year,
            @Parameter(description = "Month (1-12)", example = "1")
            @RequestParam int month) {
        try {
            if (month < 1 || month > 12) {
                return ResponseEntity.badRequest().body("{\"error\": \"Month must be between 1 and 12\"}");
            }

            Long count = statisticsService.getBookingsCountByMonth(year, month);
            String period = String.format("%04d-%02d", year, month);

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("period", period);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/bookings/year")
    @Operation(summary = "Get booking count by year")
    public ResponseEntity<?> getBookingsByYear(
            @Parameter(description = "Year", example = "2024")
            @RequestParam int year) {
        try {
            Long count = statisticsService.getBookingsCountByYear(year);

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("year", year);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/bookings/range")
    @Operation(summary = "Get booking count between dates")
    public ResponseEntity<?> getBookingsBetweenDates(
            @Parameter(description = "Start date in format YYYY-MM-DD", example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date in format YYYY-MM-DD", example = "2024-01-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            if (startDate.isAfter(endDate)) {
                return ResponseEntity.badRequest().body("{\"error\": \"Start date must be before end date\"}");
            }

            Long count = statisticsService.getBookingsCountBetweenDates(startDate, endDate);
            String period = startDate + " to " + endDate;

            Map<String, Object> response = new HashMap<>();
            response.put("count", count);
            response.put("period", period);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/top-phim-hot")
    @Operation(summary = "Get top hot movies with status SHOWING ordered by bookings")
    public ResponseEntity<?> getTopPhimHotByBooking() {
        try {
            List<TopPhimHotDTO> topMovies = statisticsService.getTopPhimHotByBooking();

            Map<String, Object> response = new HashMap<>();
            response.put("count", topMovies.size());
            response.put("topPhimHot", topMovies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    @GetMapping("/all-movies-stats")
    @Operation(summary = "Get all movies with booking and revenue statistics")
    public ResponseEntity<?> getAllMoviesByBookingAndRevenue() {
        try {
            List<TopPhimHotDTO> allMovies = statisticsService.getAllMoviesByBookingAndRevenue();

            Map<String, Object> response = new HashMap<>();
            response.put("count", allMovies.size());
            response.put("movies", allMovies);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/member-statistics")
    @Operation(summary = "Get member statistics by year, month and role ID")
    public ResponseEntity<AccountStaticsResponseDTO> getMemberStatistics(
        @RequestParam int year,
        @RequestParam int month
      
    ) {
        try {
            AccountStaticsResponseDTO stats = statisticsService.getAccountStatistics(year, month, 3);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
        return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/employee-statistics")
    @Operation(summary = "Get employee statistics by year, month and role ID")
    public ResponseEntity<AccountStaticsResponseDTO> getEmployeeStatistics(
        @RequestParam int year,
        @RequestParam int month
      
    ) {
        try {
            AccountStaticsResponseDTO stats = statisticsService.getAccountStatistics(year, month, 2);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
        return ResponseEntity.badRequest().build();
        }
    }

}