package org.app.common.utils;


/**
 * <pre>{@code
 * String maskedCard = DataMaskingUtils.maskCard("1234567890123456", 4);
 * // Output: ************3456
 * }
 * </pre>
 * */
public class DataMaskingUtils {
    public static String maskCard(String creditCard, int visibleDigits) {
        if (creditCard == null || creditCard.length() < visibleDigits) {
            return creditCard;
        }
        String maskedPart = "*".repeat(creditCard.length() - visibleDigits);
        return maskedPart + creditCard.substring(creditCard.length() - visibleDigits);
    }
}
