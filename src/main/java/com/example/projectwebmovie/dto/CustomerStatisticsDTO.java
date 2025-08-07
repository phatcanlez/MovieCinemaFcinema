package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatisticsDTO {
    private Long totalCustomers;
    private Long totalActiveCustomers;
}