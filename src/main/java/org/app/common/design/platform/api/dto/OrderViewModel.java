package org.app.common.design.platform.api.dto;

import lombok.Builder;

@Builder
public class OrderViewModel {
    private String displayId;
    private String statusText;
    private String statusColor;
    private String statusIcon;
    private String totalFormatted;
    private String dateFormatted;
    private String relativeTime;
    private int itemCount;
    private boolean canCancel;
    private boolean canReturn;
    private int progressPercentage;
}
