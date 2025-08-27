package main.java.me.test;

import io.helidon.http.HeaderNames;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import main.java.me.test.ai.DiscoveryChatService;

final class AiRoutes {
    private AiRoutes() {}

    static void registerRoutes(HttpRouting.Builder r, MrsClient mrs, DiscoveryChatService explainer) {
        r.any("/ai/actors/{id}/explain", (ServerRequest req, ServerResponse res) -> {
            if (!Auth.isAuthenticated(req)) { res.status(401).send("Unauthorized"); return; }
            res.next();
        });

        r.get("/ai/actors/{id}/explain", (req, res) -> {
            String id = req.path().pathParameters().get("id");
            String actorJson = mrs.get("/sakila/actor/" + id, null);
            String prompt =
                "Explain this actor record in a HTML snippet. " +
                "Highlight first/last name and lastUpdate. Provide a summary of the actor's filmography " +
                "with dates and languages. Please highlight the best and the worse movie explaining why." +
                "Output ONLY HTML (no <html> wrapper).\n\n"
                + actorJson;
            String html = explainer.chat(prompt);
            res.header(HeaderNames.CONTENT_TYPE, "text/html; charset=utf-8").send(html);
        });

    }
}
