package com.bitehub.repository;

import com.bitehub.model.Order;
import com.bitehub.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerIdOrderByPlacedAtDesc(Long customerId);
    List<Order> findByRestaurantIdOrderByPlacedAtDesc(Long restaurantId);
    List<Order> findByDeliveryPersonnelIdOrderByPlacedAtDesc(Long deliveryPersonnelId);
    List<Order> findByOrderStatusOrderByPlacedAtDesc(OrderStatus orderStatus);
    List<Order> findAllByOrderByPlacedAtDesc();

    long countByRestaurantIdAndOrderStatus(Long restaurantId, OrderStatus status);
    long countByOrderStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.restaurant.id = :restaurantId AND o.orderStatus = 'DELIVERED'")
    Double calculateTotalRevenueByRestaurant(@Param("restaurantId") Long restaurantId);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0.0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    Double calculateTotalSystemRevenue();

    @Query("SELECT o FROM Order o WHERE o.placedAt BETWEEN :startDate AND :endDate ORDER BY o.placedAt DESC")
    List<Order> findOrdersBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE o.restaurant.id = :restaurantId AND o.placedAt BETWEEN :startDate AND :endDate ORDER BY o.placedAt DESC")
    List<Order> findRestaurantOrdersBetweenDates(@Param("restaurantId") Long restaurantId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
