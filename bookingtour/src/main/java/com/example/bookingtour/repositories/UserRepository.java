package com.example.bookingtour.repositories;

import com.example.bookingtour.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    @Query("SELECT u FROM User u WHERE u.role.roleName = 'ADMIN' OR u.role.roleName = 'KETOAN'")
    List<User> findAdminAndKetoan();
}
