package com.retailstore.testdata;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestDataConfig {

    @Bean public UserTestData userTestData(){return new UserTestData(); }
    @Bean public ProductTestData productTestData(){return new ProductTestData(); }
    @Bean public CategoryTestData categoryTestData(){return new CategoryTestData(); }
    @Bean public CartTestData cartTestData(){return new CartTestData(); }
    @Bean public OrderTestData orderTestData(){return new OrderTestData(); }
    @Bean public InventoryTestData inventoryTestData(){return new InventoryTestData(); }
    @Bean public PaymentTestData paymentTestData(){return new PaymentTestData(); }
    @Bean public AddressTestData addressTestData(){return new AddressTestData(); }
}
