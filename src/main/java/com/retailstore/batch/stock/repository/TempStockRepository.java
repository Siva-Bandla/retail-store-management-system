package com.retailstore.batch.stock.repository;

import com.retailstore.batch.stock.model.TempStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempStockRepository extends JpaRepository<TempStock, Long> {
}
