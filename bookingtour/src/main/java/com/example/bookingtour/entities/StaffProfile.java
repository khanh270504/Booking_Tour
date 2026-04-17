package com.example.bookingtour.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "staff_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffProfile {

    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "employee_code", unique = true, length = 20)
    private String employeeCode;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "position", length = 100)
    private String position;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "hire_date")
    private LocalDate hireDate;
}