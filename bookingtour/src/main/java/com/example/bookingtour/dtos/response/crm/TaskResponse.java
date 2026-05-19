package com.example.bookingtour.dtos.response.crm;

import com.example.bookingtour.entities.CrmTask;
import com.example.bookingtour.enums.TaskPriority;
import com.example.bookingtour.enums.TaskStatus;
import lombok.*;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private Integer id;
    private String title;
    private String description;
    private String leadName;
    private String staffName;
    private String status;
    private String priority;
    private Instant dueDate;

    public static TaskResponse fromTask(CrmTask entity) {
        return TaskResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .leadName(entity.getLead().getFullName())
                .staffName(entity.getAssignedStaff().getFullName())
                .status(entity.getStatus().name())
                .priority(entity.getPriority().name())
                .dueDate(entity.getDueDate())
                .build();
    }
}