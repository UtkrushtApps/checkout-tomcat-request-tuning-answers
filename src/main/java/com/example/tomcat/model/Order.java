package com.example.tomcat.model;

/**
 * Represents an order row surfaced by the read and checkout request paths.
 */
public class Order {

    private final long id;
    private final int customerId;
    private final String status;
    private final int totalCents;

    public Order(long id, int customerId, String status, int totalCents) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalCents = totalCents;
    }

    public long getId() { return id; }
    public int getCustomerId() { return customerId; }
    public String getStatus() { return status; }
    public int getTotalCents() { return totalCents; }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"customerId\":" + customerId
                + ",\"status\":\"" + status + "\""
                + ",\"totalCents\":" + totalCents + "}";
    }
}
