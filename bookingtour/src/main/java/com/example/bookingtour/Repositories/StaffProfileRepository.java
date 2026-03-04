package com.example.bookingtour.Repositories;

import com.example.bookingtour.Entities.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, String> {
    Optional<StaffProfile> findByEmployeeCode(String employeeCode);
}