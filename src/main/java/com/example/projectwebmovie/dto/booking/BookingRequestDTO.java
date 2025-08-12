package com.example.projectwebmovie.dto.booking;

import com.example.projectwebmovie.enums.PaymentMethod;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequestDTO {
    private String movieId;
    private Integer showDateId;
    private Integer scheduleId;
    private List<Integer> seatsIds; // Danh sách ID ghế được chọn
    private Integer usedPoints; // Số điểm người dùng muốn sử dụng, nullable
    private List<BookingComboDTO> combos; // Danh sách ID combo được chọn
    private PaymentMethod paymentMethod; // Phương thức thanh toán
    private Integer totalPrice; // Tổng giá tiền, do FE tính và gửi về
    private String promotionId; // Mã khuyến mãi (thêm mới)
    private String accountId; // ID tài khoản khách hàng (dành cho nhân viên đặt vé)
}