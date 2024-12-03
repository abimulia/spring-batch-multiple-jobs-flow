package com.abimulia.batch.batch_process.mapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import com.abimulia.batch.batch_process.record.Order;

public class OrderItemPreparedStatementSetter implements ItemPreparedStatementSetter<Order> {

    @Override
    public void setValues(Order item, PreparedStatement ps) throws SQLException {
        ps.setLong(1, item.orderId());
		ps.setString(2, item.firstName());
		ps.setString(3, item.lastName());
		ps.setString(4,  item.email());
		ps.setString(5,item.itemId());
		ps.setString(6, item.itemName());
		ps.setBigDecimal(7, item.cost());
		ps.setDate(8, new Date(item.shipDate().getTime()));
    }

}
