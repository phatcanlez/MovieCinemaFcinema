package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.ComboDTO;
import com.example.projectwebmovie.dto.SeatScheduleDTO;
import com.example.projectwebmovie.dto.booking.ApplyPromotionDTO;
import com.example.projectwebmovie.dto.booking.BookingDetailDTO;
import com.example.projectwebmovie.dto.booking.BookingRequestDTO;
import com.example.projectwebmovie.dto.booking.PagedBookingInAdmin;
import com.example.projectwebmovie.enums.ComboStatus;
import com.example.projectwebmovie.model.Account;
import com.example.projectwebmovie.model.Combo;
import com.example.projectwebmovie.model.Movie;
import com.example.projectwebmovie.repository.ComboRepository;
import com.example.projectwebmovie.repository.MovieScheduleRepository;

import com.example.projectwebmovie.model.MovieSchedule;
import com.example.projectwebmovie.model.ShowDates;
import com.example.projectwebmovie.service.AccountService;
import com.example.projectwebmovie.service.BookingService;
import com.example.projectwebmovie.service.PromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingAPIController {
    private final BookingService bookingService;
    private final ComboRepository comboRepository;
    private final PromotionService promotionService;
    private final AccountService accountService;
    private final MovieScheduleRepository movieScheduleRepository;

    @PostMapping("/create")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO requestDTO) {
        try {
            Object result = bookingService.bookTicket(requestDTO);
            if (result instanceof String) {
                return ResponseEntity.ok().body("{\"payUrl\": \"" + result + "\"}");
            } else {
                return ResponseEntity.ok(result);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/confirm/{bookingId}")
    public ResponseEntity<?> confirmPayment(@PathVariable String bookingId,
            @RequestParam(required = false) String paymentMethod) {
        try {
            bookingService.confirmPayment(bookingId, paymentMethod);
            return ResponseEntity.ok("{\"message\": \"Payment confirmed successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/cancel/{bookingId}")
    public ResponseEntity<?> cancelBooking(@PathVariable String bookingId) {
        try {
            bookingService.cancelBooking(bookingId);
            return ResponseEntity.ok("{\"message\": \"Booking cancelled successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/cancel-admin/{bookingId}")
    public ResponseEntity<?> cancelBookingByAdmin(@PathVariable String bookingId,
            @RequestParam(required = false) String reason) {
        try {
            bookingService.cancelBookingByAdmin(bookingId, reason);
            return ResponseEntity.ok("{\"message\": \"Booking cancelled by admin successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/seats")
    public ResponseEntity<?> getSeatsByScheduleAndMovie(
            @Parameter String movieId,
            @Parameter Integer showDateId,
            @Parameter Integer scheduleId) {
        try {
            // Chuyển đổi danh sách ghế sang SeatScheduleDTO
            Map<Integer, Map<String, SeatScheduleDTO>> seatDTOs = bookingService
                    .getAvailableSeatsByScheduleIdAndMovieIdAndShowdate(
                            scheduleId,
                            movieId, showDateId);

            if (seatDTOs.isEmpty()) {
                return ResponseEntity.ok("{\"message\": \"No available seats found\"}");
            }

            // Tạo response với count và danh sách ghế
            Map<String, Object> response = new HashMap<>();
            // int count = seatDTOs.values().stream()
            // .mapToInt(Map::size)
            // .sum(); // Tính tổng số ghế
            // response.put("count", count);
            response.put("seats", seatDTOs);

            return ResponseEntity.ok(seatDTOs);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/combos")
    public ResponseEntity<?> getAllCombos() {
        try {
            // Lấy tất cả combo có isActive = true và comboStatus là NORMAL hoặc HOT
            List<Combo> combos = comboRepository.findAll().stream()
                    .filter(combo -> combo.isActive() && (combo.getComboStatus() == ComboStatus.NORMAL
                            || combo.getComboStatus() == ComboStatus.HOT))
                    .toList();

            if (combos.isEmpty()) {
                return ResponseEntity.ok("{\"message\": \"No active combos available\"}");
            }

            // Chuyển đổi danh sách combo sang ComboDTO
            List<ComboDTO> comboDTOs = combos.stream().map(combo -> {
                Integer discountedPrice = combo.getDiscountedPrice(); // Sử dụng phương thức từ model
                return new ComboDTO(
                        combo.getComboId(),
                        combo.getComboName(),
                        combo.getDescription(),
                        combo.getPrice(),
                        combo.getDiscountPercentage(),
                        discountedPrice,
                        combo.isActive(),
                        combo.getImageUrl(),
                        combo.getComboStatus().name() // Chuyển enum sang String
                );
            }).collect(Collectors.toList());

            // Tạo response với count và danh sách combo
            Map<String, Object> response = new HashMap<>();
            response.put("count", comboDTOs.size());
            response.put("combos", comboDTOs);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/details")
    public ResponseEntity<?> getBookingDetails(
            @Parameter String movieId,
            @Parameter Integer showDateId,
            @Parameter Integer scheduleId) {
        try {
            // Kiểm tra tham số đầu vào
            if (movieId == null || showDateId == null || scheduleId == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Missing required parameters\"}");
            }

            // Lấy lịch chiếu theo movieId, showDateId và scheduleId
            MovieSchedule schedule = movieScheduleRepository
                    .findFullMovieSchedules(movieId, scheduleId, showDateId);

            if (schedule == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Schedule not found\"}");
            }

            Movie movie = schedule.getMovie();
            ShowDates showDate = schedule.getShowDates();
            if (movie == null || showDate == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Movie or ShowDate not found\"}");
            }
            String roomName = schedule.getCinemaRoom() != null ? schedule.getCinemaRoom().getCinemaRoomName()
                    : "Unknown";
            if (roomName == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Cinema room not found\"}");
            }
            // Kiểm tra xem lịch chiếu có hợp lệ không
            if (schedule.getSchedule() == null || schedule.getSchedule().getScheduleTime() == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Invalid schedule time\"}");
            }

            // Tạo response data
            Map<String, Object> response = new HashMap<>();
            response.put("movieId", movie.getMovieId());
            response.put("movieName", movie.getMovieNameVn());
            response.put("posterUrl", movie.getSmallImage());
            response.put("showDate", showDate.getShowDate());
            response.put("startTime", schedule.getSchedule().getScheduleTime());
            response.put("roomName", roomName);
            response.put("movieFormat", movie.getVersion().getValue());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/getAllBooking")
    public ResponseEntity<?> getAllBookingByAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        try {
            PagedBookingInAdmin bookings = bookingService.getAllBookingByAdmin(page, size, search, status);
            if (bookings.getBookings().isEmpty()) {
                return ResponseEntity.ok("{\"message\": \"No bookings found\"}");
            }
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/ticket-detail/{bookingId}")
    @Operation(summary = "Get booking details by booking ID")
    public ResponseEntity<?> getBookingDetail(@PathVariable String bookingId) {
        try {
            if (bookingId == null || bookingId.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Booking ID is required\"}");
            }

            BookingDetailDTO bookingDetail = bookingService.getBookingDetailById(bookingId);
            return ResponseEntity.ok(bookingDetail);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("{\"error\": \"An error occurred while fetching booking details\"}");
        }
    }

    @PostMapping("/apply-promotion")
    @Operation(summary = "Apply promotion code to booking")
    public ResponseEntity<?> applyPromotion(@RequestBody ApplyPromotionDTO request) {
        try {
            if (request.getPromotionCode() == null || request.getOriginalAmount() == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Promotion code and amount are required\"}");
            }

            Map<String, Object> result = promotionService.applyPromotionByCode(
                    request.getPromotionCode(),
                    request.getOriginalAmount(),
                    request.getAccountId());

            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/points")
    @Operation(summary = "Get user points")
    public ResponseEntity<?> getPoint() {
        try {
            Account account = accountService.getCurrentAccount();
            if (account == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Account not found\"}");
            }
            Map<String, Object> response = new HashMap<>();
            response.put("point", account.getPoints());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}