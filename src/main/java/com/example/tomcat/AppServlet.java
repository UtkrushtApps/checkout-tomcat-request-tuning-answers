package com.example.tomcat;

import com.example.tomcat.model.Order;
import com.example.tomcat.service.OrderService;
import com.example.tomcat.util.JsonResponseWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Handles lightweight and read-oriented request paths:
 *   /health  - container-observable readiness signal backed by a database ping
 *   /orders  - database-backed order listing via the JNDI datasource
 */
public class AppServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(AppServlet.class.getName());
    private OrderService orderService;

    @Override
    public void init() throws ServletException {
        this.orderService = new OrderService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath();
        if ("/health".equals(path)) {
            handleHealth(resp);
        } else if ("/orders".equals(path)) {
            handleOrders(resp);
        } else {
            JsonResponseWriter.writeError(resp, HttpServletResponse.SC_NOT_FOUND, "Unknown path");
        }
    }

    private void handleHealth(HttpServletResponse resp) throws IOException {
        boolean dbReachable = orderService.canReachDatabase();
        if (dbReachable) {
            JsonResponseWriter.writeJson(resp, HttpServletResponse.SC_OK,
                    "{\"status\":\"UP\",\"database\":\"UP\"}");
        } else {
            JsonResponseWriter.writeJson(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "{\"status\":\"DEGRADED\",\"database\":\"DOWN\"}");
        }
    }

    private void handleOrders(HttpServletResponse resp) throws IOException {
        try {
            List<Order> orders = orderService.listOrders();
            StringBuilder sb = new StringBuilder("{\"orders\":[");
            for (int i = 0; i < orders.size(); i++) {
                if (i > 0) sb.append(',');
                sb.append(orders.get(i).toJson());
            }
            sb.append("]}");
            JsonResponseWriter.writeJson(resp, HttpServletResponse.SC_OK, sb.toString());
        } catch (Exception e) {
            // Under pool exhaustion we want bounded latency with a fast failure.
            // Return 503 (rather than 500) so clients can treat it as a transient outage.
            LOG.warning("Order listing failed: " + e.getMessage());
            JsonResponseWriter.writeError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Order listing temporarily unavailable");
        }
    }
}
