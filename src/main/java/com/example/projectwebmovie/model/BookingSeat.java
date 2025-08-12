package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MOVIETHEATER_BOOKING_SEAT")
@Data
@IdClass(BookingSeatId.class)
@NoArgsConstructor
@AllArgsConstructor
public class BookingSeat {

    @Id
    @Column(name = "BOOKING_ID", length = 10)
    private String bookingId; // Mã đặt vé, khóa chính, tối đa 10 ký tự

    @Id
    @Column(name = "SCHEDULE_SEAT_ID", length = 10)
    private String scheduleSeatId; // Mã ghế theo lịch chiếu, khóa chính, tối đa 10 ký tự

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_ID", referencedColumnName = "TICKET_ID", nullable = true) // Optional if ticket can be null
    private Ticket ticket; // Loại vé áp dụng, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "BOOKING_ID", referencedColumnName = "BOOKING_ID", insertable = false, updatable = false)
    private Booking booking; // Đặt vé liên quan, quan hệ nhiều-một

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHEDULE_SEAT_ID", referencedColumnName = "SCHEDULE_SEAT_ID", insertable = false, updatable = false)
    private ScheduleSeat scheduleSeat; // Ghế theo lịch chiếu liên quan, quan hệ nhiều-một

    @Override
    public String toString() {
        return "BookingSeat{bookingId='" + bookingId + "', scheduleSeatId='" + scheduleSeatId + "', ticketId='" + (ticket != null ? ticket.getTicketId() : "null") + "'}";
    }
}