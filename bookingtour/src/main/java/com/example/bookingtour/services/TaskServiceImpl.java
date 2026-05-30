package com.example.bookingtour.services;

import com.example.bookingtour.IServices.ITaskService;
import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.response.crm.TaskResponse;
import com.example.bookingtour.entities.CrmLead;
import com.example.bookingtour.entities.CrmTask;
import com.example.bookingtour.entities.StaffProfile;
import com.example.bookingtour.enums.TaskPriority;
import com.example.bookingtour.enums.TaskStatus;
import com.example.bookingtour.repositories.CrmTaskRepository;
import com.example.bookingtour.repositories.CrmLeadRepository;
import com.example.bookingtour.repositories.StaffProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.internal.concurrent.Task;
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
        StaffProfile assignedStaff = (request.getAssignedStaffId() != null)
                ? staffRepository.findById(request.getAssignedStaffId()).orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"))
                : currentStaff;

        // Xử lý quyền giao việc (nếu cần)
        if (!assignedStaff.getId().equals(currentStaff.getId()) && !isCurrentUserAdmin()) {
            throw new RuntimeException("Lỗi quyền: Bạn không có quyền giao việc cho nhân viên khác!");
        }

        CrmTask task = CrmTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .lead(lead)
                .assignedStaff(assignedStaff)
                .taskType(request.getTaskType()) // Fix lỗi thiếu cột task_type
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .note(request.getNote())
                .build();

        return TaskResponse.fromTask(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void completeTask(Integer taskId) {
        StaffProfile currentStaff = getCurrentStaff();
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(currentStaff.getId())) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không có quyền hoàn thành công việc này!");
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
    // 1. Sửa nội dung Task (Tiêu đề, ngày, nội dung, độ ưu tiên)
    @Override
    @Transactional
    public TaskResponse updateTask(Integer taskId, TaskCreateRequest request) {
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        // Kiểm tra quyền (chỉ người được giao hoặc Admin mới được sửa)
        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(getCurrentStaff().getId())) {
            throw new RuntimeException("Bạn không có quyền sửa công việc này!");
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

        // Kiểm tra quyền
        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(getCurrentStaff().getId())) {
            throw new RuntimeException("Bạn không có quyền xóa công việc này!");
        }

        taskRepository.delete(task);
    }

    public void createBookingReminderTask(CrmLead lead) {
        boolean exists = taskRepository.existsByLeadAndStatus(lead, TaskStatus.TODO);
        if (exists) {
            log.info("⚠️ [TASK] Task nhắc tạo booking cho Lead {} đã tồn tại, không tạo thêm.", lead.getFullName());
            return;
        }

        CrmTask task = new CrmTask();
        task.setTitle(" Cần tạo Booking: " + lead.getFullName());

        // Gắn thêm LeadID để sau này FE làm link bấm vào là ra trang tạo booking
        task.setDescription("Lead " + lead.getFullName() + " đã chuyển sang trạng thái WON. " +
                "Vui lòng vào trang Đặt tour để tạo Booking. " +
                "LeadID: " + lead.getId());

        task.setAssignedStaff(lead.getAssignedStaff() != null ? lead.getAssignedStaff() : null);
        task.setLead(lead);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.HIGH);
        task.setDueDate(Instant.now().plus(1, ChronoUnit.DAYS));
        task.setCreatedAt(Instant.now());

        taskRepository.save(task);

        log.info("✅ [TASK] Đã tạo nhiệm vụ tạo booking cho Lead ID: {}", lead.getId());
    }
}