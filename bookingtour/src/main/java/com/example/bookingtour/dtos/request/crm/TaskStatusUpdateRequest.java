package com.example.bookingtour.dtos.request.crm;

import com.example.bookingtour.enums.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusUpdateRequest {
    @NotNull
    private TaskStatus status;
}