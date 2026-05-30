package com.example.bookingtour.controllers;

import com.example.bookingtour.IServices.ITaskService;
import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/crm/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final ITaskService taskService;

    // 1. Tạo mới công việc
    @PostMapping
    public ApiResponse<?> createTask(@Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .result(taskService.createTask(request))
                .build();
    }

    @GetMapping("/me")
    public ApiResponse<?> getMyTasks() {
        return ApiResponse.builder()
                .code(200)
                .result(taskService.getMyTasks())
                .build();
    }

    @GetMapping
    public ApiResponse<?> getAllTasks() {
        return ApiResponse.builder()
                .code(200)
                .result(taskService.getAllTasks())
                .build();
    }
    //hoan thanh
    @PostMapping("/{id}/complete")
    public ApiResponse<?> completeTask(@PathVariable Integer id) {
        taskService.completeTask(id);
        return ApiResponse.builder()
                .code(200)
                .message("Đã hoàn thành công việc!")
                .build();
    }

    // Sửa thông tin
    @PutMapping("/{id}")
    public ApiResponse<?> updateTask(@PathVariable Integer id, @Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.builder()
                .code(200)
                .result(taskService.updateTask(id, request))
                .build();
    }

    // Xóa
    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ApiResponse.builder()
                .code(200)
                .message("Xóa công việc thành công")
                .build();
    }
}