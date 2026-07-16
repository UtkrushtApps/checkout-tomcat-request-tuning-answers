package com.example.tomcat.dao;

import com.example.tomcat.model.Order;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data access for orders using the container-managed JNDI datasource
 * bound at java:comp/env/jdbc/CheckoutDS.
 */
public class OrderDao {

    private DataSource dataSource() throws NamingException {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        return (DataSource) envCtx.lookup("jdbc/CheckoutDS");
    }

    public boolean ping() {
        try (Connection conn = dataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (NamingException | SQLException e) {
            return false;
        }
    }

    public List<Order> findAll() throws SQLException, NamingException {
        String sql = "SELECT id, customer_id, status, total_cents FROM orders ORDER BY id";
        List<Order> orders = new ArrayList<>();
        try (Connection conn = dataSource().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                orders.add(new Order(
                        rs.getLong("id"),
                        rs.getInt("customer_id"),
                        rs.getString("status"),
                        rs.getInt("total_cents")
                ));
            }
        }
        return orders;
    }

    public long insertOrder(int customerId, int productId) throws SQLException, NamingException {
        String priceSql = "SELECT price_cents FROM products WHERE id = ?";
        String orderSql = "INSERT INTO orders (customer_id, status, total_cents) VALUES (?, 'NEW', ?) RETURNING id";
        String itemSql = "INSERT INTO order_items (order_id, product_id, quantity) VALUES (?, ?, 1)";

        try (Connection conn = dataSource().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int price;
                try (PreparedStatement ps = conn.prepareStatement(priceSql)) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) {
                        price = rs.next() ? rs.getInt(1) : 0;
                    }
                }

                simulateProcessingLatency();

                long orderId;
                try (PreparedStatement ps = conn.prepareStatement(orderSql)) {
                    ps.setInt(1, customerId);
                    ps.setInt(2, price);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        orderId = rs.getLong(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement(itemSql)) {
                    ps.setLong(1, orderId);
                    ps.setInt(2, productId);
                    ps.executeUpdate();
                }
                conn.commit();
                return orderId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Models the realistic processing cost of a checkout write under load.
     * This sleep keeps a database connection held for its duration, which
     * makes pool exhaustion observable when many requests arrive concurrently.
     */
    private void simulateProcessingLatency() {
        try {
            Thread.sleep(400L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
