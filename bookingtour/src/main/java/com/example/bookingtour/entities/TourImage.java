package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tour_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TourImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "tour_id")
    private Tour tour;

    @Column(name = "image_url", columnDefinition = "text")
    private String imageUrl;
}