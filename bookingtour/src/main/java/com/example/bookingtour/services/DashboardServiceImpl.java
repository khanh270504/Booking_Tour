package com.example.bookingtour.services;

import com.example.bookingtour.IServices.IDashboardService;
import com.example.bookingtour.dtos.response.dashboard.DashboardOverviewResponse;
import com.example.bookingtour.entities.CrmTask;
import com.example.bookingtour.enums.LeadSource;
import com.example.bookingtour.enums.LeadStatus;
import com.example.bookingtour.repositories.BookingRepository;
import com.example.bookingtour.repositories.CrmTaskRepository;
import com.example.bookingtour.repositories.CrmLeadRepository;
import com.example.bookingtour.repositories.TourCostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements IDashboardService {
    private final TourCostRepository tourCostRepository;
    private final BookingRepository bookingRepository;
    private final CrmLeadRepository leadRepository;
    private final CrmTaskRepository taskRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getOverviewData(String period) {
        log.info("Đang xử lý tổng hợp dữ liệu Dashboard hệ thống với bộ lọc: {}", period);

        double totalRevenueSum = 0;
        int totalCompletedBookings = 0;
        int totalLeads = 0;

        // 1. BIỂU ĐỒ DOANH THU & CHI PHÍ (Hỗ trợ MONTH / YEAR mượt mà)
        List<DashboardOverviewResponse.RevenueChartDto> revenueData = new ArrayList<>();

        if ("MONTH".equalsIgnoreCase(period)) {
            // Chế độ: THÁNG NÀY (Gom nhóm theo ngày)
            List<Object[]> revenueDb = bookingRepository.getRevenueByDayInCurrentMonth();
            List<Object[]> costDb = tourCostRepository.getCostByDayInCurrentMonth();

            Map<Integer, Double> revenueMap = new HashMap<>();
            for (Object[] row : revenueDb) {
                if (row[0] != null && row[1] != null) {
                    revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
                }
            }

            Map<Integer, Double> costMap = new HashMap<>();
            for (Object[] row : costDb) {
                if (row[0] != null && row[1] != null) {
                    costMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
                }
            }

            int lengthOfMonth = LocalDate.now().lengthOfMonth(); // Tự động lấy 28, 29, 30 hoặc 31 ngày
            for (int i = 1; i <= lengthOfMonth; i++) {
                double rev = revenueMap.getOrDefault(i, 0.0);
                double cost = costMap.getOrDefault(i, 0.0);
                totalRevenueSum += rev;

                revenueData.add(DashboardOverviewResponse.RevenueChartDto.builder()
                        .month("Ngày " + i)
                        .revenue(rev / 1_000_000.0) // Chuyển đổi thành đơn vị Triệu VNĐ
                        .cost(cost / 1_000_000.0)
                        .build());
            }
        } else {
            List<Object[]> revenueDb = bookingRepository.getRevenueByMonth();
            List<Object[]> costDb = tourCostRepository.getCostByMonth();

            Map<Integer, Double> revenueMap = new HashMap<>();
            for (Object[] row : revenueDb) {
                if (row[0] != null && row[1] != null) {
                    revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
                }
            }

            Map<Integer, Double> costMap = new HashMap<>();
            for (Object[] row : costDb) {
                if (row[0] != null && row[1] != null) {
                    costMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
                }
            }

            for (int i = 1; i <= 12; i++) {
                double rev = revenueMap.getOrDefault(i, 0.0);
                double cost = costMap.getOrDefault(i, 0.0);
                totalRevenueSum += rev;

                revenueData.add(DashboardOverviewResponse.RevenueChartDto.builder()
                        .month("T" + i)
                        .revenue(rev / 1_000_000.0) // Chuyển đổi thành đơn vị Triệu VNĐ
                        .cost(cost / 1_000_000.0)
                        .build());
            }
        }

        // 2. TOP ĐỊA ĐIỂM YÊU THÍCH (Top Destinations)

        List<DashboardOverviewResponse.DestinationDto> destinationData = new ArrayList<>();
        List<Object[]> destDb = bookingRepository.getTopDestinations();
        String[] destColors = {"#3b82f6", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899"};
        int destIdx = 0;

        for (Object[] row : destDb) {
            destinationData.add(DashboardOverviewResponse.DestinationDto.builder()
                    .name(row[0] != null ? row[0].toString() : "Chưa xác định")
                    .bookings(row[1] != null ? ((Number) row[1]).intValue() : 0)
                    .color(destColors[destIdx % destColors.length])
                    .build());
            destIdx++;
        }


        // 3. TRẠNG THÁI BOOKING (Booking Status Distribution)

        List<DashboardOverviewResponse.StatusDto> statusData = new ArrayList<>();
        List<Object[]> statusDb = bookingRepository.countBookingsByStatus();

        for (Object[] row : statusDb) {
            String rawStatus = row[0] != null ? row[0].toString() : "UNKNOWN";
            int count = row[1] != null ? ((Number) row[1]).intValue() : 0;

            if ("COMPLETED".equals(rawStatus)) {
                totalCompletedBookings += count;
            }

            String VietnameseStatus = switch (rawStatus) {
                case "COMPLETED" -> "Hoàn thành";
                case "PENDING", "PENDING_PAYMENT" -> "Chờ thanh toán";
                case "CANCELED" -> "Đã hủy";
                default -> rawStatus;
            };

            String color = switch (rawStatus) {
                case "COMPLETED" -> "#10b981";
                case "PENDING", "PENDING_PAYMENT" -> "#f59e0b";
                case "CANCELED" -> "#ef4444";
                default -> "#64748b";
            };

            statusData.add(DashboardOverviewResponse.StatusDto.builder()
                    .name(VietnameseStatus)
                    .value(count)
                    .color(color)
                    .build());
        }


        // 4. PHỄU CHUYỂN ĐỔI SALES (Sales Funnel)

        List<DashboardOverviewResponse.FunnelDto> funnelData = new ArrayList<>();
        List<Object[]> funnelDb = leadRepository.getLeadFunnel();
        String[] funnelColors = {"#64748b", "#3b82f6", "#f59e0b", "#10b981"};
        int funnelIdx = 0;

        for (Object[] row : funnelDb) {
            LeadStatus status = (LeadStatus) row[0];
            String stageName = (status != null) ? status.name() : "Chưa phân loại";
            int count = row[1] != null ? ((Number) row[1]).intValue() : 0;

            totalLeads += count;

            funnelData.add(DashboardOverviewResponse.FunnelDto.builder()
                    .stage(stageName)
                    .count(count)
                    .conversion((100 - (funnelIdx * 20)) + "%")
                    .color(funnelColors[funnelIdx % funnelColors.length])
                    .build());
            funnelIdx++;
        }


        // 5. NGUỒN KHÁCH HÀNG (Lead Sources)

        List<DashboardOverviewResponse.SourceDto> sourceData = new ArrayList<>();
        List<Object[]> sourceDb = leadRepository.getLeadSources();
        String[] sourceColors = {"#1877f2", "#ea4335", "#000000", "#8b5cf6"};
        int srcIdx = 0;

        for (Object[] row : sourceDb) {
            LeadSource source = (LeadSource) row[0];
            String sourceName = (source != null) ? source.name() : "Khác";

            sourceData.add(DashboardOverviewResponse.SourceDto.builder()
                    .name(sourceName)
                    .value(row[1] != null ? ((Number) row[1]).intValue() : 0)
                    .color(sourceColors[srcIdx % sourceColors.length])
                    .build());
            srcIdx++;
        }


        // 6. GIÁM SÁT TASK CRM QUÁ HẠN NGUY HIỂM (Urgent Tasks)

        List<DashboardOverviewResponse.TaskAlertDto> urgentTasks = new ArrayList<>();
        List<CrmTask> overdueTasks = taskRepository.findOverdueTasks();

        for (CrmTask task : overdueTasks) {
            urgentTasks.add(DashboardOverviewResponse.TaskAlertDto.builder()
                    .text(task.getTitle())
                    .staff(task.getAssignedStaff() != null ? task.getAssignedStaff().getFullName() : "Chưa phân công")
                    .delay("Trễ hạn")
                    .isUrgent(true)
                    .build());
        }


        // 7. BẢNG XẾP HẠNG DOANH SỐ (Sales Leaderboard - DỮ LIỆU THẬT)
        List<DashboardOverviewResponse.SalesLeaderboardDto> salesLeaderboard = new ArrayList<>();
        List<Object[]> leaderboardDb = bookingRepository.getSalesLeaderboard();

       // String[] avatars = {"😎", "🤠", "🤩", "🤓", "🥳"};
        int rank = 0;

        for (Object[] row : leaderboardDb) {
            String staffName = row[0] != null ? row[0].toString() : "Nhân viên ẩn danh";
            double revenue = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            int deals = row[2] != null ? ((Number) row[2]).intValue() : 0;

            salesLeaderboard.add(DashboardOverviewResponse.SalesLeaderboardDto.builder()
                    .name(staffName)
                    .revenue(String.format("%,.1f Tr", revenue / 1_000_000.0)) // Đơn vị Triệu VNĐ
                    .deals(deals)
                    .target(100)
              //      .avatar(avatars[rank % avatars.length])
                    .build());
            rank++;
        }

        // 8. TÍNH TOÁN REAL-TIME CHO KHỐI CHỈ SỐ TỔNG QUAN (Top Cards)
        String formattedTotalRevenue = String.format("%,.1f Tr", totalRevenueSum / 1_000_000.0);

        double winRate = (totalLeads > 0) ? ((double) totalCompletedBookings / totalLeads) * 100 : 0.0;
        String formattedWinRate = String.format("%.1f%%", winRate);

        Integer pendingCountDb = taskRepository.countPendingTasks();
        int pendingTasksCount = pendingCountDb != null ? pendingCountDb : 0;

        DashboardOverviewResponse.TopStatsDto topStats = DashboardOverviewResponse.TopStatsDto.builder()
                .totalRevenue(formattedTotalRevenue)
                .revenueChange("+12.5%") // Tạm thời hardcode tỷ lệ biến động
                .winRate(formattedWinRate)
                .winRateChange("+2.1%")
                .newLeads(totalLeads)
                .leadsChange("+5.1%")
                .pendingTasks(pendingTasksCount)
                .overdueTasks(overdueTasks.size())
                .build();

        return DashboardOverviewResponse.builder()
                .topStats(topStats)
                .revenueData(revenueData)
                .destinationData(destinationData)
                .statusData(statusData)
                .funnelData(funnelData)
                .sourceData(sourceData)
                .salesLeaderboard(salesLeaderboard)
                .urgentTasks(urgentTasks)
                .build();
    }
}