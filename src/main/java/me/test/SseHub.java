package main.java.me.test;

import io.helidon.webserver.sse.SseSink;
import io.helidon.http.sse.SseEvent;
import io.helidon.common.media.type.MediaTypes;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

final class SseHub {
    private static final SseHub INSTANCE = new SseHub();
    static SseHub get() { return INSTANCE; }

    private final Set<SseSink> sinks = ConcurrentHashMap.newKeySet();

    void add(SseSink sink) {
        sinks.add(sink);
        try { sink.emit(SseEvent.create("{\"type\":\"connected\"}", MediaTypes.APPLICATION_JSON)); }
        catch (Exception ignore) {}
    }

    void remove(SseSink sink) { sinks.remove(sink); }

    /** Broadcast a JSON string to all subscribers */
    void broadcastJson(String json) {
        sinks.removeIf(s -> {
            try {
                s.emit(SseEvent.create(json, MediaTypes.APPLICATION_JSON));
                return false;
            } catch (Exception e) {
                return true; // drop broken sink
            }
        });
    }
}
