package com.abimulia.batch.processor;

import java.math.BigDecimal;

import org.springframework.batch.item.ItemProcessor;

import com.abimulia.batch.batch_process.record.TrackedOrder;

public class FreeShippingItemProcessor implements ItemProcessor<TrackedOrder, TrackedOrder>{

    @Override
    public TrackedOrder process(TrackedOrder item) throws Exception {
        if (item.getOrder().cost().compareTo(new BigDecimal("80"))==1) {
            item.setFreeShipping(true);
        }else{
            item.setFreeShipping(false);
        }

        return item.isFreeShipping() ? item:null;
    }

}
