package org.app.common.security.factor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorDTO {
    private String secret;
    private int code;
}
