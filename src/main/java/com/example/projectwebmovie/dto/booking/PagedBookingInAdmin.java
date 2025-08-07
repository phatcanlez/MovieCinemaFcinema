package com.example.projectwebmovie.dto.booking;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PagedBookingInAdmin {
    List<BookingAdminDTO> bookings;
    Integer totalPages;
    Long totalCount;
    Integer currentPage;
    Integer pageSize;
    Long totalCompletedBookings;
    Long totalCancelledBookings;
    Long totalPendingBookings;
}
