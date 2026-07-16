package com.example.tomcat.util;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Small helper for emitting consistent JSON responses and error payloads.
 */
public final class JsonResponseWriter {

    private JsonResponseWriter() {
    }

    public static void writeJson(HttpServletResponse resp, int status, String jsonBody)
            throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.write(jsonBody);
        }
    }

    public static void writeError(HttpServletResponse resp, int status, String message)
            throws IOException {
        writeJson(resp, status, "{\"error\":\"" + message + "\"}");
    }
}
