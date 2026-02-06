package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private String userId; // Người thực hiện hành động

    @Column(name = "action", length = 100)
    private String action; // UPDATE_TOUR, DELETE_USER

    @Column(name = "target_table", length = 50)
    private String targetTable; // tours, users

    @Column(name = "target_id", length = 50)
    private String targetId;

    // --- JSONB MAPPING ---
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;
    // ---------------------

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}