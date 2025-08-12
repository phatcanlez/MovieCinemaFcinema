package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountStaticsResponseDTO {
    private int totalMembers; // Tổng số thành viên trong hệ thống
    private int increasedMembers; // Số lượng thành viên tăng thêm trong tháng (nếu giảm để số âm)
}
