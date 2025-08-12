package com.example.projectwebmovie.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class PagedTickets {
    private List<MyTicketDTO> tickets;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;
}
