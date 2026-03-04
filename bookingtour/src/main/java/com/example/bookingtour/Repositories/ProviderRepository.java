package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface ProviderRepository extends JpaRepository<Provider, Integer> {}