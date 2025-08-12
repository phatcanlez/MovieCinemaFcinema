package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopCustomerDTO {
    private String accountId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private Long totalBookings;
    private Double totalSpent;
}