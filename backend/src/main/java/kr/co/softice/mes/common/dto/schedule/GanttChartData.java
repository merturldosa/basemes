package kr.co.softice.mes.common.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gantt Chart Data DTO
 * Gantt Chart 렌더링용 데이터 구조
 *
 * @author Moon Myung-seop
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanttChartData {

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<GanttTask> tasks;
    private List<GanttResource> resources;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GanttTask {
        private String id;                    // schedule_id
        private String name;                  // workOrderNo - processName
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer duration;             // 분
        private BigDecimal progress;          // 0-100
        private String status;
        private String color;                 // 상태별 색상
        private String parentId;              // workOrderId (그룹화용)
        private List<String> dependencies;   // 선행 작업 ID 목록
        private ResourceInfo resource;        // 할당 리소스
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GanttResource {
        private Long resourceId;
        private String resourceType;          // EQUIPMENT, USER
        private String resourceName;
        private List<TimeSlot> occupiedSlots; // 사용 중인 시간대
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlot {
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String scheduleId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceInfo {
        private String equipmentCode;
        private String equipmentName;
        private Integer workers;
        private String assignedUserName;
    }
}
