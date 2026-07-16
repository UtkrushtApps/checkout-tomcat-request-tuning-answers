package com.example.tomcat.service;

import com.example.tomcat.dao.OrderDao;
import com.example.tomcat.model.Order;

import java.util.List;

/**
 * Coordinates order-related business flows. Business logic here is complete;
 * request stability under load depends on the surrounding container and
 * resource configuration rather than on changes to this class.
 */
public class OrderService {

    private final OrderDao orderDao = new OrderDao();

    public boolean canReachDatabase() {
        return orderDao.ping();
    }

    public List<Order> listOrders() throws Exception {
        return orderDao.findAll();
    }

    public long placeOrder(int customerId, int productId) throws Exception {
        return orderDao.insertOrder(customerId, productId);
    }
}
