<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>CheckoutWeb — NorthPeak Retail</title>
    <style>
        body { font-family: system-ui, sans-serif; margin: 2rem; color: #222; }
        code { background: #f2f2f2; padding: 2px 5px; border-radius: 4px; }
        li { margin: 6px 0; }
    </style>
</head>
<body>
    <h1>CheckoutWeb</h1>
    <p>Order-processing service for NorthPeak Retail. The deployment is live; explore the request paths below.</p>
    <ul>
        <li><a href="health">Health check</a> — <code>GET /checkoutweb/health</code></li>
        <li><a href="orders">Order lookup</a> — <code>GET /checkoutweb/orders</code></li>
        <li>Checkout submission — <code>POST /checkoutweb/checkout/place</code></li>
    </ul>
    <p>Session id: <code><%= session.getId() %></code></p>
</body>
</html>
