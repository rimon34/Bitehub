package com.bitehub.repository;

import com.bitehub.model.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi.itemName, SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.orderStatus != 'CANCELLED' " +
           "GROUP BY oi.itemName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostOrderedItems(Pageable pageable);

    @Query("SELECT oi.itemName, SUM(oi.quantity), SUM(oi.subtotal) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.orderStatus != 'CANCELLED' AND oi.order.restaurant.id = :restaurantId " +
           "GROUP BY oi.itemName " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findMostOrderedItemsByRestaurant(@Param("restaurantId") Long restaurantId, Pageable pageable);
}
