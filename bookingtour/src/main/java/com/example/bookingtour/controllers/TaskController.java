package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ITaskService;
import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.request.crm.TaskStatusUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMyTasks() {
        // Tự động lấy danh sách việc của người đang cầm Token
        return ResponseEntity.ok(taskService.getMyTasks());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateTaskStatus(@PathVariable Integer id, @Valid @RequestBody TaskStatusUpdateRequest request) {
        taskService.updateTaskStatus(id, request);
        return ResponseEntity.ok("Cập nhật công việc thành công");
    }
}