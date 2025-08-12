package com.example.projectwebmovie.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "MOVIETHEATER_TICKET")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    @Id
    @Column(name = "TICKET_ID")
    private Integer ticketId; // Mã loại vé, khóa chính

    @Column(name = "TICKET_TYPE_NAME", length = 50)
    private String ticketTypeName; // Tên loại vé (REGULAR, VIP, CHILD, STUDENT), tối đa 50 ký tự (bổ sung)

    @Column(name = "PRICE")
    private Integer price; // Giá vé (ví dụ: 100000 cho vé thường)

    @Column(name = "TICKET_TYPE")
    private Integer ticketType; // Mã loại vé (có thể bỏ nếu không cần)

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingSeat> bookingSeats; // Danh sách ghế được đặt với loại vé này, quan hệ một-nhiều (bổ sung)
}