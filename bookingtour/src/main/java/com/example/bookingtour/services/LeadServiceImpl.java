package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IBookingService;
import com.example.bookingtour.IServices.ILeadService;
import com.example.bookingtour.dtos.request.crm.LeadCreateRequest;
import com.example.bookingtour.dtos.request.crm.LeadUpdateRequest;
import com.example.bookingtour.dtos.response.crm.LeadResponse;
import com.example.bookingtour.dtos.response.notification.NotificationResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.*;
import com.example.bookingtour.exceptions.AppException;
import com.example.bookingtour.exceptions.ErrorCode;
import com.example.bookingtour.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeadServiceImpl implements ILeadService {
    private final TourScheduleRepository tourScheduleRepository;

    private final CrmLeadRepository leadRepository;
    private final TourRepository tourRepository;
    private final StaffProfileRepository staffRepository;
    private final TaskServiceImpl taskService;
    private final NotificationRepository notificationRepository;
    private final CrmTaskRepository taskRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final IBookingService bookingService;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public LeadResponse createLead(LeadCreateRequest request) {
        Tour tour = request.getInterestedTourId() != null
                ? tourRepository.findById(request.getInterestedTourId()).orElse(null) : null;

        StaffProfile staff = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                staff = request.getAssignedStaffId() != null
                        ? staffRepository.findById(request.getAssignedStaffId()).orElse(null) : null;
            } else {
                Jwt jwt = (Jwt) auth.getPrincipal();
                Integer currentUserId = Math.toIntExact(jwt.getClaim("userId"));
                staff = staffRepository.findById(currentUserId).orElse(null);
            }
        }

        CrmLead lead = CrmLead.builder()
                .leadCode("LEAD-" + System.currentTimeMillis())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .source(request.getSource() != null ? LeadSource.valueOf(request.getSource().toUpperCase()) : null)
                .status(LeadStatus.NEW)
                .interestedTour(tour)
                .assignedStaff(staff)
                .notes(request.getNotes())
                .build();

        CrmLead savedLead = leadRepository.save(lead);

        try {
            if (staff != null && staff.getUser() != null) {
                Notification taskNoti = Notification.builder()
                        .title("Nhiệm vụ mới: Chăm sóc khách hàng")
                        .message(String.format("Bạn được chỉ định phụ trách khách hàng tiềm năng %s (%s). Vui lòng liên hệ tư vấn.",
                                savedLead.getFullName(), savedLead.getLeadCode()))
                        .type(NotificationType.TASK)
                        .user(staff.getUser())
                        .createdBy("CRM System")
                        .receiverRoleCode("ROLE_SALE")
                        .build();

                Notification savedNoti = notificationRepository.save(taskNoti);

                messagingTemplate.convertAndSend(
                        "/user/" + staff.getUser().getId() + "/queue/notifications",
                        NotificationResponse.fromEntity(savedNoti)
                );
                log.info("[REALTIME] Đã đẩy chuông nhiệm vụ mới đến Sale ID: {}", staff.getUser().getId());
            }
        } catch (Exception e) {
            log.error("Lỗi bắn thông báo tạo Lead: {}", e.getMessage());
        }

        return LeadResponse.fromLead(savedLead);
    }

    @Override
    @Transactional
    public LeadResponse updateLead(Integer id, LeadUpdateRequest request) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));

        StaffProfile oldStaff = lead.getAssignedStaff();

        if (request.getFullName() != null) lead.setFullName(request.getFullName());
        if (request.getPhone() != null) lead.setPhone(request.getPhone());
        if (request.getEmail() != null) lead.setEmail(request.getEmail());
        if (request.getSource() != null) lead.setSource(request.getSource());
        if (request.getPriority() != null) lead.setPriority(request.getPriority());
        if (request.getEstimatedPeople() != null) lead.setEstimatedPeople(request.getEstimatedPeople());
        if (request.getEstimatedBudget() != null) lead.setEstimatedBudget(request.getEstimatedBudget());
        if (request.getExpectedTravelDate() != null) lead.setExpectedTravelDate(request.getExpectedTravelDate());
        if (request.getNotes() != null) lead.setNotes(request.getNotes());

        if (request.getInterestedTourId() != null) {
            Tour tour = tourRepository.findById(request.getInterestedTourId()).orElse(null);
            lead.setInterestedTour(tour);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Integer currentUserId = null;
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof Jwt jwt) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            Long uId = jwt.getClaim("userId");
            if (uId != null) currentUserId = uId.intValue();

            if (isAdmin) {
                if (request.getAssignedStaffId() != null) {
                    StaffProfile newStaff = staffRepository.findById(request.getAssignedStaffId()).orElse(null);

                    if (newStaff != null && (oldStaff == null || !oldStaff.getId().equals(newStaff.getId()))) {
                        lead.setAssignedStaff(newStaff);

                        try {
                            List<CrmTask> activeTasks = taskRepository.findByLeadAndStatus(lead, TaskStatus.TODO);
                            if (!activeTasks.isEmpty()) {
                                for (CrmTask task : activeTasks) {
                                    task.setAssignedStaff(newStaff);
                                }
                                taskRepository.saveAll(activeTasks);
                                log.info("CRM System: Đã tự động chuyển đổi bàn giao {} nhiệm vụ (Task TODO) liên quan đến khách %s từ Sale cũ sang cho Sale mới phụ trách.", activeTasks.size(), lead.getFullName());
                            }
                        } catch (Exception e) {
                            log.error("Lỗi tự động chuyển giao Task khi update Lead: {}", e.getMessage());
                        }

                        try {
                            if (newStaff.getUser() != null) {
                                Notification reassignNoti = Notification.builder()
                                        .title("Nhiệm vụ mới: Tiếp nhận bàn giao khách hàng")
                                        .message(String.format("Admin đã bàn giao khách hàng %s sang cho bạn phụ trách. Vui lòng vào phân hệ Khách hàng và Lịch hẹn để tiếp nhận công việc.", lead.getFullName()))
                                        .type(NotificationType.TASK)
                                        .user(newStaff.getUser())
                                        .createdBy("Admin")
                                        .receiverRoleCode("ROLE_SALE")
                                        .build();

                                Notification savedReassignNoti = notificationRepository.save(reassignNoti);

                                messagingTemplate.convertAndSend(
                                        "/user/" + newStaff.getUser().getId() + "/queue/notifications",
                                        NotificationResponse.fromEntity(savedReassignNoti)
                                );
                                log.info("⚡ [REALTIME] Đã bắn thông báo bàn giao khách đến Sale mới ID: {}", newStaff.getUser().getId());
                            }
                        } catch (Exception e) {
                            log.error("Lỗi bắn thông báo điều phối Lead: {}", e.getMessage());
                        }
                    }
                }
            } else {
                log.info("CRM Security: Nhân viên Sale không có quyền điều phối lại nhân sự.");
            }
        }

        if (request.getStatus() == LeadStatus.LOST) {
            lead.setLostReason(request.getLostReason());
        }

        if (request.getStatus() != null) {
            if (request.getStatus() == LeadStatus.WON && lead.getStatus() != LeadStatus.WON) {
                leadRepository.save(lead);
                return changeLeadStatus(id, LeadStatus.WON, currentUserId);
            } else {
                lead.setStatus(request.getStatus());
            }
        }

        lead.setUpdatedAt(Instant.now());
        CrmLead updatedLead = leadRepository.save(lead);
        return LeadResponse.fromLead(updatedLead);
    }

    @Override
    @Transactional
    public void updateLeadStatus(Integer id, LeadStatus newStatus) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));

        lead.setStatus(newStatus);
        lead.setUpdatedAt(Instant.now());
        leadRepository.save(lead);
    }

    @Override
    public List<LeadResponse> getLeads(Integer requestedStaffId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Jwt jwt = (Jwt) auth.getPrincipal();
        Integer currentUserId = Math.toIntExact(jwt.getClaim("userId"));

        if (isAdmin) {
            if (requestedStaffId != null) {
                return leadRepository.findByAssignedStaff_IdOrderByCreatedAtDesc(requestedStaffId).stream()
                        .map(LeadResponse::fromLead)
                        .toList();
            } else {
                return leadRepository.findAllByOrderByCreatedAtDesc().stream()
                        .map(LeadResponse::fromLead)
                        .toList();
            }
        } else {
            return leadRepository.findByAssignedStaff_IdOrderByCreatedAtDesc(currentUserId).stream()
                    .map(LeadResponse::fromLead)
                    .toList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadDetail(Integer id) {
        CrmLead lead = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lead"));
        return LeadResponse.fromLead(lead);
    }

    @Override
    @Transactional
    public LeadResponse changeLeadStatus(Integer leadId, LeadStatus newStatus, Integer userInternalId) {
        CrmLead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new AppException(ErrorCode.LEAD_NOT_FOUND));

        lead.setStatus(newStatus);
        leadRepository.save(lead);

        return LeadResponse.fromLead(lead);
    }
}