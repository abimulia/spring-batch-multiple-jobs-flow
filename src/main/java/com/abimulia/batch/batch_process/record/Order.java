package com.abimulia.batch.batch_process.record;

import java.math.BigDecimal;
import java.util.Date;

import jakarta.validation.constraints.Pattern;



public record Order(Long orderId, 
        String firstName, 
        String lastName, 
        @Pattern(regexp = ".*\\.gov", message = "Email must use government domain (.gov)")
        String email, 
        BigDecimal cost, 
        String itemId,
        String itemName, 
        Date shipDate) {

}
