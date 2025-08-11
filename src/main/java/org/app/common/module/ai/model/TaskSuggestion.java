package org.app.common.module.ai.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskSuggestion {
    String title;
    String description;
    LocalDateTime startDate;
    LocalDateTime endDate;
    int priority;
    String assignedTo;
    String approvalUser;
}
