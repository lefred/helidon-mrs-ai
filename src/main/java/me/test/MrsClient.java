package main.java.me.test;

import io.helidon.webclient.api.WebClient;
import io.helidon.webclient.api.HttpClientRequest;
import io.helidon.webclient.api.HttpClientResponse;
import io.helidon.http.HeaderNames;
import io.helidon.common.media.type.MediaTypes;   // <-- use this
import io.helidon.webclient.api.Proxy;
import io.helidon.common.tls.Tls;

public class MrsClient {
    private final WebClient client;
    private final String baseServicePath; // e.g., https://localhost:8443/myService
    private volatile String bearer;       // "Bearer <token>"

    // New: creds from -D props or env
    private final String username = System.getProperty("mrs.username", System.getenv("MRS_USERNAME"));
    private final String password = System.getProperty("mrs.password", System.getenv("MRS_PASSWORD"));
    private final String authApp  = System.getProperty("mrs.authApp",  System.getenv("MRS_AUTH_APP"));
    // sessionType=bearer yields a JWT-like accessToken
    private final String sessionType = System.getProperty("mrs.sessionType", "bearer");

    public MrsClient(String baseUrl, String ignoredBearerToken) {
            var builder = WebClient.builder().baseUri(baseUrl);

    // Disable proxy if requested (prevents corporate/system proxy from intercepting)
    if (Boolean.getBoolean("mrs.noProxy")) {
        builder = builder.proxy(Proxy.noProxy());
    }

    if (baseUrl.startsWith("https")) {
        builder = builder.tls(tls -> tls
            .trustAll(Boolean.getBoolean("mrs.insecureTls"))
            .endpointIdentificationAlgorithm(Tls.ENDPOINT_IDENTIFICATION_NONE) // <—
        );
    }

    this.client = builder.build();
    this.baseServicePath = baseUrl;
    loginIfPossible(); // your existing method
}

    private void applyCommon(HttpClientRequest req) {
        if (bearer != null) {
            req.headers().set(HeaderNames.AUTHORIZATION, bearer);
        }
        req.accept(MediaTypes.APPLICATION_JSON);
    }

    // ---- NEW: programmatic login to /authentication/login
    private synchronized void loginIfPossible() {
        if (bearer != null || username == null || password == null || authApp == null) return;
        String payload = """
            {"username":"%s","password":"%s","authApp":"%s","sessionType":"%s"}
            """.formatted(username, password, authApp, sessionType);

        HttpClientRequest req = client.post()
                .path("/authentication/login")
                .contentType(MediaTypes.APPLICATION_JSON);

        try (HttpClientResponse res = req.submit(payload)) {
            String body = res.as(String.class);
            // parse minimal JSON to extract accessToken without a full JSON lib
            String marker = "\"accessToken\"";
            int i = body.indexOf(marker);
            if (i > -1) {
                int colon = body.indexOf(':', i);
                int q1 = body.indexOf('"', colon + 1);
                int q2 = body.indexOf('"', q1 + 1);
                String token = body.substring(q1 + 1, q2);
                this.bearer = "Bearer " + token;
            } else {
                throw new IllegalStateException("Login failed: " + body);
            }
        }
    }

    // ---- helper to perform request with 401 retry
    private String withAuthRetry(java.util.function.Supplier<HttpClientResponse> call) {
        try (HttpClientResponse res = call.get()) {
            if (res.status().code() == 401) {
                // try once to re-login and retry
                this.bearer = null;
                loginIfPossible();
                try (HttpClientResponse res2 = call.get()) {
                    return res2.as(String.class);
                }
            }
            return res.as(String.class);
        }
    }

    public String get(String path, String rawQuery) {
    HttpClientRequest req = client.get().path(path); // <-- use path, not uri
    applyCommon(req);

    // Add query params safely (supports q and limit/offset, etc.)
    if (rawQuery != null && !rawQuery.isBlank()) {
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) continue;
            int eq = pair.indexOf('=');
            String name, value;
            if (eq >= 0) {
                name  = java.net.URLDecoder.decode(pair.substring(0, eq), java.nio.charset.StandardCharsets.UTF_8);
                value = java.net.URLDecoder.decode(pair.substring(eq + 1), java.nio.charset.StandardCharsets.UTF_8);
            } else {
                name  = java.net.URLDecoder.decode(pair, java.nio.charset.StandardCharsets.UTF_8);
                value = "";
            }
            req.queryParam(name, value);
        }
    }

    try (HttpClientResponse res = req.request()) {
        return res.as(String.class);
    }
}

    public String postJson(String path, String json) {
        return withAuthRetry(() -> {
            HttpClientRequest req = client.post().path(path).contentType(MediaTypes.APPLICATION_JSON);
            applyCommon(req);
            return req.submit(json);
        });
    }

    public String patchJson(String path, String json) {
        return withAuthRetry(() -> {
            HttpClientRequest req = client.patch().path(path).contentType(MediaTypes.APPLICATION_JSON);
            applyCommon(req);
            return req.submit(json);
        });
    }

    public String putJson(String path, String json) {
        return withAuthRetry(() -> {
            HttpClientRequest req = client.put().path(path).contentType(MediaTypes.APPLICATION_JSON);
            applyCommon(req);
            return req.submit(json);
        });
    }

    public String delete(String path) {
        return withAuthRetry(() -> {
            HttpClientRequest req = client.delete().path(path);
            applyCommon(req);
            return req.request();
        });
    }
}
