package com.retailstore.security;

import com.retailstore.order.repository.OrderRepository;
import com.retailstore.security.userdetails.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class OrderSecurity {

    private final OrderRepository orderRepository;

    @Autowired
    public OrderSecurity(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Checks whether the currently authenticated user is the owner of the order.
     *
     * @param orderId       ID of the order
     * @param authentication Spring Security authentication object
     * @return true if user is owner, false otherwise
     */
    public boolean isOwnerByOrderId(Long orderId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return orderRepository.existsByIdAndUserId(orderId, userDetails.getId());
    }
}
