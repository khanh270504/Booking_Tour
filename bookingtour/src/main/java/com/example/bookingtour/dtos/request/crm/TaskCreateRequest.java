package com.example.bookingtour.dtos.request.crm;

import com.example.bookingtour.enums.TaskPriority;
import com.example.bookingtour.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.Instant;

@Data
public class TaskCreateRequest {
    @NotBlank(message = "Tiêu đề công việc không được để trống")
    private String title;

    private String description;

    @NotNull(message = "Phải gắn công việc với một khách hàng")
    private Integer leadId;

    private Integer assignedStaffId;

    private TaskPriority priority;

    private Instant dueDate;

    private String note;
}