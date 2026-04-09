package com.retailstore.inventory.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long id;

    @Column(name = "product_id", nullable = false, unique = true)
    private Long productId;

    @Positive
    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "is_Deleted")
    private Boolean deleted = false;
}
