package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, String> {
    Optional<StaffProfile> findByEmployeeCode(String employeeCode);
}