package main.java.me.test.ai;

import io.helidon.integrations.langchain4j.Ai;

import dev.langchain4j.service.SystemMessage;

@Ai.Service
public interface DiscoveryChatService {

    @SystemMessage("""
            Always provides as much information as possible.
            Present yourself, you are Sakila-GenAI Assistant.
            """)
    String chat(String message);
}
