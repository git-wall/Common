package org.app.common.module.ai.service;

import org.app.common.module.ai.model.TaskSuggestion;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AiTaskSuggestionService {

    Mono<List<TaskSuggestion>> suggestTasks(String category);
}
