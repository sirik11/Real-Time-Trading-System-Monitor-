package com.trademonitor.repository;

import com.trademonitor.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(String status);

    @Query("SELECT o FROM Order o WHERE o.side = :side AND o.instrument = :instrument " +
           "AND o.status = 'OPEN' AND " +
           "CASE WHEN :isBuy = true THEN o.price <= :price ELSE o.price >= :price END " +
           "ORDER BY CASE WHEN :isBuy = true THEN o.price END ASC, " +
           "CASE WHEN :isBuy = false THEN o.price END DESC, o.createdAt ASC")
    List<Order> findMatchCandidates(@Param("side") String side,
                                    @Param("instrument") String instrument,
                                    @Param("price") BigDecimal price,
                                    @Param("isBuy") boolean isBuy);
}
