package com.abimulia.batch.processor;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;

import com.abimulia.batch.batch_process.record.Order;
import com.abimulia.batch.batch_process.record.TrackedOrder;

public class TrackedOrderItemProcessor implements ItemProcessor<Order,TrackedOrder> {

    @Override
    public TrackedOrder process(Order item) throws Exception {
        TrackedOrder trackedOrder = new TrackedOrder(item);
        trackedOrder.setTrackingNumber(UUID.randomUUID().toString());
        return trackedOrder;
    }

}
