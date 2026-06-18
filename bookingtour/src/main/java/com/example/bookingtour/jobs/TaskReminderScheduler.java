package com.example.bookingtour.jobs;

import com.example.bookingtour.entities.CrmTask;
import com.example.bookingtour.entities.Notification;
import com.example.bookingtour.enums.NotificationType;
import com.example.bookingtour.enums.TaskStatus;
import com.example.bookingtour.repositories.CrmTaskRepository;
import com.example.bookingtour.repositories.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskReminderScheduler {

    private final CrmTaskRepository taskRepository;
    private final NotificationRepository notificationRepository;

    @Scheduled(cron = "0 0 7 * * ?")
    @Transactional
    public void scanAndRemindDailyTasks() {
        log.info(" [SCHEDULER] Bắt đầu quét các công việc đến hạn hôm nay để bắn chuông nhắc nhở...");

        List<CrmTask> activeTasks = taskRepository.findByStatus(TaskStatus.TODO);
        LocalDate today = LocalDate.now();

        int alertCount = 0;

        for (CrmTask task : activeTasks) {
            if (task.getDueDate() != null && task.getAssignedStaff() != null) {
                // Chuyển đổi Instant của dueDate sang LocalDate để so sánh ngày
                LocalDate taskDueDate = task.getDueDate().atZone(ZoneId.systemDefault()).toLocalDate();

                // Nếu hạn xử lý trùng với ngày hôm nay
                if (taskDueDate.equals(today)) {
                    try {
                        if (task.getAssignedStaff().getUser() != null) {
                            Notification reminderNoti = Notification.builder()
                                    .title("Nhắc hẹn: Công việc cần xử lý hôm nay")
                                    .message(String.format("Hôm nay bạn có lịch hẹn: \"%s\". Vui lòng kiểm tra và xử lý đúng hạn.", task.getTitle()))
                                    .type(NotificationType.TASK)
                                    .user(task.getAssignedStaff().getUser())
                                    .createdBy("Hệ thống nhắc việc")
                                    .build();

                            notificationRepository.save(reminderNoti);
                            alertCount++;
                        }
                    } catch (Exception e) {
                        log.error(" Lỗi bắn chuông nhắc hạn cho Task ID {}: {}", task.getId(), e.getMessage());
                    }
                }
            }
        }

        log.info(" [SCHEDULER] Hoàn thành quét ngầm. Đã phát ra {} thông báo nhắc việc đầu ngày.", alertCount);
    }

    @Scheduled(cron = "0 0 7 * * ?")
    @Transactional
    public void scanAndAlertOverdueTasks() {
        log.info("[CRONJOB QUÁ HẠN] Bắt đầu rà soát các công việc bị chậm tiến độ dưới Database...");

        Instant now = Instant.now();
        List<CrmTask> overdueTasks = taskRepository.findByStatusAndDueDateBefore(TaskStatus.TODO, now);

        int overdueAlertCount = 0;

        for (CrmTask task : overdueTasks) {
            if (task.getAssignedStaff() != null && task.getAssignedStaff().getUser() != null) {
                try {
                    Notification urgentNoti = Notification.builder()
                            .title("CẢNH BÁO: Công việc quá hạn nguy hiểm!")
                            .message(String.format("Nhiệm vụ: \"%s\" phối hợp với khách %s đã QUÁ HẠN xử lý.\nVui lòng cập nhật tiến độ ngay lập tức để tránh mất khách hàng!",
                                    task.getTitle(),
                                    task.getLead() != null ? task.getLead().getFullName() : "Ẩn danh"))
                            .type(NotificationType.TASK)
                            .user(task.getAssignedStaff().getUser())
                            .createdBy("Hệ thống kiểm toán")
                            .build();

                    notificationRepository.save(urgentNoti);
                    overdueAlertCount++;
                } catch (Exception e) {
                    log.error("Lỗi bắn chuông quá hạn cho Task ID {}: {}", task.getId(), e.getMessage());
                }
            }
        }
        log.info("[CRONJOB QUÁ HẠN] Hoàn thành. Đã phát hiện và phát ra {} chuông cảnh báo trễ hạn.", overdueAlertCount);
    }
}
