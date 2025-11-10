
package main.java.me.test;

import io.helidon.config.Config;
import io.helidon.logging.common.LogConfig;
import io.helidon.service.registry.Services;
import io.helidon.webserver.WebServer;

import main.java.me.test.ai.DiscoveryChatService;

public class Main {
    public static void main(String[] args) {

        // load logging configuration
        LogConfig.configureRuntime();

        // initialize config from default configuration
        Config config = Config.create();

        final DiscoveryChatService chatService;

        // MRS client setup
        String baseUrl = System.getProperty("mrs.url", System.getenv("MRS_URL"));
        if (baseUrl == null || baseUrl.isBlank()) {
            System.err.println("Missing -Dmrs.url (e.g., https://host:33060/myService/)");
            System.exit(1);
        }
        final MrsClient mrs = new MrsClient(baseUrl, "");

        try {
            chatService = Services.get(DiscoveryChatService.class);
            System.out.println("Service initialized: " + chatService);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize service: " + e.getMessage());
            return;
        }

        WebServer server = WebServer.builder()
                .config(config.get("server"))
                .routing((r) -> {
                    r.get("/", (req, res) -> res.send("Helidon 4.3 + MySQL REST Service + AI"));

                    //r.post("/chat", (req, res) -> {
                    //    var prompt = req.content().as(String.class);
                    //    var response = chatService.chat(prompt);
                    //    res.send(response);
                    //});

                    Auth.registerRoutes(r, mrs);
                    Ui.registerRoutes(r);
                    ApiRoutes.registerRoutes(r, mrs);
                    SseRoutes.registerRoutes(r);
                    AiRoutes.registerRoutes(r, mrs, chatService);
                })
                .build()
                .start();

        System.out.println("WEB server is up! http://localhost:" + server.port() + "/simple-greet");
    }
}
