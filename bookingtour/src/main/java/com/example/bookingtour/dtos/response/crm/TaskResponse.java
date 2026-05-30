package com.example.bookingtour.dtos.response.crm;

import com.example.bookingtour.entities.CrmTask;
import com.example.bookingtour.enums.TaskPriority;
import com.example.bookingtour.enums.TaskStatus;
import com.example.bookingtour.enums.TaskType;
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

    private Integer leadId;
    private String leadName;
    private String staffName;

    private TaskStatus status;
    private TaskPriority priority;
    private TaskType taskType;

    private Instant dueDate;
    private Instant completedAt;
    private String note;

    public static TaskResponse fromTask(CrmTask entity) {
        if (entity == null) return null;

        return TaskResponse.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())

                .leadId(entity.getLead() != null ? entity.getLead().getId() : null)
                .leadName(entity.getLead() != null ? entity.getLead().getFullName() : "N/A")
                .staffName(entity.getAssignedStaff() != null ? entity.getAssignedStaff().getFullName() : "Chưa phân công")

                .status(entity.getStatus())
                .priority(entity.getPriority())
                .taskType(entity.getTaskType())
                .dueDate(entity.getDueDate())
                .completedAt(entity.getCompletedAt())
                .note(entity.getNote())
                .build();
    }
}