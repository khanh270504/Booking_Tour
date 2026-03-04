package com.example.bookingtour.Repositories;
import com.example.bookingtour.Entities.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface DestinationRepository extends JpaRepository<Destination, Integer> {}