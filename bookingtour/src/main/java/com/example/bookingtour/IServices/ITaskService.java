package com.example.bookingtour.IServices;

import com.example.bookingtour.dtos.request.crm.TaskCreateRequest;
import com.example.bookingtour.dtos.request.crm.TaskStatusUpdateRequest;
import com.example.bookingtour.dtos.response.crm.TaskResponse;

import java.util.List;

public interface ITaskService {

    TaskResponse createTask(TaskCreateRequest request);

    List<TaskResponse> getMyTasks();

    void completeTask(Integer taskId);

    List<TaskResponse> getAllTasks();

    void deleteTask(Integer taskId);

    TaskResponse updateTask(Integer taskId, TaskCreateRequest request);
}