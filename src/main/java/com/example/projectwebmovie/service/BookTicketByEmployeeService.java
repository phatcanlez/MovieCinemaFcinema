package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.SeatScheduleDTO;
import com.example.projectwebmovie.dto.booking.BookingComboDTO;
import com.example.projectwebmovie.dto.booking.BookingRequestDTO;
import com.example.projectwebmovie.enums.BookingStatus;
import com.example.projectwebmovie.enums.PaymentMethod;
import com.example.projectwebmovie.enums.PaymentStatus;
import com.example.projectwebmovie.mapper.SeatMapper;
import com.example.projectwebmovie.model.*;
import com.example.projectwebmovie.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookTicketByEmployeeService {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ShowDatesRepository showDatesRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private MovieScheduleRepository movieScheduleRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private ScheduleSeatRepository scheduleSeatRepository;

    @Autowired
    private ComboRepository comboRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Autowired
    private MoMoPaymentService moMoPaymentService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final int POINTS_VALUE = 10; // 1 điểm = 10,000 VND
    private static final double POINT_PERCENTAGE = 0.10; // 10% số tiền được cộng thành điểm

    public Object bookTicketByEmployee(BookingRequestDTO form) {
        // Validate required fields
        if (form.getMovieId() == null || form.getScheduleId() == null
                || form.getShowDateId() == null || form.getTotalPrice() == null || form.getPaymentMethod() == null) {
            throw new RuntimeException("Thông tin đặt vé không hợp lệ, 1 giá trị không được để trống");
        }

        // Fetch the show date
        ShowDates showDate = showDatesRepository.findById(form.getShowDateId())
                .orElseThrow(() -> new RuntimeException("Ngày chiếu không tồn tại: " + form.getShowDateId()));
        LocalDate date = showDate.getShowDate();

        // Fetch schedules for the movie and date
        List<MovieSchedule> schedules = movieScheduleRepository.findByMovieMovieIdAndShowDatesShowDate(
                form.getMovieId(), date);

        // Find the specific schedule based on scheduleId
        MovieSchedule movieSchedule = schedules.stream()
                .filter(ms -> ms.getSchedule().getScheduleId().equals(form.getScheduleId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Lịch chiếu không tồn tại cho phim và ngày đã chọn"));

        // Fetch movie to get base price
        Movie movie = movieRepository.findById(form.getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại: " + form.getMovieId()));
        double moviePrice = movie.getPrice();

        // Fetch all available seats for the schedule (seat_status = 0)
        List<ScheduleSeat> availableSeats = scheduleSeatRepository.findAvailableSeatsByScheduleId(form.getScheduleId(),
                form.getMovieId(), form.getShowDateId());
        if (availableSeats == null || availableSeats.isEmpty()) {
            throw new RuntimeException("No available seats for schedule ID: " + form.getScheduleId());
        }

        // Check seat availability and calculate base price
        double basePrice = 0;
        if (form.getSeatsIds() != null && !form.getSeatsIds().isEmpty()) {
            List<Integer> availableSeatIds = availableSeats.stream()
                    .map(ss -> ss.getSeat().getSeatId())
                    .collect(Collectors.toList());
            List<Integer> requestedSeatIds = form.getSeatsIds();
            if (!availableSeatIds.containsAll(requestedSeatIds)) {
                List<Integer> unavailableSeatIds = requestedSeatIds.stream()
                        .filter(seatId -> !availableSeatIds.contains(seatId))
                        .collect(Collectors.toList());
                throw new RuntimeException("Các ghế không khả dụng hoặc đã bị đặt: " + unavailableSeatIds);
            }

            for (Integer seatId : requestedSeatIds) {
                ScheduleSeat seat = availableSeats.stream()
                        .filter(ss -> ss.getSeat().getSeatId().equals(seatId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Ghế không tìm thấy: " + seatId));
                if (seat.getSeatStatus() != 0) {
                    throw new RuntimeException("Ghế đã được book: " + seatId);
                }
                basePrice += seat.getSeatPrice() != null ? seat.getSeatPrice() : moviePrice;
            }
        }

        // Calculate combo price
        double comboPrice = 0;
        if (form.getCombos() != null && !form.getCombos().isEmpty()) {
            for (BookingComboDTO comboDTO : form.getCombos()) {
                Combo combo = comboRepository.findById(comboDTO.getComboId())
                        .orElseThrow(() -> new RuntimeException("Combo không tồn tại: " + comboDTO.getComboId()));
                int quantity = comboDTO.getQuantity() != null ? comboDTO.getQuantity() : 1;

                if (quantity <= 0) {
                    throw new RuntimeException("Số lượng combo phải lớn hơn 0: " + combo.getComboId());
                }
                comboPrice += combo.getDiscountedPrice() * quantity;
            }
        }

        // Handle points deduction for customer email
        Account account = null;
        int availablePoints = 0;
        String accountId = form.getAccountId(); // Thêm field này vào BookingRequestDTO

        if (accountId != null && !accountId.isEmpty()) {
            account = accountRepository.findByAccountId(accountId);
            if (account != null) {
                availablePoints = account.getPoints() != null ? account.getPoints() : 0;
            }
        }

        int usedPoints = 0;
        if (form.getUsedPoints() != null && account != null) {
            usedPoints = Math.min(form.getUsedPoints(), availablePoints);
            if (form.getUsedPoints() > availablePoints) {
                throw new RuntimeException("Số điểm sử dụng vượt quá điểm hiện có: " + availablePoints);
            }
        }

        // Calculate base total price with points deduction
        double baseTotalPrice = basePrice + comboPrice;

        if (baseTotalPrice < 0)
            baseTotalPrice = 0;

        // Apply promotion (if any) at the end
        double finalTotalPrice = baseTotalPrice;
        String promotionId = form.getPromotionId(); // Assuming promotionId is added to BookingRequestDTO
        if (promotionId != null && !promotionId.isEmpty()) {
            Promotion promotion = promotionRepository.findById(promotionId)
                    .orElseThrow(() -> new RuntimeException("Khuyến mãi không tồn tại: " + promotionId));
            if (promotion.getIsActive() && LocalDateTime.now().isBefore(promotion.getEndTime())
                    && LocalDateTime.now().isAfter(promotion.getStartTime())) {
                if (promotion.getDiscountAmount() != null) {
                    finalTotalPrice = Math.max(0, baseTotalPrice - promotion.getDiscountAmount());
                } else if (promotion.getDiscountLevel() != null) {
                    double discountAmount = baseTotalPrice * (promotion.getDiscountLevel() / 100.0);
                    if (promotion.getMaxAmountForPercentDiscount() != null) {
                        discountAmount = Math.min(discountAmount, promotion.getMaxAmountForPercentDiscount());
                    }
                    finalTotalPrice -= discountAmount;
                }
            }
        }

        if (form.getUsedPoints() != null) {
            finalTotalPrice -= form.getUsedPoints() * POINTS_VALUE;
        }

        // Compare totalPrice from FE and BE (after promotion)
        int frontendTotalPrice = form.getTotalPrice();
        if (Math.abs(finalTotalPrice - frontendTotalPrice) > 0.10) {
            throw new RuntimeException("Total price mismatch: Frontend (" + frontendTotalPrice + ") vs Backend ("
                    + finalTotalPrice + ") after applying promotion");
        }

        // Set accountId to customer's accountId if email is provided, otherwise null
        accountId = account != null ? account.getAccountId() : null;

        // Create new booking with unique 10-digit numeric bookingId
        String bookingId = generateUniqueNumericBookingId();
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setAccountId(accountId); // Customer's accountId or null
        booking.setMovieId(form.getMovieId());
        booking.setScheduleId(form.getScheduleId());
        booking.setShowDateId(form.getShowDateId());
        booking.setPromotionId(promotionId);
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(finalTotalPrice);
        booking.setUsedPoints(usedPoints); // Store used points

        // Save booking
        booking = bookingRepository.save(booking);

        // Create and save booking seats
        if (form.getSeatsIds() != null) {
            for (Integer seatId : form.getSeatsIds()) {
                ScheduleSeat scheduleSeat = availableSeats.stream()
                        .filter(ss -> ss.getSeat().getSeatId().equals(seatId))
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Ghế không tìm thấy: " + seatId));
                scheduleSeat.setSeatStatus(1); // Mark as booked
                ScheduleSeat savedScheduleSeat = scheduleSeatRepository.save(scheduleSeat);

                BookingSeat bookingSeat = new BookingSeat();
                bookingSeat.setBookingId(booking.getBookingId());
                bookingSeat.setScheduleSeatId(savedScheduleSeat.getScheduleSeatId());
                bookingSeat.setBooking(booking);
                bookingSeat.setScheduleSeat(savedScheduleSeat);
                bookingSeatRepository.save(bookingSeat);
            }
        }

        // Create and save booking combos
        if (form.getCombos() != null && !form.getCombos().isEmpty()) {
            for (BookingComboDTO comboDTO : form.getCombos()) {
                Combo combo = comboRepository.findById(comboDTO.getComboId())
                        .orElseThrow(() -> new RuntimeException("Combo không tồn tại: " + comboDTO.getComboId()));
                int quantity = comboDTO.getQuantity() != null ? comboDTO.getQuantity() : 1;
                if (quantity <= 0) {
                    throw new RuntimeException("Số lượng combo phải lớn hơn 0: " + comboDTO.getComboId());
                }
                BookingCombo bookingCombo = new BookingCombo();
                bookingCombo.setBookingId(booking.getBookingId());
                bookingCombo.setComboId(combo.getComboId());
                bookingCombo.setQuantity(quantity);
                bookingComboRepository.save(bookingCombo);
            }
        }

        // Create and save payment record
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString().substring(0, 10));
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(finalTotalPrice);
        payment.setPaymentMethod(form.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());

        // Handle payment method
        Object result;
        try {
            if (form.getPaymentMethod() == PaymentMethod.CASH) {
                payment.setStatus(PaymentStatus.SUCCESS.toString());
                paymentRepository.save(payment);
                booking.setStatus(BookingStatus.SUCCESS);
                bookingRepository.save(booking);

                // Deduct used points immediately for CASH payment (if account exists)
                if (account != null && usedPoints > 0) {
                    int currentPoints = account.getPoints() != null ? account.getPoints() : 0;
                    int newPoints = currentPoints - usedPoints;
                    if (newPoints < 0) {
                        throw new RuntimeException("Không đủ điểm để trừ");
                    }
                    account.setPoints(newPoints);
                    accountRepository.save(account);
                }

                // Add points based on total price (if account exists)
                if (account != null) {
                    int pointsToAdd = (int) (finalTotalPrice * POINT_PERCENTAGE / POINTS_VALUE);
                    int newTotalPoints = (account.getPoints() != null ? account.getPoints() : 0) + pointsToAdd;
                    account.setPoints(newTotalPoints);
                    accountRepository.save(account);
                }

                result = booking;
            } else if (form.getPaymentMethod() == PaymentMethod.MOMO) {
                payment.setStatus("PENDING");
                paymentRepository.save(payment);
                String payUrl = moMoPaymentService.createPaymentRequest(
                        1L,
                        (long) finalTotalPrice,
                        booking.getBookingId());
                result = payUrl;
            } else {
                throw new RuntimeException("Phương thức thanh toán không hỗ trợ: " + form.getPaymentMethod());
            }
        } catch (Exception e) {
            // Manual rollback if payment fails
            paymentRepository.delete(payment);
            if (form.getSeatsIds() != null) {
                for (Integer seatId : form.getSeatsIds()) {
                    ScheduleSeat scheduleSeat = scheduleSeatRepository.findById(
                            bookingSeatRepository.findByBookingId(booking.getBookingId()).stream()
                                    .filter(bs -> bs.getScheduleSeat().getSeat().getSeatId().equals(seatId))
                                    .findFirst().map(BookingSeat::getScheduleSeatId)
                                    .orElseThrow(() -> new RuntimeException("Seat not found for rollback: " + seatId)))
                            .orElseThrow();
                    if (scheduleSeat.getSeatStatus() == 1) {
                        scheduleSeat.setSeatStatus(0);
                        scheduleSeatRepository.save(scheduleSeat);
                    }
                }
            }
            throw new RuntimeException("Error processing payment: " + e.getMessage(), e);
        }

        return result;
    }

    private String generateUniqueNumericBookingId() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 9000) + 1000;
        String baseId = String.valueOf(timestamp % 1000000000);
        String numericId = (baseId + random).substring(0, 10);

        while (bookingRepository.existsById(numericId)) {
            random = (int) (Math.random() * 9000) + 1000;
            numericId = (baseId + random).substring(0, 10);
        }

        return numericId;
    }

    public Booking confirmPayment(String bookingId, String paymentMethod) {
        Optional<Payment> paymentOpt = paymentRepository.findTopByBookingIdOrderByPaymentDateDesc(bookingId);
        Payment payment = paymentOpt
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (booking.getStatus() != BookingStatus.PENDING || !"PENDING".equals(payment.getStatus())) {
            throw new IllegalStateException("Booking or payment is not in PENDING status.");
        }

        payment.setPaymentMethod(
                paymentMethod != null ? PaymentMethod.valueOf(paymentMethod) : payment.getPaymentMethod());
        payment.setStatus(PaymentStatus.SUCCESS.toString());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.SUCCESS);
        bookingRepository.save(booking);

        // Deduct used points from customer account (for MOMO payment)
        String accountId = booking.getAccountId();
        if (accountId != null) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại: " + accountId));

            if (booking.getUsedPoints() != null && booking.getUsedPoints() > 0) {
                int currentPoints = account.getPoints() != null ? account.getPoints() : 0;
                int newPoints = currentPoints - booking.getUsedPoints();
                if (newPoints < 0) {
                    throw new RuntimeException("Không đủ điểm để trừ");
                }
                account.setPoints(newPoints);
                accountRepository.save(account);
            }

            // Add points based on total price
            int pointsToAdd = (int) (booking.getTotalPrice() * POINT_PERCENTAGE / POINTS_VALUE);
            int newTotalPoints = (account.getPoints() != null ? account.getPoints() : 0) + pointsToAdd;
            account.setPoints(newTotalPoints);
            accountRepository.save(account);
        }

        return booking;
    }

    public void cancelBooking(String bookingId) {
        Optional<Payment> paymentOpt = paymentRepository.findTopByBookingIdOrderByPaymentDateDesc(bookingId);
        Payment payment = paymentOpt
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (booking.getStatus() != BookingStatus.PENDING || !"PENDING".equals(payment.getStatus())) {
            throw new IllegalStateException("Booking or payment is not in PENDING status.");
        }

        payment.setStatus(PaymentStatus.FAILED.toString());
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason("Hủy do không thanh toán");
        bookingRepository.save(booking);

        // Release seats
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bs : bookingSeats) {
            ScheduleSeat scheduleSeat = scheduleSeatRepository.findById(bs.getScheduleSeatId())
                    .orElseThrow(() -> new RuntimeException("Schedule seat not found"));
            scheduleSeat.setSeatStatus(0); // Release seat
            scheduleSeatRepository.save(scheduleSeat);
        }
    }

    public void cancelBookingByAdmin(String bookingId, String reason) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        if (booking.getStatus() != BookingStatus.CANCELLED) { // Chỉ hủy nếu chưa bị hủy
            Optional<Payment> paymentOpt = paymentRepository.findTopByBookingIdOrderByPaymentDateDesc(bookingId);
            paymentOpt.ifPresent(payment -> {
                payment.setStatus(PaymentStatus.FAILED.toString());
                paymentRepository.save(payment);
            });

            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelReason(reason != null ? reason : "Hủy bởi admin");
            bookingRepository.save(booking);

            // Release seats
            List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
            for (BookingSeat bs : bookingSeats) {
                ScheduleSeat scheduleSeat = scheduleSeatRepository.findById(bs.getScheduleSeatId()).orElseThrow();
                scheduleSeat.setSeatStatus(0); // Release seat
                scheduleSeatRepository.save(scheduleSeat);
            }
        }
    }

    public Map<Integer, Map<String, SeatScheduleDTO>> getAvailableSeatsByScheduleIdAndMovieIdAndShowdate(
            Integer scheduleId, String movieId,
            Integer showDateId) {
        List<SeatScheduleDTO> scheduleSeatDTOs = scheduleSeatRepository
                .findSeatsByMovieScheduleAndShowDate(movieId, scheduleId, showDateId).stream()
                .map(SeatMapper::toSeatScheduleDTO).toList();
        if (scheduleSeatDTOs.isEmpty()) {
            throw new RuntimeException("Không có ghế nào khả dụng cho lịch chiếu này");
        }
        Map<Integer, Map<String, SeatScheduleDTO>> scheduleSeatMap = new HashMap<>();
        for (SeatScheduleDTO seat : scheduleSeatDTOs) {
            scheduleSeatMap.computeIfAbsent(seat.getSeatRow(), r -> new HashMap<>())
                    .put(seat.getSeatColumn(), seat);
        }
        return scheduleSeatMap;
    }

    public List<MovieSchedule> getSchedulesByMovieAndDate(String movieId, LocalDate date) {
        return movieScheduleRepository.findByMovieMovieIdAndShowDatesShowDate(movieId, date);
    }
}