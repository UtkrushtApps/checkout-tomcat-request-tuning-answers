package com.example.tomcat.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Assigns a correlation identifier to each request and exposes it as a
 * request attribute and response header so downstream logging and clients
 * can tie together the lifecycle of a single request.
 */
public class RequestCorrelationFilter implements Filter {

    public static final String CORRELATION_ATTR = "requestId";
    public static final String CORRELATION_HEADER = "X-Request-Id";

    @Override
    public void init(FilterConfig filterConfig) {
        // no configuration required
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpResp = (HttpServletResponse) response;

        String incoming = httpReq.getHeader(CORRELATION_HEADER);
        String requestId = (incoming != null && !incoming.isEmpty())
                ? incoming
                : UUID.randomUUID().toString();

        httpReq.setAttribute(CORRELATION_ATTR, requestId);
        httpResp.setHeader(CORRELATION_HEADER, requestId);

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // no resources to release
    }
}
