package com.trademonitor.repository;

import com.trademonitor.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {

    @Query(value = "SELECT * FROM trades ORDER BY executed_at DESC LIMIT :limit", nativeQuery = true)
    List<Trade> findTopNByOrderByExecutedAtDesc(@Param("limit") int limit);
}
