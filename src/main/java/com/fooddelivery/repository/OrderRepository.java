package com.fooddelivery.repository;
import com.fooddelivery.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    Page<Order> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);
    Page<Order> findByDeliveryPartnerId(Long partnerId, Pageable pageable);
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND (:status IS NULL OR o.status = :status) ORDER BY o.createdAt DESC")
    Page<Order> findByCustomerIdAndStatus(@Param("customerId") Long customerId,
                                          @Param("status") Order.OrderStatus status,
                                          Pageable pageable);

    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);
}
