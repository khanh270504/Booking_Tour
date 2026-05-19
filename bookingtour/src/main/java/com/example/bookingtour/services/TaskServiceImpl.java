package com.example.bookingtour.services;

import com.example.bookingtour.IServices.ITaskService;
import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.request.crm.TaskStatusUpdateRequest;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
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
        StaffProfile assignedStaff;

        if (request.getAssignedStaffId() != null) {
            if (!request.getAssignedStaffId().equals(currentStaff.getId()) && !isCurrentUserAdmin()) {
                throw new RuntimeException("Lỗi quyền: Bạn không có quyền giao việc cho nhân viên khác!");
            }

            assignedStaff = staffRepository.findById(request.getAssignedStaffId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên được giao"));
        } else {
            assignedStaff = currentStaff;
        }

        CrmTask task = CrmTask.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .lead(lead)
                .assignedStaff(assignedStaff)
                .status(TaskStatus.TODO)
                .priority(request.getPriority() != null ? request.getPriority() : TaskPriority.MEDIUM)
                .dueDate(request.getDueDate())
                .note(request.getNote())
                .build();

        CrmTask savedTask = taskRepository.save(task);
        return TaskResponse.fromTask(savedTask);
    }

    @Override
    @Transactional
    public void completeTask(Integer taskId) {
        StaffProfile currentStaff = getCurrentStaff();
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc"));

        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(currentStaff.getId())) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không thể hoàn thành công việc của người khác!");
        }

        task.setStatus(TaskStatus.DONE);
        task.setCompletedAt(Instant.now());

        taskRepository.save(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getMyTasks() {
        StaffProfile currentStaff = getCurrentStaff();

        return taskRepository.findByAssignedStaff_IdOrderByDueDateAsc(currentStaff.getId())
                .stream()
                .map(TaskResponse::fromTask)
                .toList();
    }

    @Override
    @Transactional
    public void updateTaskStatus(Integer taskId, TaskStatusUpdateRequest request) {
        StaffProfile currentStaff = getCurrentStaff();
        CrmTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy công việc với ID: " + taskId));

        if (!isCurrentUserAdmin() && !task.getAssignedStaff().getId().equals(currentStaff.getId())) {
            throw new RuntimeException("Lỗi bảo mật: Bạn không có quyền cập nhật trạng thái công việc của người khác!");
        }

        task.setStatus(request.getStatus());

        if (request.getStatus() == TaskStatus.DONE) {
            task.setCompletedAt(Instant.now());
        } else {
            task.setCompletedAt(null);
        }

        taskRepository.save(task);
    }
}