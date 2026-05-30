package com.example.bookingtour.dtos.request.crm;

import com.example.bookingtour.enums.TaskPriority;
import com.example.bookingtour.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class TaskCreateRequest {

    @NotBlank(message = "Tiêu đề công việc không được để trống")
    private String title;

    // Nội dung công việc
    private String description;

    @NotNull(message = "Phải gắn công việc với một khách hàng")
    private Integer leadId;

    private Integer assignedStaffId;

    private TaskType taskType;

    private TaskPriority priority;

    private Instant dueDate;

    private String note;
}

