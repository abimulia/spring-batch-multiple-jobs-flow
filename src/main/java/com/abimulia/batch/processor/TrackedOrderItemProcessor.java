package com.abimulia.batch.processor;

import java.util.UUID;

import org.springframework.batch.item.ItemProcessor;

import com.abimulia.batch.batch_process.exception.OrderProcessingException;
import com.abimulia.batch.batch_process.record.Order;
import com.abimulia.batch.batch_process.record.TrackedOrder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TrackedOrderItemProcessor implements ItemProcessor<Order,TrackedOrder> {

    @Override
    public TrackedOrder process(Order item) throws Exception {
        System.out.println("## Processing order with id: " + item.orderId());
        System.out.println("## Processing with thread " + Thread.currentThread().getName());

        TrackedOrder trackedOrder = new TrackedOrder(item);
        trackedOrder.setTrackingNumber(this.getTrackingNumber());
        return trackedOrder;
    }

    private String getTrackingNumber() throws OrderProcessingException {
        log.info("getTrackingNumber()");
        if (Math.random() <.05) {
            throw new OrderProcessingException("Failed to get tracking number");
        }
        return UUID.randomUUID().toString();

    }

}
