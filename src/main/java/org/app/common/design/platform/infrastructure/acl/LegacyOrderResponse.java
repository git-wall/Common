package org.app.common.design.platform.infrastructure.acl;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegacyOrderResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private LegacyOrder data;
}
