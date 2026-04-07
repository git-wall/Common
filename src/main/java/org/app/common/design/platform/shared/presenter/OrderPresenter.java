package org.app.common.design.platform.shared.presenter;

import lombok.RequiredArgsConstructor;
import org.app.common.design.platform.api.dto.OrderViewModel;
import org.app.common.design.platform.domain.model.order.Order;
import org.app.common.design.platform.domain.model.order.OrderStatus;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OrderPresenter {

    private final MessageSource messageSource;

    public OrderViewModel present(Order order, Locale locale) {

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDateTime(
            FormatStyle.MEDIUM
        ).withLocale(locale);

        return OrderViewModel.builder()
            .displayId("#" + order.getId().substring(0, 8).toUpperCase())
            .statusText(translateStatus(order.getStatus(), locale))
            .statusColor(getStatusColor(order.getStatus()))
            .statusIcon(getStatusIcon(order.getStatus()))
            .totalFormatted(currencyFormat.format(order.getTotal()))
            .dateFormatted(order.getCreatedAt().format(dateFormat))
            .relativeTime(formatRelativeTime(order.getCreatedAt(), locale))
            .itemCount(order.getItems().size())
            .canCancel(order.canCancel())
            .canReturn(order.canReturn())
            .progressPercentage(calculateProgress(order.getStatus()))
            .build();
    }

    private String translateStatus(OrderStatus status, Locale locale) {
        String key = "order.status." + status.name().toLowerCase();
        return messageSource.getMessage(key, null, status.name(), locale);
    }

    private String getStatusColor(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "#FFA500";
            case CONFIRMED:
                return "#007BFF";
            case SHIPPED:
                return "#6F42C1";
            case DELIVERED:
                return "#28A745";
            case CANCELLED:
                return "#DC3545";
            default:
                return "#6C757D";
        }
    }

    private String getStatusIcon(OrderStatus status) {
        switch (status) {
            case PENDING:
                return "⏳";
            case CONFIRMED:
                return "✓";
            case SHIPPED:
                return "🚚";
            case DELIVERED:
                return "📦";
            case CANCELLED:
                return "❌";
            default:
                return "❓";
        }
    }

    private String formatRelativeTime(LocalDateTime dateTime, Locale locale) {
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long hours = duration.toHours();

        if (hours < 1) {
            return messageSource.getMessage("time.just.now", null, locale);
        } else if (hours < 24) {
            return messageSource.getMessage("time.hours.ago",
                new Object[]{hours}, locale);
        } else {
            long days = duration.toDays();
            return messageSource.getMessage("time.days.ago",
                new Object[]{days}, locale);
        }
    }

    private int calculateProgress(OrderStatus status) {
        switch (status) {
            case PENDING:
                return 25;
            case CONFIRMED:
                return 50;
            case SHIPPED:
                return 75;
            case DELIVERED:
                return 100;
            case CANCELLED:
                return 0;
            default:
                return 0;
        }
    }
}
