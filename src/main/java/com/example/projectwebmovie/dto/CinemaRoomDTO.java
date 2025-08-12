package com.example.projectwebmovie.dto;

import lombok.*;

@Data
@NoArgsConstructor
@Builder
public class CinemaRoomDTO {
    private Integer cinemaRoomId; // Mã phòng chiếu, khóa chính
    private String cinemaRoomName; // Tên phòng chiếu (ví dụ: "Phòng 1"), tối đa 255 ký t
    private Long seatQuantity;
    private boolean isActive; // Trạng thái hoạt động của phòng chiếu

    public CinemaRoomDTO(Integer cinemaRoomId, String cinemaRoomName, Long seatQuantity, boolean isActive) {
        this.cinemaRoomId = cinemaRoomId;
        this.cinemaRoomName = cinemaRoomName;
        this.seatQuantity = seatQuantity;
        this.isActive = isActive;
    }
}
