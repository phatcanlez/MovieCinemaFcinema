package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_CINEMA_ROOM")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CinemaRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CINEMA_ROOM_ID")
    private Integer cinemaRoomId; // Mã phòng chiếu, khóa chính

    @Column(name = "CINEMA_ROOM_NAME", length = 255)
    private String cinemaRoomName; // Tên phòng chiếu (ví dụ: "Phòng 1"), tối đa 255 ký tự

    @OneToMany(mappedBy = "cinemaRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats; // Danh sách ghế trong phòng chiếu, quan hệ một-nhiều

    @OneToMany(mappedBy = "cinemaRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MovieSchedule> movieSchedules; // Danh sách lịch chiếu trong phòng chiếu, quan hệ một-nhiều (bổ sung)

    @Column(name = "IS_ACTIVE", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean isActive;

    @Override
    public String toString() {
        return "CinemaRoom{cinemaRoomId=" + cinemaRoomId + ", cinemaRoomName='" + cinemaRoomName + "'}";
    }
}