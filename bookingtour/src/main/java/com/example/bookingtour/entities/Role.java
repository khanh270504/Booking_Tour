package com.example.bookingtour.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Role {
    @Id
    @Column(name = "role_name", length = 50)
    private String roleName;

    @Column(name = "description", columnDefinition = "text")
    private String description;
}
