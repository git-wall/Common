package org.app.common.module.ai.controller;

import org.app.common.module.ai.model.Answer;
import org.app.common.module.ai.model.Question;
import org.app.common.module.ai.service.AIChatService;
import org.app.common.module.ai.service.AiTaskSuggestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.util.Locale;

@RestController
public class AskController {

    private final AIChatService chatService;
    private final AiTaskSuggestionService taskSuggestionService;

    public AskController(AIChatService chatService,
                         AiTaskSuggestionService taskSuggestionService) {
        this.chatService = chatService;
        this.taskSuggestionService = taskSuggestionService;
    }

    @PostMapping(value = "/ask", produces = "application/json")
    public  Mono<ResponseEntity<Answer>> askQuestion(@RequestBody @Valid Question question, ServerWebExchange exchange) {

        String acceptLanguage = exchange.getRequest().getHeaders().getFirst("Accept-Language");
        Locale locale = (acceptLanguage != null && !acceptLanguage.isBlank())
            ? Locale.forLanguageTag(acceptLanguage)
            : Locale.ENGLISH;

        return chatService.askQuestion(question, locale)
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping(value = "/suggest-task/{category}", produces = "application/json")
    public Mono<ResponseEntity<?>> suggestTasks(@PathVariable String category) {

        return taskSuggestionService.suggestTasks(category)
            .map(suggestions -> {
                if (suggestions.isEmpty()) {
                    return ResponseEntity.noContent().build();
                }
                return ResponseEntity.ok(suggestions);
            })
            .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
