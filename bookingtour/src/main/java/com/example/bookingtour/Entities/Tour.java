package com.example.bookingtour.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "tours")
@Data
@NoArgsConstructor
public class Tour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "destination_id")
    private Destination destination;

    @Column(name = "name")
    private String name;

    @Column(name = "tour_type", length = 50)
    private String tourType;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Column(name = "status", length = 20)
    private String status;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "itinerary", columnDefinition = "jsonb")
    private List<Map<String, Object>> itinerary;

    @CreationTimestamp
    @Column(name = "created_at")
    private Instant createdAt;
}