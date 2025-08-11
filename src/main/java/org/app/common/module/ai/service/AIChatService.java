package org.app.common.module.ai.service;

import org.app.common.module.ai.model.Answer;
import org.app.common.module.ai.model.Question;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface AIChatService {
    Mono<Answer> askQuestion(Question question, Locale locale);
}
