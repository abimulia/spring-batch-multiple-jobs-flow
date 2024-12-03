package com.abimulia.batch.batch_process.mapper;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.abimulia.batch.batch_process.record.Order;

public class OrderRowMapper implements RowMapper<Order> {

    @Override
    public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
        Order order = new Order(rs.getLong("order_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getBigDecimal("cost"),
                rs.getString("item_id"),
                rs.getString("item_name"),
                rs.getDate("ship_date"));
        return order;
    }

}
