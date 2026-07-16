# Solution Steps

1. Update Tomcat request/worker and keep-alive behavior to avoid slow clients and long-lived connections from amplifying stalls: edit `conf/server.xml` connector settings (smaller `connectionTimeout`, bounded `keepAliveTimeout` and `maxKeepAliveRequests`, and a reasonable `maxThreads`).

2. Bound JDBC pool wait time so threads fail fast when all connections are in use: edit `conf/context.xml` for `jdbc/CheckoutDS` and set `maxWaitMillis` to a small positive value (e.g., 1000ms) instead of `-1` (wait forever). Keep `maxTotal` at the DBA-agreed maximum (8) so the app cannot exceed the DB connection ceiling.

3. Make incidents diagnosable from the access log by adding the correlation identifier and elapsed time: edit `conf/server.xml` AccessLogValve `pattern` to log `X-Request-Id` (set by `RequestCorrelationFilter`), HTTP method/path (`%m %U%q`), status (`%s`), and elapsed time (`%T`).

4. Ensure failure responses are prompt and clearly transient for the read path too: adjust `src/main/java/com/example/tomcat/AppServlet.java` so `/orders` returns HTTP 503 when database/pool access fails (instead of 500), matching the “fail fast” expectation.

5. Rebuild and redeploy: run `mvn -q -DskipTests package` (the provided `run.sh` does this) and start with `docker compose up -d --build`. Confirm health at `/checkoutweb/health`.

6. Run a concurrency burst: execute `bash scripts/load_test.sh 40` (or higher) and verify that checkout POSTs complete with bounded latency and that failed requests return quickly (HTTP 503) rather than hanging for long periods.

7. Validate /orders stability during the same burst: while checkout load runs, repeatedly curl `/checkoutweb/orders` and ensure latency remains bounded; failures should also be quick (503) when the pool is exhausted.

8. Check the database connection ceiling: during the test, observe active DB connections from PostgreSQL (e.g., via `pg_stat_activity`) and confirm the app does not exceed the configured `maxTotal` (8) concurrent connections.

9. Verify correlation end-to-end after an incident: pick a request ID from client/response headers (`X-Request-Id`) and confirm the same value appears in `localhost_access_log*.txt` along with path, status, and elapsed time.

