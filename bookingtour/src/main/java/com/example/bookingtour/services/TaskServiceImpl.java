package com.example.bookingtour.services;

import com.example.bookingtour.IServices.ITaskService;
import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.response.crm.TaskResponse;
import com.example.bookingtour.entities.*;
import com.example.bookingtour.enums.*;
import com.example.bookingtour.repositories.CrmTaskRepository;
import com.example.bookingtour.repositories.CrmLeadRepository;
import com.example.bookingtour.repositories.StaffProfileRepository;
import com.example.bookingtour.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements ITaskService {

    private final CrmTaskRepository taskRepository;
    private final CrmLeadRepository leadRepository;
    private final StaffProfileRepository staffRepository;
    private final NotificationRepository notificationRepository;

    private boolean isCurrentUserAdmin() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private StaffProfile getCurrentStaff() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return staffRepository.findByUser_Email(email)
                .orElseThrow(() -> new RuntimeException("Tài khoản chưa được gắn hồ sơ nhân viên"));
    }

    @Override
    @Transactional
    public TaskResponse createTask(TaskCreateRequest request) {
        CrmLead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        StaffProfile currentStaff = getCurrentStaff();

        CrmTask task = CrmTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .lead(lead)
                .assignedStaff(currentStaff)
                .taskType(request.getTaskType())
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .note(request.getNote())
                .build();

        CrmTask savedTask = taskRepository.save(task);
        log.info("📝 Sale {} đã lưu một nhật ký tương tác / lịch hẹn với khách {}.", currentStaff.getId(), lead.getFullName());

        return TaskResponse.fromTask(savedTask);
    }

    @Override
    @Transactional
    public void completeTask(Integer taskId) {
        StaffProfile currentStaff = getCurrentStaff();
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        // Bảo mật: Nhật ký việc của ai người đó tự hoàn thành, trừ Admin có quyền kiểm tra can thiệp
        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(currentStaff.getId())) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không có quyền hoàn thành nhật ký công việc này!");
        }

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(Instant.now());
        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        return taskRepository.findByAssignedStaff_IdOrderByDueDateAsc(getCurrentStaff().getId())
                .stream().map(TaskResponse::fromTask).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(TaskResponse::fromTask)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Integer taskId, TaskCreateRequest request) {
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        StaffProfile currentStaff = getCurrentStaff();

        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(currentStaff.getId())) {
            throw new RuntimeException("Bạn không có quyền sửa nhật ký tương tác này!");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDate(request.getDueDate());
        task.setPriority(request.getPriority());
        task.setNote(request.getNote());
        task.setTaskType(request.getTaskType());

        return TaskResponse.fromTask(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void deleteTask(Integer taskId) {
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(getCurrentStaff().getId())) {
            throw new RuntimeException("Bạn không có quyền xóa nhật ký tương tác này!");
        }

        taskRepository.delete(task);
    }

    public void createBookingReminderTask(CrmLead lead) {
        boolean exists = taskRepository.existsByLeadAndStatus(lead, TaskStatus.TODO);
        if (exists) {
            log.info("[TASK] Task nhắc tạo booking cho Lead {} đã tồn tại.", lead.getFullName());
            return;
        }

        CrmTask task = new CrmTask();
        task.setTitle(" Cần tạo Booking: " + lead.getFullName());
        task.setDescription("Lead " + lead.getFullName() + " đã chuyển sang trạng thái WON. Vui lòng vào trang Đặt tour để tạo Booking. LeadID: " + lead.getId());

        task.setAssignedStaff(lead.getAssignedStaff());
        task.setLead(lead);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(Instant.now().plus(1, ChronoUnit.DAYS));
        task.setCreatedAt(Instant.now());

        CrmTask savedSystemTask = taskRepository.save(task);
        log.info("[TASK] Đã tạo nhiệm vụ tạo booking tự động cho Lead ID: {}", lead.getId());

        try {
            if (savedSystemTask.getAssignedStaff() != null && savedSystemTask.getAssignedStaff().getUser() != null) {
                Notification sysTaskNoti = Notification.builder()
                        .title("Khẩn cấp: Tạo đơn đặt tour")
                        .message(String.format("Khách hàng %s đã đồng ý chốt đơn (WON).\nHệ thống đã tự động lên lịch nhắc việc. Hãy tiến hành tạo Đơn đặt tour ngay!", lead.getFullName()))
                        .type(NotificationType.TASK)
                        .user(savedSystemTask.getAssignedStaff().getUser())
                        .createdBy("Hệ thống tự động")
                        .build();
                notificationRepository.save(sysTaskNoti);
            }
        } catch (Exception e) {
            log.error(" Lỗi thông báo hệ thống tạo Task tự động: {}", e.getMessage());
        }
    }
}