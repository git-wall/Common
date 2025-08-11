package org.app.common.module.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.ai.model.Answer;
import org.app.common.module.ai.model.Question;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Service
@Slf4j
public class AiChatServiceImpl implements AIChatService {

    private final ChatClient chatClient;

    public AiChatServiceImpl(ChatClient.Builder builder, ChatMemory memory) {
        this.chatClient = builder
            .defaultAdvisors(
                PromptChatMemoryAdvisor.builder(memory).build(),
                new SimpleLoggerAdvisor())
            .build();
    }

    @Override
    public Mono<Answer> askQuestion(Question question, Locale locale) {

        return chatClient.prompt()
            .user(question.question)
            .stream()
            .content()
            .doOnNext(chunk -> log.info("Received AI chunk: {}", chunk))
            .reduce((accumulated, newContent) -> accumulated + newContent)
            .map(aws -> new Answer(question.question, aws));
    }
}
