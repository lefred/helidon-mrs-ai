package main.java.me.test;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.http.HeaderNames;

final class ApiRoutes {
    private ApiRoutes() {}

    static void registerRoutes(HttpRouting.Builder r, MrsClient mrs) {
        // Gate all /api/** by login (optional – remove if you want it public)
        r.any("/api{+}", (req, res) -> {
            if (!Auth.isAuthenticated(req)) { res.status(401).send("Unauthorized"); return; }
            res.next();
        });

        // ------- ACTORS collection -------
        r.get("/api/actors", (req, res) -> {
            String body = mrs.get("/sakila/actor/", req.query().rawValue());
            res.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });
        r.get("/api/actors/", (req, res) -> {
            String body = mrs.get("/sakila/actor/", req.query().rawValue());
            res.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });

        // ------- Single actor -------
        r.get("/api/actors/{id}", (req, res) -> {
            String id = req.path().pathParameters().get("id");
            String body = mrs.get("/sakila/actor/" + id, null);
            res.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });

        // ------- Create / Update / Delete + broadcast -------
        r.post("/api/actors", (ServerRequest req2, ServerResponse res2) -> {
            String json = req2.content().as(String.class);
            String body = mrs.postJson("/sakila/actor/", json);
            SseHub.get().broadcastJson("{\"type\":\"actor.changed\",\"op\":\"create\"}");
            res2.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });

        r.patch("/api/actors/{id}", (req2, res2) -> {
            String id = req2.path().pathParameters().get("id");
            String json = req2.content().as(String.class);
            String body = mrs.patchJson("/sakila/actor/" + id, json);
            SseHub.get().broadcastJson("{\"type\":\"actor.changed\",\"op\":\"patch\",\"id\":"+id+"}");
            res2.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });

        r.put("/api/actors/{id}", (req2, res2) -> {
            String id = req2.path().pathParameters().get("id");
            String json = req2.content().as(String.class);
            String body = mrs.putJson("/sakila/actor/" + id, json);
            SseHub.get().broadcastJson("{\"type\":\"actor.changed\",\"op\":\"put\",\"id\":"+id+"}");
            res2.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });

        r.put("/api/actors/{id}/", (req2, res2) -> {
            String id = req2.path().pathParameters().get("id");
            String json = req2.content().as(String.class);
            String body = mrs.putJson("/sakila/actor/" + id, json);
            SseHub.get().broadcastJson("{\"type\":\"actor.changed\",\"op\":\"put\",\"id\":"+id+"}");
            res2.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });

        r.delete("/api/actors/{id}", (req2, res2) -> {
            String id = req2.path().pathParameters().get("id");
            String body = mrs.delete("/sakila/actor/" + id);
            SseHub.get().broadcastJson("{\"type\":\"actor.changed\",\"op\":\"delete\",\"id\":"+id+"}");
            res2.header(HeaderNames.CONTENT_TYPE, "application/json").send(body);
        });
    }
}
