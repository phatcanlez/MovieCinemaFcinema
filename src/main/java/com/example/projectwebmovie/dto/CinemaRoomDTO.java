package com.example.projectwebmovie.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CinemaRoomDTO {
    private Integer cinemaRoomId; // Mã phòng chiếu, khóa chính
    private String cinemaRoomName; // Tên phòng chiếu (ví dụ: "Phòng 1"), tối đa 255 ký t
    private Long seatQuantity;
}
