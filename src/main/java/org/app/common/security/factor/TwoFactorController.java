package org.app.common.security.factor;

import lombok.RequiredArgsConstructor;
import org.app.common.constant.TagURL;
import org.app.common.context.AuthContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(TagURL.API + TagURL.TWO_FACTOR_AUTH)
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorAuthService twoFactorAuthService;

    @PostMapping(TagURL.REGISTER)
    public ResponseEntity<Object> register() {
        // Generate a TOTP secret key
        String secret = twoFactorAuthService.generateKey();

        // Generate QR code URL
        String qrCodeUrl = twoFactorAuthService.generateQRUrlUserName(secret, AuthContext.getUserName());

        // Return the secret and QR code URL to the client
        Map<String, String> response = new HashMap<>(2);
        response.put("secret", secret);
        response.put("qrCodeUrl", qrCodeUrl);

        return ResponseEntity.ok(response);
    }

    @PostMapping(TagURL.LOGIN)
    public ResponseEntity<Object> login(@RequestBody TwoFactorDTO request) {
        // Validate the TOTP code
        boolean isValid = twoFactorAuthService.isValid(request.getSecret(), request.getCode());

        if (isValid) {
            return ResponseEntity.ok("Login 2FA successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid TOTP code");
        }
    }
}
