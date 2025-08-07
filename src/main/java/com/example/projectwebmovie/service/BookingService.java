package com.example.projectwebmovie.service;

import com.example.projectwebmovie.dto.booking.BookingAdminDTO;
import com.example.projectwebmovie.dto.booking.BookingComboDTO;
import com.example.projectwebmovie.dto.booking.BookingDetailDTO;
import com.example.projectwebmovie.dto.booking.BookingRequestDTO;
import com.example.projectwebmovie.dto.booking.PagedBookingInAdmin;
import com.example.projectwebmovie.dto.SeatScheduleDTO;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BookingService {

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
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingComboRepository bookingComboRepository;

    @Autowired
    private MoMoPaymentService moMoPaymentService;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private AccountService accountService;

    private static final long PAYMENT_TIMEOUT_MINUTES = 3;
    private static final int POINTS_VALUE = 10; // 1 điểm = 10.000 VND
    private static final double POINT_PERCENTAGE = 0.10; // 10% số tiền được cộng thành điểm

    public Object bookTicket(BookingRequestDTO form) {
        System.out.println("BookingRequestDTO: " + form);

        String accountId = accountService.getCurrentAccount().getAccountId();
        // Validate required fields
        if (accountId == null || form.getMovieId() == null || form.getScheduleId() == null
                || form.getShowDateId() == null || form.getTotalPrice() == null) {
            throw new RuntimeException("Thông tin đặt vé không hợp lệ, 1 giá trị không được để trống");
        }

        // Fetch the show date to convert showDateId to LocalDate
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

        System.out.println("Kiểm tra lịch chiếu: MovieSchedule ID = " + movieSchedule.getId());

        // Fetch movie to get base price
        Movie movie = movieRepository.findByMovieIdWithPrice(form.getMovieId())
                .orElseThrow(() -> new RuntimeException("Phim không tồn tại: " + form.getMovieId()));

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
                    .toList();
            List<Integer> requestedSeatIds = form.getSeatsIds();
            if (!availableSeatIds.containsAll(requestedSeatIds)) {
                List<Integer> unavailableSeatIds = requestedSeatIds.stream()
                        .filter(seatId -> !availableSeatIds.contains(seatId))
                        .toList();
                throw new RuntimeException("Các ghế không khả dụng hoặc đã bị đặt: " + unavailableSeatIds);
            }

            for (Integer seatId : requestedSeatIds) {
                ScheduleSeat seat = availableSeats.stream()
                        .filter(ss -> ss.getSeat().getSeatId().equals(seatId))
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("Ghế không tìm thấy trong danh sách sẵn có: " + seatId));
                Integer seatStatus = seat.getSeatStatus();
                if (seatStatus == null || seatStatus != 0) {
                    throw new RuntimeException(
                            "Ghế đã được book hoặc không hợp lệ: " + seatId + " (seat_status = " + seatStatus + ")");
                }
                System.out.println("Kiểm tra ghế: Seat ID = " + seat.getSeat().getSeatId()
                        + ", Price = " + seat.getSeatPrice());
                basePrice += seat.getSeatPrice();
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
                System.out.println("Kiểm tra combo: Combo ID = " + combo.getComboId()
                        + ", Discounted Price = " + combo.getDiscountedPrice() + ", Quantity = " + quantity);
                comboPrice += combo.getDiscountedPrice() * quantity;
            }
        }

        // Calculate base total price with points deduction
        double baseTotalPrice = basePrice + comboPrice
                - (form.getUsedPoints() != null ? form.getUsedPoints() * POINTS_VALUE : 0);
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
                    finalTotalPrice = Math.max(0, baseTotalPrice - discountAmount);
                }
            }
        }

        // So sánh totalPrice từ FE và BE (after promotion)
        int frontendTotalPrice = form.getTotalPrice();
        if (Math.abs(finalTotalPrice - frontendTotalPrice) > 0.10) {
            throw new RuntimeException("Total price mismatch: Frontend (" + frontendTotalPrice + ") vs Backend ("
                    + finalTotalPrice + ") after applying promotion");
        }

        // Fetch account and validate points based on accountId from DTO
        Account account = null;
        int availablePoints = 0;
        if (accountId != null && !accountId.startsWith("GUEST_")) {
            account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại: " + accountId));
            Optional<Integer> pointsOpt = accountRepository.findPointsByAccountId(accountId);
            availablePoints = pointsOpt.orElse(0);
        }
        int usedPoints = form.getUsedPoints() != null ? Math.min(form.getUsedPoints(), availablePoints) : 0;
        if (account != null && usedPoints > availablePoints) {
            throw new RuntimeException("Số điểm sử dụng vượt quá điểm hiện có: " + availablePoints);
        }

        // Create new booking with unique 10-digit numeric bookingId
        String bookingId = generateUniqueNumericBookingId();
        Booking booking = new Booking();
        booking.setBookingId(bookingId);
        booking.setAccountId(accountId);
        booking.setMovieId(form.getMovieId());
        booking.setScheduleId(form.getScheduleId());
        booking.setShowDateId(form.getShowDateId());
        booking.setPromotionId(promotionId); // Set promotionId if provided
        booking.setBookingDate(LocalDateTime.now());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(finalTotalPrice); // Use final total price after promotion
        booking.setUsedPoints(usedPoints);

        // Save booking
        booking = bookingRepository.save(booking);
        System.out.println("Booking saved: Booking ID = " + booking.getBookingId());

        // Create and save booking seats with seat_status update
        if (form.getSeatsIds() != null) {
            for (Integer seatId : form.getSeatsIds()) {
                ScheduleSeat scheduleSeat = availableSeats.stream()
                        .filter(ss -> ss.getSeat().getSeatId().equals(seatId))
                        .findFirst()
                        .orElseThrow(
                                () -> new RuntimeException("Ghế không tìm thấy trong danh sách sẵn có: " + seatId));
                Integer seatStatus = scheduleSeat.getSeatStatus();
                if (seatStatus == null || seatStatus != 0) {
                    throw new RuntimeException(
                            "Ghế đã được book hoặc không hợp lệ: " + seatId + " (seat_status = " + seatStatus + ")");
                }
                scheduleSeat.setSeatStatus(1); // Mark as booked
                ScheduleSeat savedScheduleSeat = scheduleSeatRepository.save(scheduleSeat);
                System.out.println(
                        "ScheduleSeat updated to booked: ScheduleSeat ID = " + savedScheduleSeat.getScheduleSeatId());

                BookingSeat bookingSeat = new BookingSeat();
                bookingSeat.setBookingId(booking.getBookingId());
                bookingSeat.setScheduleSeatId(savedScheduleSeat.getScheduleSeatId());
                bookingSeat.setBooking(booking);
                bookingSeat.setScheduleSeat(savedScheduleSeat);

                bookingSeatRepository.save(bookingSeat);
                System.out.println("BookingSeat saved: BookingSeat ID = " + bookingSeat.getScheduleSeatId());
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
                System.out.println(
                        "BookingCombo saved: bookingId=" + booking.getBookingId() + ", comboId=" + combo.getComboId()
                                + ", quantity=" + quantity);
            }
        }

        // Create and save payment record
        Payment payment = new Payment();
        payment.setPaymentId(UUID.randomUUID().toString().substring(0, 10));
        payment.setBookingId(booking.getBookingId());
        payment.setAmount(finalTotalPrice); // Use final total price after promotion
        payment.setPaymentMethod(form.getPaymentMethod() != null ? form.getPaymentMethod() : PaymentMethod.CASH);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        // Handle payment method
        Object result;
        try {
            if (form.getPaymentMethod() != null && form.getPaymentMethod() == PaymentMethod.MOMO) {
                // Call MoMo payment request
                if (finalTotalPrice <= 0) {
                    payment.setStatus(PaymentStatus.SUCCESS.toString());
                    paymentRepository.save(payment);
                    booking.setStatus(BookingStatus.SUCCESS);
                    bookingRepository.save(booking);
                    return booking;
                }
                String payUrl = moMoPaymentService.createPaymentRequest(
                        1L, // Placeholder invoiceId (replace with actual logic if needed)
                        finalTotalPrice, // Sử dụng giá trị từ BE sau khi áp dụng khuyến mãi
                        booking.getBookingId());
                result = payUrl; // Trả về URL để redirect đến MoMo
            } else if (form.getPaymentMethod() != null && form.getPaymentMethod() == PaymentMethod.CASH) { // Default to
                                                                                                           // CASH or
                                                                                                           // other
                                                                                                           // methods
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
                // Add points based on total price
                if (account != null && booking.getStatus() == BookingStatus.SUCCESS) {
                    int pointsToAdd = (int) (finalTotalPrice * POINT_PERCENTAGE / POINTS_VALUE);
                    int newTotalPoints = (account.getPoints() != null ? account.getPoints() : 0) + pointsToAdd;
                    account.setPoints(newTotalPoints);
                    accountRepository.save(account);
                    System.out.println("Added " + pointsToAdd + " points for bookingId: " + booking.getBookingId());
                }
                System.out.println("Payment confirmed:" + booking);
                result = booking; // Trả về booking nếu thanh toán ngay
            } else {
                throw new RuntimeException("Phương thức thanh toán không hợp lệ: " + form.getPaymentMethod());
            }
        } catch (Exception e) {
            // Manual rollback if payment fails
            paymentRepository.delete(payment); // Rollback Payment
            // Rollback seat_status if needed
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

        // Schedule cancellation if not paid within 10 minutes
        if (booking.getStatus() == BookingStatus.PENDING) {
            final String finalBookingId = bookingId; // Ensure final for lambda
            final LocalDateTime finalPaymentDate = payment.getPaymentDate(); // Ensure final for lambda
            scheduleCancellation(finalBookingId, finalPaymentDate);
        }

        return result; // Trả về payUrl hoặc booking tùy phương thức thanh toán
    }

    private String generateUniqueNumericBookingId() {
        long timestamp = System.currentTimeMillis(); // Lấy timestamp hiện tại (13 chữ số)
        int random = (int) (Math.random() * 9000) + 1000; // Thêm 4 số ngẫu nhiên (1000-9999)
        String baseId = String.valueOf(timestamp % 1000000000); // Lấy 9 số cuối của timestamp
        String numericId = (baseId + random).substring(0, 10); // Kết hợp và cắt thành 10 số

        // Kiểm tra trùng lặp trong cơ sở dữ liệu
        while (bookingRepository.existsById(numericId)) {
            random = (int) (Math.random() * 9000) + 1000; // Tạo số ngẫu nhiên mới
            numericId = (baseId + random).substring(0, 10); // Tạo lại ID
        }

        return numericId;
    }

    public Booking confirmPayment(String bookingId, String paymentMethod) {
        Optional<Payment> paymentOpt = paymentRepository.findTopByBookingIdOrderByPaymentDateDesc(bookingId);
        Payment payment = paymentOpt
                .orElseThrow(() -> new RuntimeException("No payment found for booking: " + bookingId));
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));
        // Kiểm tra trạng thái của booking và payment
        if (booking == null || payment == null) {
            throw new RuntimeException("Booking or payment not found for bookingId: " + bookingId);
        }
        if (paymentMethod == null || paymentMethod.toUpperCase().equals("MOMO")) {
            if (booking.getStatus() != BookingStatus.PENDING || !"PENDING".equals(payment.getStatus())) {
                throw new IllegalStateException("Booking or payment is not in PENDING status.");
            }
        }

        payment.setPaymentMethod(
                paymentMethod != null ? PaymentMethod.valueOf(paymentMethod) : payment.getPaymentMethod());
        payment.setStatus(PaymentStatus.SUCCESS.toString());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        booking.setStatus(BookingStatus.SUCCESS);
        bookingRepository.save(booking);

        // Deduct used points from account based on accountId from DTO
        String accountId = booking.getAccountId();
        if (accountId != null && !accountId.startsWith("GUEST_")) {
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
        }

        // Add points based on total price
        if (accountId != null && !accountId.startsWith("GUEST_") && booking.getStatus() == BookingStatus.SUCCESS) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại: " + accountId));
            int pointsToAdd = (int) (booking.getTotalPrice() * POINT_PERCENTAGE / POINTS_VALUE);
            int newTotalPoints = (account.getPoints() != null ? account.getPoints() : 0) + pointsToAdd;
            account.setPoints(newTotalPoints);
            accountRepository.save(account);
            System.out.println("Added " + pointsToAdd + " points for bookingId: " + booking.getBookingId());
        }

        return booking; // Trả về booking để sử dụng trong controller
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

            // Refund used points if any
            String accountId = booking.getAccountId();
            if (accountId != null && !accountId.startsWith("GUEST_")) {
                Account account = accountRepository.findById(accountId)
                        .orElseThrow(() -> new RuntimeException("Account not found for booking: " + bookingId));
                if (booking.getUsedPoints() != null && booking.getUsedPoints() > 0) {
                    int currentPoints = account.getPoints() != null ? account.getPoints() : 0;
                    account.setPoints(currentPoints + booking.getUsedPoints());
                    accountRepository.save(account);
                }
            }
        }
    }

    private void scheduleCancellation(String bookingId, LocalDateTime paymentDate) {
        new Thread(() -> {
            try {
                System.out.println("Scheduling cancellation for booking: " + bookingId);
                TimeUnit.MINUTES.sleep(PAYMENT_TIMEOUT_MINUTES);
                Booking booking = bookingRepository.findById(bookingId).orElse(null);
                if (booking != null && booking.getStatus() == BookingStatus.PENDING) {
                    LocalDateTime now = LocalDateTime.now();
                    if (now.isAfter(paymentDate.plusMinutes(PAYMENT_TIMEOUT_MINUTES))) {
                        cancelBooking(bookingId);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
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
        booking.setCancelReason("Hủy do không thanh toán trong 10 phút");
        bookingRepository.save(booking);

        // Release seats
        List<BookingSeat> bookingSeats = bookingSeatRepository.findByBookingId(bookingId);
        for (BookingSeat bs : bookingSeats) {
            ScheduleSeat scheduleSeat = scheduleSeatRepository.findById(bs.getScheduleSeatId())
                    .orElseThrow(() -> new RuntimeException("Schedule seat not found"));
            scheduleSeat.setSeatStatus(0); // Release seat
            scheduleSeatRepository.save(scheduleSeat);
        }

        // Refund used points if any
        String accountId = booking.getAccountId();
        if (accountId != null && !accountId.startsWith("GUEST_")) {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại: " + accountId));
            if (booking.getUsedPoints() != null && booking.getUsedPoints() > 0) {
                int currentPoints = account.getPoints() != null ? account.getPoints() : 0;
                account.setPoints(currentPoints + booking.getUsedPoints());
                accountRepository.save(account);
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

    public PagedBookingInAdmin getAllBookingByAdmin(int page, int size, String searchTerm, String status) {
        if (page < 0)
            page = 0;
        if (size <= 0)
            size = 10;

        int offset = page * size;

        // Chuyển đổi status từ String sang enum nếu cần
        String statusValue = null;
        if (status != null && !status.isEmpty()) {
            statusValue = status;
        }

        // Search term trống thì set null
        String searchTermValue = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;

        // Lấy tổng số booking và phân trang
        Long totalCount = bookingRepository.countAllBookings();
        int totalPages = (int) Math.ceil((double) totalCount / size);

        // Điều chỉnh page nếu vượt quá tổng số trang
        if (page >= totalPages && totalPages > 0) {
            page = totalPages - 1;
            offset = page * size;
        }

        List<Booking> bookings = bookingRepository.findBookingsWithFilters(
                searchTermValue, statusValue, size, offset);

        // Ánh xạ sang DTO
        List<BookingAdminDTO> bookingAdminDTOs = bookings.stream()
                .map(booking -> {
                    String movieName = booking.getMovie() != null ? booking.getMovie().getMovieNameVn() : "Unknown";
                    LocalDate showDate = booking.getShowDates() != null ? booking.getShowDates().getShowDate() : null;
                    LocalTime showTime = booking.getSchedule() != null
                            ? LocalTime.parse(booking.getSchedule().getScheduleTime())
                            : null;
                    String roomName = booking.getSchedule() != null
                            && !booking.getSchedule().getMovieSchedules().isEmpty()
                                    ? booking.getSchedule().getMovieSchedules().get(0).getCinemaRoom()
                                            .getCinemaRoomName()
                                    : "Unknown";
                    String bookingStatus = booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN";

                    return new BookingAdminDTO(
                            booking.getBookingId(),
                            movieName,
                            showDate,
                            showTime,
                            roomName,
                            booking.getTotalPrice(),
                            bookingStatus);
                })
                .collect(Collectors.toList());

        // Lấy thống kê
        Long totalCompletedBookings = bookingRepository.countSuccessfulBookings();
        Long totalCancelledBookings = bookingRepository.countCancelledBookings();
        Long totalPendingBookings = bookingRepository.countPendingBookings();

        // Tạo và trả về đối tượng PagedBookingInAdmin
        return new PagedBookingInAdmin(
                bookingAdminDTOs,
                totalPages,
                totalCount,
                page,
                size,
                totalCompletedBookings,
                totalCancelledBookings,
                totalPendingBookings);
    }

    public BookingDetailDTO getBookingDetailById(String bookingId) {
        // Tìm booking theo ID
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with ID: " + bookingId));

        BookingDetailDTO dto = new BookingDetailDTO();

        dto.setBookingId(booking.getBookingId());
        dto.setBookingDate(booking.getBookingDate());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN");
        dto.setTotalAmount(booking.getTotalPrice());
        dto.setUsedPoints(booking.getUsedPoints());

        // Customer Information
        if (booking.getAccount() != null) {
            Account account = booking.getAccount();
            dto.setCustomerId(account.getAccountId());
            dto.setCustomerName(account.getFullName());
            dto.setCustomerEmail(account.getEmail());
            dto.setCustomerPhone(account.getPhoneNumber());
            dto.setMemberPoints(account.getPoints());
        } else {
            dto.setCustomerId("N/A");
            dto.setCustomerName("N/A");
            dto.setCustomerEmail("N/A");
            dto.setCustomerPhone("N/A");
            dto.setMemberPoints(0);
        }

        // Movie Information
        if (booking.getMovie() != null) {
            dto.setMovieName(booking.getMovie().getMovieNameVn());
        } else {
            dto.setMovieName("Unknown");
        }

        // Show Date Information
        if (booking.getShowDates() != null) {
            dto.setShowDate(booking.getShowDates().getShowDate());
        }

        // Schedule Information
        if (booking.getSchedule() != null) {
            try {
                dto.setShowTime(LocalTime.parse(booking.getSchedule().getScheduleTime()));
            } catch (Exception e) {
                dto.setShowTime(null);
            }

            // Room Information
            if (!booking.getSchedule().getMovieSchedules().isEmpty()) {
                dto.setRoomName(booking.getSchedule().getMovieSchedules().get(0)
                        .getCinemaRoom().getCinemaRoomName());
            } else {
                dto.setRoomName("Unknown");
            }
        }

        // Payment Method from Payment table
        if (booking.getPayments() != null && !booking.getPayments().isEmpty()) {
            dto.setPaymentMethod(booking.getPayments().get(0).getPaymentMethod().name());
        } else {
            dto.setPaymentMethod("N/A");
        }

        // Seats Information
        if (booking.getBookingSeats() != null && !booking.getBookingSeats().isEmpty()) {
            String seats = booking.getBookingSeats().stream()
                    .map(bs -> bs.getScheduleSeat().getSeat().getSeatRow() +
                            bs.getScheduleSeat().getSeat().getSeatColumn())
                    .collect(Collectors.joining(", "));
            dto.setSeats(seats);

            // Calculate ticket price
            double ticketPrice = booking.getBookingSeats().stream()
                    .mapToDouble(bs -> bs.getScheduleSeat().getSeatPrice())
                    .sum();
            dto.setTicketPrice(ticketPrice);
        } else {
            dto.setSeats("N/A");
            dto.setTicketPrice(0.0);
        }

        // Combo Information
        List<BookingDetailDTO.ComboDetailDTO> combos = new ArrayList<>();
        double totalComboPrice = 0.0;

        if (booking.getBookingCombos() != null && !booking.getBookingCombos().isEmpty()) {
            for (BookingCombo bc : booking.getBookingCombos()) {
                Combo combo = bc.getCombo();
                if (combo != null) {
                    double unitPrice = combo.getDiscountedPrice() != null ? combo.getDiscountedPrice()
                            : combo.getPrice();
                    double comboTotalPrice = unitPrice * bc.getQuantity();
                    totalComboPrice += comboTotalPrice;

                    combos.add(new BookingDetailDTO.ComboDetailDTO(
                            combo.getComboName(),
                            bc.getQuantity(),
                            unitPrice,
                            comboTotalPrice));
                }
            }
        }
        dto.setCombos(combos);
        dto.setFoodBeveragePrice(totalComboPrice);

        // Promotion Information
        if (booking.getPromotion() != null) {
            dto.setPromotionCode(booking.getPromotion().getPromotionCode());
            dto.setPromotionName(booking.getPromotion().getTitle());
            if (booking.getPromotion().getDiscountAmount() != null) {
                dto.setPromotionDiscount(booking.getPromotion().getDiscountAmount());
            } else if (booking.getPromotion().getDiscountLevel() != null) {
                double discountAmount = booking.getTotalPrice() * (booking.getPromotion().getDiscountLevel() / 100.0);
                if (booking.getPromotion().getMaxAmountForPercentDiscount() != null) {
                    discountAmount = Math.min(discountAmount, booking.getPromotion().getMaxAmountForPercentDiscount());
                }
                dto.setPromotionDiscount(discountAmount);
            } else {
                dto.setPromotionDiscount(0.0);
            }
        }

        // Calculate discount amount
        double pointsDiscount = booking.getUsedPoints() != null ? booking.getUsedPoints() * 10 : 0;
        double promotionDiscount = dto.getPromotionDiscount() != null ? dto.getPromotionDiscount() : 0;
        dto.setDiscountAmount(pointsDiscount + promotionDiscount);

        return dto;
    }
}