package com.example.bookingtour.jobs;

import com.example.bookingtour.entities.TourSchedule;
import com.example.bookingtour.enums.ScheduleStatus;
import com.example.bookingtour.repositories.TourScheduleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TourScheduleCronJob {

    private final TourScheduleRepository scheduleRepository;

    //@Scheduled(cron = "0 5 0 * * ?")
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void autoUpdateScheduleStatus() {

        log.info("[CRON JOB] Bắt đầu cập nhật trạng thái lịch trình");

        LocalDate today = LocalDate.now();

        // OPENING/FULL -> DEPARTED
        List<ScheduleStatus> activeStatuses = Arrays.asList(
                ScheduleStatus.OPENING,
                ScheduleStatus.FULL
        );

        List<TourSchedule> readyToDepart =
                scheduleRepository.findByDepartureDateLessThanEqualAndStatusIn(
                        today,
                        activeStatuses
                );

        for (TourSchedule schedule : readyToDepart) {
            schedule.setStatus(ScheduleStatus.DEPARTED);

            log.info(
                    "Schedule ID {} chuyển sang DEPARTED",
                    schedule.getId()
            );
        }

        scheduleRepository.saveAll(readyToDepart);

        List<TourSchedule> readyToComplete =
                scheduleRepository.findByReturnDateLessThanAndStatus(
                        today,
                        ScheduleStatus.DEPARTED
                );

        log.info("Tour cần hoàn thành: {}", readyToComplete.size());

        for (TourSchedule schedule : readyToComplete) {
            schedule.setStatus(ScheduleStatus.COMPLETED);

            log.info(
                    "Schedule ID {} chuyển sang COMPLETED",
                    schedule.getId()
            );
        }

        scheduleRepository.saveAll(readyToComplete);

        log.info("[CRON JOB] Cập nhật lịch trình hoàn tất");
    }
}