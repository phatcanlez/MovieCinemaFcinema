package com.example.projectwebmovie.repository;

import com.example.projectwebmovie.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket, Integer> {
}
