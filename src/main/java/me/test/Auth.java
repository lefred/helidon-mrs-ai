package main.java.me.test;

import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;
import io.helidon.common.parameters.Parameters;
import io.helidon.http.HeaderNames;
import io.helidon.http.SetCookie;
import io.helidon.http.SetCookie.SameSite;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mindrot.jbcrypt.BCrypt;

final class Auth {
    static final String SESSION_COOKIE = "SESSION";
    private static final ConcurrentHashMap<String, String> SESSIONS = new ConcurrentHashMap<>();

    // bcrypt field finder (supports passwordHash or password_hash)
    private static final Pattern HASH_FIELD =
            Pattern.compile("\"(passwordHash|password_hash)\"\\s*:\\s*\"([^\"]+)\"");

    private Auth() {}

    static void registerRoutes(HttpRouting.Builder r, MrsClient mrs) {
        r.get("/login", (req, res) -> {
            if (isAuthenticated(req)) {
                res.status(302).header(HeaderNames.LOCATION, "/ui").send();
                return;
            }
            res.header(HeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
               .send(LoginPage.html(null));
        });

        r.post("/auth/login", (ServerRequest req2, ServerResponse res2) -> {
            try {
                Parameters form = req2.content().as(Parameters.class); // x-www-form-urlencoded
                String username = form.first("username").orElse("");
                String password = form.first("password").orElse("");

                if (username.isBlank() || password.isBlank()) {
                    res2.status(400).header(HeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                       .send(LoginPage.html("Please enter username and password."));
                    return;
                }

                String qJson = "{\"username\":{\"$eq\":\"" + escapeJson(username) + "\"}}";
                String encoded = "q=" + URLEncoder.encode(qJson, StandardCharsets.UTF_8) + "&limit=1";
                String json = mrs.get("/sakila/users/", encoded);

                String storedHash = findFirstPasswordHash(json).map(String::trim).orElse(null);
                boolean ok = false;
                if (storedHash != null && !storedHash.isBlank()) {
                    try { ok = BCrypt.checkpw(password, storedHash); } catch (Exception ignore) {}
                }
                if (!ok) {
                    res2.status(401).header(HeaderNames.CONTENT_TYPE, "text/html; charset=utf-8")
                       .send(LoginPage.html("Invalid credentials."));
                    return;
                }

                String sid = newSession(username);
                SetCookie cookie = SetCookie.builder(SESSION_COOKIE, sid)
                        .httpOnly(true)
                        .sameSite(SameSite.LAX)
                        .path("/")
                        .maxAge(Duration.ofHours(8))
                        .build();
                res2.headers().addCookie(cookie);
                res2.status(302).header(HeaderNames.LOCATION, "/ui").send();
            } catch (Exception e) {
                res2.status(500).send("Login error: " + e.getMessage());
            }
        });

        r.get("/auth/logout", (req2, res2) -> {
            sessionId(req2).ifPresent(SESSIONS::remove);
            SetCookie cookie = SetCookie.builder(SESSION_COOKIE, "")
                    .httpOnly(true)
                    .sameSite(SameSite.LAX)
                    .path("/")
                    .maxAge(Duration.ZERO)
                    .build();
            res2.headers().addCookie(cookie);
            res2.status(302).header(HeaderNames.LOCATION, "/login").send();
        });

        // helper to generate a bcrypt hash (to seed the table)
        r.get("/_debug/hash", (req, res) -> {
            String pwd = req.query().first("pwd").orElse("");
            if (pwd.isBlank()) { res.status(400).send("Usage: /_debug/hash?pwd=yourPassword"); return; }
            String h = BCrypt.hashpw(pwd, BCrypt.gensalt(12));
            res.send(h);
        });
    }

    static boolean isAuthenticated(ServerRequest req) {
        return sessionId(req).map(SESSIONS::containsKey).orElse(false);
    }

    static Optional<String> sessionId(ServerRequest req) {
        try {
            return req.headers().cookies().first(SESSION_COOKIE).asOptional();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static String newSession(String username) {
        byte[] buf = new byte[24];
        ThreadLocalRandom.current().nextBytes(buf);
        String id = Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
        SESSIONS.put(id, username);
        return id;
    }

    private static Optional<String> findFirstPasswordHash(String json) {
        Matcher m = HASH_FIELD.matcher(json);
        return m.find() ? Optional.ofNullable(m.group(2)) : Optional.empty();
    }

    static String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    static String escapeHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
                .replace("\"","&quot;").replace("'","&#39;");
    }
}
