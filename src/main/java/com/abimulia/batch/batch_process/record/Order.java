package com.abimulia.batch.batch_process.record;

import java.math.BigDecimal;
import java.util.Date;

public record Order(Long orderId, String firstName, String lastName, String email, BigDecimal cost, String itemId,
        String itemName, Date shipDate) {

}
