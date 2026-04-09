package com.retailstore.batch.stock.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "temp_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TempStock {
    @Id
    private Long productId;
    private Integer actualStock;
}
