package com.example.bookingtour.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "invalidated_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvalidatedToken {

    @Id
    private String id;

    private Date expiryTime;
}