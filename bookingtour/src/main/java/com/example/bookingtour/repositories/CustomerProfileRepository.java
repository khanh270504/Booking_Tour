package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.CustomerProfile;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Integer> {
    Optional<CustomerProfile> findByUser_Id(Integer id);
    Optional<CustomerProfile> findByEmail(String email);
    @Query("SELECT c FROM CustomerProfile c LEFT JOIN c.user u WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR " +
            "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "(u IS NOT NULL AND LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    Page<CustomerProfile> searchCustomers(@Param("keyword") String keyword, Pageable pageable);
}
