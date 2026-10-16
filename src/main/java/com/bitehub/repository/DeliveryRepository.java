package com.bitehub.repository;

import com.bitehub.model.Delivery;
import com.bitehub.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByDeliveryPersonnelIdOrderByAssignedAtDesc(Long deliveryPersonnelId);
    List<Delivery> findByDeliveryPersonnelIdAndDeliveryStatus(Long deliveryPersonnelId, OrderStatus status);
}
