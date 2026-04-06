package org.app.security.security.factor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TwoFactorDTO {
    private String secret;
    private int code;
}
