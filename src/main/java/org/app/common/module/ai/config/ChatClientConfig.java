package org.app.common.module.ai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.*;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${spring.ai.openai.embedding.options.model:gpt-4o-mini}")
    private String openAiModel;

    @Bean
    ChatMemory chatMemory() {
        return null;
    }

    @Bean
    public ChatClient.Builder chatClientBuilder() {
        return ChatClient
            .builder(OpenAiChatModel.builder()
                .defaultOptions(OpenAiChatOptions.builder().model(openAiModel).build())
                .build());
    }

    @Bean
    public VectorStore vectorStore() {
        return SimpleVectorStore
            .builder(
                new OpenAiEmbeddingModel(OpenAiApi.builder().apiKey(openAiApiKey).build()))
            .build();
    }

    @Bean
    ChatClient chatClient(ChatClient.Builder builder, VectorStore vector, ChatMemory memory) {
        return builder
            .defaultAdvisors(
                PromptChatMemoryAdvisor.builder(memory).build(),
                MessageChatMemoryAdvisor.builder(memory).build(),
                VectorStoreChatMemoryAdvisor.builder(vector).build(),
                new SimpleLoggerAdvisor(),
                new QuestionAnswerAdvisor(vector))
            .build();
    }
}
