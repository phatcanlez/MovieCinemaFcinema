package com.example.projectwebmovie.controller;

import com.example.projectwebmovie.enums.PaymentMethod;
import com.example.projectwebmovie.model.Booking;
import com.example.projectwebmovie.model.Payment;
import com.example.projectwebmovie.service.BookingService;

import com.example.projectwebmovie.service.EmailService;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/booking")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/redirect")
    public String handlePaymentRedirect(
            @RequestParam String resultCode,
            @RequestParam String orderInfo,
            Model model) {
        if (orderInfo.isEmpty()) {
            System.out.println("Order info is empty");
            return "redirect:/api/booking/failure"; // Chuyển hướng đến trang thất bại
        }

        try {
            if (resultCode.equals("0")) {
                // Thanh toán thành công
                Booking booking = bookingService.confirmPayment(orderInfo, null);
                List<HashMap<String, Object>> seats = booking.getBookingSeats().stream()
                        .map(seat -> {
                            HashMap<String, Object> seatMap = new HashMap();
                            seatMap.put("seatColRow",
                                    seat.getScheduleSeat().getSeatRow() + seat
                                            .getScheduleSeat()
                                            .getSeatColumn());
                            seatMap.put("price", seat.getScheduleSeat().getSeatPrice());
                            return seatMap;
                        })
                        .collect(Collectors.toList());
                // bookingId, movie, showDate, schedule, seats, totalPrice, roomName,
                // totalseats, ghế

                // Gửi email xác nhận đặt vé
//                emailService.sendPaymentConfirmationEmail(booking.getAccount().getEmail(), booking);


                model.addAttribute("booking", booking);
                model.addAttribute("combos",
                        booking.getBookingCombos().stream()
                                .map(m -> m.getCombo().getComboName() + " x "
                                        + m.getQuantity() + " = "
                                        + m.getCombo().getDiscountedPrice()
                                        * m.getQuantity())
                                .collect(Collectors.toList()));
                model.addAttribute("movie", booking.getMovie());
                model.addAttribute("showDate", booking.getShowDates().getShowDate());
                model.addAttribute("showTime",
                        LocalTime.parse(booking.getSchedule().getScheduleTime()));
                model.addAttribute("roomName",
                        booking.getSchedule().getMovieSchedules().get(0).getCinemaRoom()
                                .getCinemaRoomName());
                model.addAttribute("seats", seats); // Giả sử có phương thức getSeats()
                model.addAttribute("totalAmount", booking.getTotalPrice());
                model.addAttribute("ticketId", booking.getBookingId());

                return "home/booking/booking-success"; // Render template success
            } else {
                // Thanh toán thất bại
                bookingService.cancelBooking(orderInfo);


                return "redirect:/booking/failure"; // Chuyển hướng đến trang thất bại
            }
        } catch (Exception e) {
            System.out.println("Error processing booking: " + e.getMessage());
            // bookingService.cancelBooking(orderInfo); // Hủy booking nếu có lỗi
            return "redirect:/booking/failure";
        }
    }

    @GetMapping("/confirmation/{bookingId}")
    public String handleCashPayment(@PathVariable("bookingId") String bookingId, Model model) {
        if (bookingId == null || bookingId.isEmpty()) {
            System.out.println("Booking ID is empty");
            return "redirect:/booking/failure"; // Chuyển hướng đến trang thất bại
        }
        // Thanh toán thành công
        Booking booking = bookingService.confirmPayment(bookingId, PaymentMethod.CASH.toString());
        List<HashMap<String, Object>> seats = booking.getBookingSeats().stream()
                .map(seat -> {
                    HashMap<String, Object> seatMap = new HashMap();
                    seatMap.put("seatColRow",
                            seat.getScheduleSeat().getSeatRow()
                                    + seat.getScheduleSeat().getSeatColumn());
                    seatMap.put("price", seat.getScheduleSeat().getSeatPrice());
                    return seatMap;
                })
                .collect(Collectors.toList());
        // bookingId, movie, showDate, schedule, seats, totalPrice, roomName,
        // totalseats, ghế

        model.addAttribute("booking", booking);
        model.addAttribute("combos",
                booking.getBookingCombos().stream()
                        .map(m -> m.getCombo().getComboName() + " x " + m.getQuantity() + " = "
                                + m.getCombo().getDiscountedPrice() * m.getQuantity())
                        .collect(Collectors.toList()));
        model.addAttribute("movie", booking.getMovie());
        model.addAttribute("showDate", booking.getShowDates().getShowDate());
        model.addAttribute("showTime", LocalTime.parse(booking.getSchedule().getScheduleTime()));
        model.addAttribute("roomName",
                booking.getSchedule().getMovieSchedules().get(0).getCinemaRoom().getCinemaRoomName());
        model.addAttribute("seats", seats); // Giả sử có phương thức getSeats()
        model.addAttribute("totalAmount", booking.getTotalPrice());
        model.addAttribute("ticketId", booking.getBookingId());

        return "home/booking/booking-success"; // Render template success
    }

    @GetMapping("/failure")
    public String bookingFailure(Model model) {
        model.addAttribute("message", "Booking failed. Please try again.");
        return "home/booking/booking-failure"; // Render template failure
    }
}