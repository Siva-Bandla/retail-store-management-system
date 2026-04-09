package com.retailstore.batch.stock.repository;

import com.retailstore.batch.stock.model.StockReconciliationView;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockReconciliationViewRepository extends JpaRepository<StockReconciliationView, Long> {
}
