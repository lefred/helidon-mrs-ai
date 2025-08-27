package main.java.me.test;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.webserver.sse.SseSink;

final class SseRoutes {
    private SseRoutes() {}

    static void registerRoutes(HttpRouting.Builder r) {
        // Require login for /events
        r.any("/events", (ServerRequest req, ServerResponse res) -> {
            if (!Auth.isAuthenticated(req)) { res.status(401).send("Unauthorized"); return; }
            res.next();
        });

        r.get("/events", (req, res) -> {
            try (SseSink sink = res.sink(SseSink.TYPE)) {
                var hub = SseHub.get();
                hub.add(sink);
                // Keep connection open with a gentle heartbeat; hub will also push change events
                while (true) {
                    try {
                        Thread.sleep(15000);
                        hub.broadcastJson("{\"type\":\"ping\",\"ts\":"+System.currentTimeMillis()+"}");
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt(); break;
                    } catch (Exception e) {
                        break; // client disconnected
                    }
                }
                hub.remove(sink);
            } catch (Exception ignore) { }
        });
    }
}