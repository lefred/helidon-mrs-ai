package main.java.me.test;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.http.HeaderNames;

import io.helidon.webserver.staticcontent.StaticContentFeature;
import io.helidon.webserver.staticcontent.ClasspathHandlerConfig;

final class Ui {
    private Ui() {}

    static void registerRoutes(HttpRouting.Builder r) {
        // --- Guards (must come BEFORE handlers they protect) ---
        r.any("/ui", (ServerRequest req, ServerResponse res) -> {
            if (!Auth.isAuthenticated(req)) {
                res.status(302).header(HeaderNames.LOCATION, "/login").send();
                return;
            }
            res.next();
        });
        r.any("/ui{+}", (ServerRequest req, ServerResponse res) -> {
            if (!Auth.isAuthenticated(req)) {
                res.status(302).header(HeaderNames.LOCATION, "/login").send();
                return;
            }
            res.next();
        });

        // Convenience redirects so /ui and /ui/ both work
        r.get("/ui", (req, res) -> res.status(302)
                .header(HeaderNames.LOCATION, "/ui/")
                .send());
        r.get("/ui/", (req, res) -> res.status(302)
                .header(HeaderNames.LOCATION, "/ui/index.html")
                .send());

        // Serve static UI from classpath: src/main/resources/WEB
        r.register("/ui",
            StaticContentFeature.createService(
                ClasspathHandlerConfig.builder()
                    .location("WEB")   // the resources folder name
                    .build()
            )
        );
    }
}
