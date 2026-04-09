package com.retailstore.security;

import com.retailstore.payment.repository.PaymentRepository;
import com.retailstore.security.userdetails.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class PaymentSecurity {

    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentSecurity(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public boolean isOwnerByPaymentId(Long paymentId, Authentication authentication){

        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)){
            return false;
        }

        return paymentRepository.existsByIdAndOrderUserId(paymentId, userDetails.getId());
    }
}
