package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.request.crm.TaskStatusUpdateRequest;
import com.example.bookingtour.dtos.response.crm.TaskResponse;

import java.util.List;

public interface ITaskService {

    TaskResponse createTask(TaskCreateRequest request);

    List<TaskResponse> getMyTasks();

    void completeTask(Integer taskId);

    void updateTaskStatus(Integer taskId, TaskStatusUpdateRequest request);
}