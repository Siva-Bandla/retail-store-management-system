package com.retailstore.batch.stock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "v_stock_reconciliation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockReconciliationView {
    @Id
    private Long productId;

    private Integer warehouseStock;
    private Integer systemStock;
    private Integer stockDifference;

    private String status;
    private String message;
}
