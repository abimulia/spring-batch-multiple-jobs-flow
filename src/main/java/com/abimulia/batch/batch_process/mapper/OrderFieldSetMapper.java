package com.abimulia.batch.batch_process.mapper;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.abimulia.batch.batch_process.record.Order;

public class OrderFieldSetMapper implements FieldSetMapper<Order> {

    @Override
    public Order mapFieldSet(FieldSet fieldSet) throws BindException {
        Order order = new Order(fieldSet.readLong("order_id"),
                fieldSet.readString("first_name"),
                fieldSet.readString("last_name"),
                fieldSet.readString("email"),
                fieldSet.readBigDecimal("cost"),
                fieldSet.readString("item_id"),
                fieldSet.readString("item_name"),
                fieldSet.readDate("ship_date"));
        return order;
    }

}
