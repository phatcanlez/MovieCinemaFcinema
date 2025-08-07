package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_SEAT")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEAT_ID")
    private Integer seatId; // Mã ghế, khóa chính

    @Column(name = "CINEMA_ROOM_ID")
    private Integer cinemaRoomId; // Mã phòng chiếu, liên kết với MOVIETHEATER_CINEMA_ROOM

    @Column(name = "SEAT_COLUMN", length = 255)
    private String seatColumn; // Cột ghế (ví dụ: "A"), không được để trống

    @Column(name = "SEAT_ROW")
    private Integer seatRow; // Hàng ghế (ví dụ: 1), không được để trống

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

    @Column(name = "SEAT_TYPE_ID")
    private Integer seatTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SEAT_TYPE_ID", insertable = false, updatable = false)
    private SeatType seatType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CINEMA_ROOM_ID", insertable = false, updatable = false)
    private CinemaRoom cinemaRoom; // Phòng chiếu liên quan, quan hệ nhiều-một

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ScheduleSeat> scheduleSeats; // Danh sách ghế theo lịch chiếu, quan hệ một-nhiều (bổ sung)

    @Override
    public String toString() {
        return this.seatId + " - " + this.seatColumn + this.seatRow + " - " + (this.isActive) ;
    }
}