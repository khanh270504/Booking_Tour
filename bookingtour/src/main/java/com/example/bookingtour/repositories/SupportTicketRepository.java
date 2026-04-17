package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.SupportTicket;
import com.example.bookingtour.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Integer> {
    List<SupportTicket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<SupportTicket> findByCustomer_User_IdOrderByCreatedAtDesc(Integer userId);
}