package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.dto.MovieDTO;
import com.example.projectwebmovie.dto.ShowtimeDTO;
import com.example.projectwebmovie.dto.booking.ApplyPromotionDTO;
import com.example.projectwebmovie.dto.booking.BookingRequestDTO;
import com.example.projectwebmovie.enums.BookingStatus;
import com.example.projectwebmovie.enums.MovieStatus;
import com.example.projectwebmovie.enums.PaymentStatus;
import com.example.projectwebmovie.model.*;
import com.example.projectwebmovie.repository.*;
import com.example.projectwebmovie.service.BookTicketByEmployeeService;
import com.example.projectwebmovie.service.MovieService;
import com.example.projectwebmovie.service.PromotionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/employee")
public class EmployeeAPIController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeAPIController.class);

    @Autowired
    private BookTicketByEmployeeService bookTicketByEmployeeService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private ScheduleSeatRepository scheduleSeatRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private MovieService movieService;

    @PostMapping("/booking/create")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Create a new booking by employee", description = "Creates a booking with CASH or MOMO payment")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<?> createBooking(@RequestBody BookingRequestDTO form) {
        logger.info("Received booking request from employee: {}", form);
        try {
            Object result = bookTicketByEmployeeService.bookTicketByEmployee(form);
            if (result instanceof String) {
                return ResponseEntity.ok().body("{\"payUrl\": \"" + result + "\"}");
            } else if (result instanceof Booking) {
                return ResponseEntity.ok().body(result);
            } else {
                return ResponseEntity.badRequest().body("{\"error\": \"Kết quả không hợp lệ\"}");
            }
        } catch (Exception e) {
            logger.error("Error creating booking: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @PostMapping("/booking/confirm-cash")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Confirm cash payment for a booking", description = "Manually confirm cash payment by employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid input or status")
    public ResponseEntity<String> confirmCashPayment(@RequestParam String bookingId) {
        logger.info("Confirming cash payment for bookingId: {}", bookingId);
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            if (booking.getStatus() == BookingStatus.PENDING) {
                booking.setStatus(BookingStatus.SUCCESS);
                bookingRepository.save(booking);
                logger.info("Cash payment confirmed for bookingId: {}", bookingId);
                return ResponseEntity.ok("Thanh toán bằng tiền mặt thành công!");
            }
            return ResponseEntity.badRequest().body("Không thể xác nhận: Đơn hàng không ở trạng thái PENDING");
        } catch (Exception e) {
            logger.error("Error confirming cash payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Lỗi khi xác nhận thanh toán: " + e.getMessage());
        }
    }

    @PostMapping("/booking/confirm-momo")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Confirm MoMo payment for a booking", description = "Manually confirm MoMo payment by employee")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid input or status")
    public ResponseEntity<?> confirmMomoPaymentByEmployee(
            @RequestParam String bookingId,
            @RequestParam String transactionId,
            @RequestParam String status) {
        logger.info("Confirming MoMo payment for bookingId: {}, transactionId: {}, status: {}", bookingId,
                transactionId, status);
        try {
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
            if (booking.getStatus() != BookingStatus.PENDING) {
                return ResponseEntity.badRequest().body("{\"error\": \"Booking not in PENDING status\"}");
            }

            Payment payment = booking.getPayments().stream()
                    .filter(p -> p.getStatus().equals("PENDING"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No pending payment found for booking: " + bookingId));

            if ("SUCCESS".equalsIgnoreCase(status)) {
                payment.setStatus(PaymentStatus.SUCCESS.toString());
                payment.setPaymentDate(LocalDateTime.now());
                booking.setStatus(BookingStatus.SUCCESS);
                paymentRepository.save(payment);
                bookingRepository.save(booking);

                // Deduct points if used and account exists
                if (booking.getUsedPoints() != null && booking.getUsedPoints() > 0) {
                    Account account = booking.getAccount();
                    if (account != null) {
                        int currentPoints = account.getPoints() != null ? account.getPoints() : 0;
                        int newPoints = currentPoints - booking.getUsedPoints();
                        if (newPoints < 0)
                            throw new RuntimeException("Không đủ điểm để trừ");
                        account.setPoints(newPoints);
                        accountRepository.save(account);
                    }
                }

                logger.info("MoMo payment confirmed for bookingId: {}", bookingId);
                return ResponseEntity.ok("{\"message\": \"MoMo payment confirmed successfully\"}");
            } else {
                payment.setStatus(PaymentStatus.FAILED.toString());
                payment.setPaymentDate(LocalDateTime.now());
                booking.setStatus(BookingStatus.CANCELLED);
                paymentRepository.save(payment);
                bookingRepository.save(booking);

                // Release seats if payment failed
                List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
                for (BookingSeat bs : bookingSeats) {
                    ScheduleSeat scheduleSeat = scheduleSeatRepository.findById(bs.getScheduleSeatId()).orElseThrow();
                    scheduleSeat.setSeatStatus(0); // Release seat
                    scheduleSeatRepository.save(scheduleSeat);
                }

                logger.info("MoMo payment failed for bookingId: {}, booking cancelled", bookingId);
                return ResponseEntity.ok("{\"message\": \"MoMo payment failed, booking cancelled\"}");
            }
        } catch (Exception e) {
            logger.error("Error confirming MoMo payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/redirect")
    public String handlePaymentRedirect(
            @RequestParam String resultCode,
            @RequestParam String orderInfo,
            Model model) {
        if (orderInfo.isEmpty()) {
            logger.warn("Order info is empty");
            return "redirect:/employee/booking/failure"; // Chuyển hướng đến trang thất bại
        }

        try {
            if ("0".equals(resultCode)) {
                // Thanh toán thành công
                Booking booking = bookTicketByEmployeeService.confirmPayment(orderInfo, null);
                List<HashMap<String, Object>> seats = booking.getBookingSeats().stream()
                        .map(seat -> {
                            HashMap<String, Object> seatMap = new HashMap<>();
                            seatMap.put("seatColRow",
                                    seat.getScheduleSeat().getSeatRow() + seat.getScheduleSeat().getSeatColumn());
                            seatMap.put("price", seat.getScheduleSeat().getSeatPrice());
                            return seatMap;
                        })
                        .collect(Collectors.toList());

                model.addAttribute("booking", booking);
                model.addAttribute("combos", booking.getBookingCombos().stream()
                        .map(m -> m.getCombo().getComboName() + " x " + m.getQuantity() + " = "
                                + (m.getCombo().getDiscountedPrice() * m.getQuantity()))
                        .collect(Collectors.toList()));
                model.addAttribute("movie", booking.getMovie());
                model.addAttribute("showDate", booking.getShowDates().getShowDate());
                model.addAttribute("showTime", LocalTime.parse(booking.getSchedule().getScheduleTime()));
                model.addAttribute("roomName",
                        booking.getSchedule().getMovieSchedules().get(0).getCinemaRoom().getCinemaRoomName());
                model.addAttribute("seats", seats);
                model.addAttribute("totalAmount", booking.getTotalPrice());
                model.addAttribute("ticketId", booking.getBookingId());

                return "employee/booking-success"; // Render template success cho employee
            } else {
                // Thanh toán thất bại
                bookTicketByEmployeeService.cancelBooking(orderInfo);
                return "redirect:/employee/booking/failure"; // Chuyển hướng đến trang thất bại
            }
        } catch (Exception e) {
            logger.error("Error processing booking redirect: {}", e.getMessage(), e);
            bookTicketByEmployeeService.cancelBooking(orderInfo); // Hủy booking nếu có lỗi
            return "redirect:/employee/booking/failure";
        }
    }

    // Thêm endpoint mới cho employee
    @GetMapping("/showtimes")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Get showtimes for a movie by employee", description = "Retrieve available showtimes for a movie on a specific date")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<List<ShowtimeDTO>> getEmployeeShowtimes(
            @RequestParam("movieId") String movieId,
            @RequestParam("date") String date) {
        logger.info("Received showtimes request from employee for movieId: {}, date: {}", movieId, date);
        try {
            LocalDate showDate = LocalDate.parse(date);
            List<MovieSchedule> schedules = bookTicketByEmployeeService.getSchedulesByMovieAndDate(movieId, showDate);
            List<ShowtimeDTO> showtimes = schedules.stream()
                    .map(schedule -> new ShowtimeDTO(
                            schedule.getSchedule().getScheduleTime().toString(),
                            schedule.getSchedule().getScheduleTime().format("hh:mm a"),
                            schedule.getCinemaRoom().getCinemaRoomName(),
                            schedule.getCinemaRoom().getCinemaRoomId(),
                            schedule.getShowDates().getShowDateId(),
                            schedule.getSchedule().getScheduleId()))
                    .sorted(Comparator.comparing(ShowtimeDTO::getTime))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(showtimes);
        } catch (DateTimeParseException e) {
            logger.error("Invalid date format: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.emptyList());
        } catch (Exception e) {
            logger.error("Error retrieving showtimes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/movies")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @Operation(summary = "Get all currently showing movies")
    @ApiResponse(responseCode = "200", description = "Success")
    @ApiResponse(responseCode = "400", description = "Cannot retrieve movies")
    public ResponseEntity<List<MovieDTO>> getAllMovies() {
        logger.info("Received request to get all movies");
        try {
            List<MovieDTO> movies = movieService.getMoviesByStatus(MovieStatus.SHOWING);
            return ResponseEntity.ok(movies);
        } catch (Exception e) {
            logger.error("Error retrieving movies: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    @GetMapping("/points")
    @Operation(summary = "Get user points by email")
    public ResponseEntity<?> getPointsByEmail(@RequestParam String email) {
        try {
            if (email == null || email.isEmpty()) {
                return ResponseEntity.badRequest().body("{\"error\": \"Email is required\"}");
            }

            Account account = accountRepository.findByEmail(email);
            if (account == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"Account not found for the provided email\"}");
            }

            Map<String, Object> response = new HashMap<>();
            response.put("accountId", account.getAccountId());
            response.put("email", email);
            response.put("points", account.getPoints());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}