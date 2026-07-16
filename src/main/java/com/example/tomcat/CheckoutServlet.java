package com.example.tomcat;

import com.example.tomcat.service.OrderService;
import com.example.tomcat.util.JsonResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Simulates checkout submission. Each request acquires a database connection
 * from the JNDI pool and performs a short transactional write that models
 * the cost of placing an order. Under concurrency this path competes for
 * pooled connections and worker threads.
 */
public class CheckoutServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(CheckoutServlet.class.getName());
    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        this.orderService = new OrderService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String customerParam = req.getParameter("customerId");
        String productParam = req.getParameter("productId");
        int customerId = parseOrDefault(customerParam, 1);
        int productId = parseOrDefault(productParam, 1);

        try {
            long orderId = orderService.placeOrder(customerId, productId);
            JsonResponseWriter.writeJson(resp, HttpServletResponse.SC_CREATED,
                    "{\"orderId\":" + orderId + ",\"status\":\"NEW\"}");
        } catch (Exception e) {
            LOG.warning("Checkout failed: " + e.getMessage());
            JsonResponseWriter.writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Checkout temporarily unavailable");
        }
    }

    private int parseOrDefault(String value, int fallback) {
        if (value == null) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
