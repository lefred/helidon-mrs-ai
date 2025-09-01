package main.java.me.test;

import io.helidon.http.HeaderNames;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import main.java.me.test.ai.DiscoveryChatService;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

final class AiRoutes {
    private AiRoutes() {}

    static void registerRoutes(HttpRouting.Builder r, MrsClient mrs, DiscoveryChatService explainer) {
        r.any("/ai/actors/{id}/explain", (ServerRequest req, ServerResponse res) -> {
            if (!Auth.isAuthenticated(req)) { res.status(401).send("Unauthorized"); return; }
            res.next();
        });

        r.get("/ai/actors/{id}/explain", (req, res) -> {
            String id = req.path().pathParameters().get("id");

            // Tell intermediaries not to buffer, and start chunked HTML
            res.header(HeaderNames.CACHE_CONTROL, "no-cache, no-transform");
            res.header(HeaderNames.create("X-Accel-Buffering"), "no"); // nginx
            res.header(HeaderNames.CONTENT_TYPE, "text/html; charset=utf-8");

            OutputStream out = res.outputStream();

            // 1) Send an immediate shell with a spinner and some padding (helps certain proxies/browsers flush early)
            String shell = """
                <!doctype html>
<meta charset="utf-8">
<title>Explaining actor…</title>
<style>
  :root { color-scheme: light dark; }
  body { font:16px/1.5 system-ui,sans-serif; margin:24px; }
  /* Center everything while loading */
  body.loading { min-height: 100svh; display: grid; place-items: center; }
  #app.loading { color:#666; display:flex; align-items:center; gap:12px; }
  .spinner{
    width:28px;height:28px;border-radius:50%;
    border:3px solid #ddd;border-top-color:#555;
    animation:spin 1s linear infinite
  }
  @keyframes spin{to{transform:rotate(360deg)}}
</style>
<body class="loading">
  <div id="app" class="loading" role="status" aria-live="polite">
    <span class="spinner" aria-hidden="true"></span>
    <span>Generating explanation using OCI GenAI…</span>
  </div>
</body>
                """;
            // Add ~2KB of padding to encourage early render
            shell += " ".repeat(2048);
            out.write(shell.getBytes(StandardCharsets.UTF_8));
            out.flush(); // 👈 force the client to render now


            String actorJson = mrs.get("/sakila/actor/" + id, null);
            String prompt =
                "Explain this actor record in a HTML snippet. " +
                "Highlight first/last name and lastUpdate. Provide a summary of the actor's filmography " +
                "with dates and languages. Please highlight the best and the worse movie explaining why with details, " +
                "including rating and rentalRate." +
                "Could you also add a chart of the categories of the movies this actor played in?" +
                " Output ONLY HTML (no <html> wrapper).\n\n"
                + actorJson;
            String html = explainer.chat(prompt);

            String safe = html
                    .replace("\\", "\\\\")
                    .replace("`", "\\`")
                    .replace("</script>", "<\\\\/script>");

            String inject = """
                <script>
                  const target = document.getElementById('app');
                  document.body.classList.remove('loading');
                  target.classList.remove('loading');
                  target.innerHTML = `%s`;
                </script>
                """.formatted(safe);

            out.write(inject.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.close();
            //res.header(HeaderNames.CONTENT_TYPE, "text/html; charset=utf-8").send(html);
        });

    }
}
