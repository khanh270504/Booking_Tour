package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.StaffProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaffProfileRepository extends JpaRepository<StaffProfile, Integer> {
    Optional<StaffProfile> findByEmployeeCode(String employeeCode);
    Optional<StaffProfile> findByUser_Id(Integer userId);

}