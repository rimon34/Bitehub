package com.bitehub.repository;

import com.bitehub.model.DeliveryPersonnel;
import com.bitehub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPersonnelRepository extends JpaRepository<DeliveryPersonnel, Long> {
    Optional<DeliveryPersonnel> findByUser(User user);
    Optional<DeliveryPersonnel> findByUserId(Long userId);
    List<DeliveryPersonnel> findByIsAvailableTrue();
}
