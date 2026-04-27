package com.example.bookingtour.services;

import com.example.bookingtour.dtos.request.support.SupportTicketCreateRequest;
import com.example.bookingtour.dtos.request.support.SupportTicketProcessRequest;
import com.example.bookingtour.dtos.response.support.SupportTicketResponse;
import com.example.bookingtour.entities.Booking;
import com.example.bookingtour.entities.CustomerProfile;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.entities.SupportTicket;
import com.example.bookingtour.enums.TicketPriority;
import com.example.bookingtour.enums.TicketStatus;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.CustomerProfileRepository;
import com.example.bookingtour.repositories.StaffProfileRepository;
import com.example.bookingtour.repositories.SupportTicketRepository;
import com.example.bookingtour.IServices.ISupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements ISupportTicketService {

    private final SupportTicketRepository ticketRepository;
    private final CustomerProfileRepository customerRepository;
    private final StaffProfileRepository staffRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public SupportTicketResponse createTicket(SupportTicketCreateRequest request, Integer userId) {

        CustomerProfile customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ Khách hàng"));


        Booking booking = null;
        if (request.getBookingId() != null) {
            booking = bookingRepository.findById(request.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy Booking"));
        }


        SupportTicket ticket = SupportTicket.builder()
                .customer(customer)
                .booking(booking)
                .subject(request.getSubject())
                .description(request.getDescription())
                .priority(request.getPriority() != null ? request.getPriority() : TicketPriority.LOW)
                .status(TicketStatus.OPEN)
                .build();

        ticketRepository.save(ticket);
        return SupportTicketResponse.fromSupportTicket(ticket);
    }

    @Override
    public List<SupportTicketResponse> getMyTickets(Integer userId) {
        List<SupportTicket> tickets = ticketRepository.findByCustomer_User_IdOrderByCreatedAtDesc(userId);
        return tickets.stream()
                .map(SupportTicketResponse::fromSupportTicket)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupportTicketResponse> getAllTicketsForAdmin(TicketStatus status) {
        List<SupportTicket> tickets;
        if (status != null) {
            tickets = ticketRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {

            tickets = ticketRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        }
        return tickets.stream()
                .map(SupportTicketResponse::fromSupportTicket)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportTicketResponse processTicket(Integer ticketId, SupportTicketProcessRequest request, Integer adminId) {

        SupportTicket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Ticket"));

        StaffProfile staff = staffRepository.findByUser_Id(adminId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hồ sơ Nhân viên"));

        ticket.setAssignedStaff(staff);
        ticket.setStatus(request.getStatus());
        ticket.setAdminResponse(request.getResponseMessage());

        if (request.getStatus() == TicketStatus.CLOSED) {
            ticket.setClosedAt(Instant.now());
        }

        ticketRepository.save(ticket);
        return SupportTicketResponse.fromSupportTicket(ticket);
    }
}