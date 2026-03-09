package com.example.bookingtour.repositories;
import com.example.bookingtour.entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {}