package org.app.common.module.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.app.common.module.ai.TaskConstants;
import org.app.common.module.ai.model.TaskSuggestion;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Service
@Slf4j
public class SpringAiTaskSuggestionService implements AiTaskSuggestionService {

    @Value("classpath:/promptTemplates/meetingNotesRandomPromptTemplate_vi.st")
    Resource randomMeetingNoteTemplateVi;

    @Value("classpath:/promptTemplates/tasksSuggestionPromptTemplate_vi.st")
    Resource tasksSuggestionPromptTemplateVi;

    private final ChatClient chatClient;

    public SpringAiTaskSuggestionService(ChatClient.Builder chatClientBuilder,
                                         ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
            .defaultAdvisors(
                PromptChatMemoryAdvisor.builder(chatMemory).build(),
                new SimpleLoggerAdvisor())
            .build();
    }

    @Override
    public Mono<List<TaskSuggestion>> suggestTasks(String category) {
        return chatClient.prompt()
            .user(promptUserSpec -> promptUserSpec
                .text(randomMeetingNoteTemplateVi)
                .param(TaskConstants.CATEGORY, category))
            .stream()
            .content()
            .reduce((accumulated, newContent) -> accumulated + newContent)
            .timeout(Duration.ofSeconds(120))
            .flatMap(content -> {
                log.info("Received content: {}", content);

                var responseData = chatClient.prompt()
                    .user(promptUserSpec -> promptUserSpec
                        .text(tasksSuggestionPromptTemplateVi)
                        .param(TaskConstants.MEETING_NOTE, content))
                    .call()
                    .responseEntity(new ParameterizedTypeReference<List<TaskSuggestion>>() {});

                ChatResponse response = responseData.response();

                assert response != null;
                ChatResponseMetadata metadata = response.getMetadata();

                logUsage(metadata.getUsage());

                return Mono.justOrEmpty(
                    responseData.entity()
                );

            });

    }

    private void logUsage(Usage usage) {
        log.info("Token usage: prompt={}, generation={}, total={}",
            usage.getPromptTokens(),
            usage.getCompletionTokens(),
            usage.getTotalTokens());
    }
}
