package org.app.common.utils;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class StringUtils {
    public static final String FORMAT_IS = "%s:%s";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9][0-9]{7,14}$");

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }

    public static String slugify(String input) {
        if (input == null) {
            return "";
        }

        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String noAccents = pattern.matcher(normalized).replaceAll("");

        return noAccents.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }

    public static String truncate(String text, int maxLength, String suffix) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - suffix.length()) + suffix;
    }

    public static String extractInitials(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }

        String initials;
        String[] nameParts = fullName.split("\\s+");
        initials = Arrays.stream(nameParts)
                .filter(part -> !part.isEmpty())
                .map(part -> String.valueOf(Character.toUpperCase(part.charAt(0))))
                .collect(Collectors.joining());

        return initials;
    }

    // New utility methods
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        List<String> words = Arrays.asList(input.toLowerCase().split("\\s+"));
        return words.stream()
                .map(word -> word.isEmpty() ? word : Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public static String maskEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            return email;
        }
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];

        String maskedName = name.charAt(0) + "*".repeat(Math.max(name.length() - 2, 1)) + name.charAt(name.length() - 1);
        return maskedName + "@" + domain;
    }

    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 8) {
            return phoneNumber;
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && PHONE_PATTERN.matcher(phoneNumber).matches();
    }

    public static String removeSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static String padLeft(String input, int length, char padChar) {
        if (input == null) {
            return null;
        }
        return String.format("%" + length + "s", input).replace(' ', padChar);
    }

    public static String padRight(String input, int length, char padChar) {
        if (input == null) {
            return null;
        }
        return String.format("%-" + length + "s", input).replace(' ', padChar);
    }

    public static String reverseString(String input) {
        if (input == null) {
            return null;
        }
        return new StringBuilder(input).reverse().toString();
    }

    public static String extractNumbers(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^0-9]", "");
    }

    public static String extractLetters(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^a-zA-Z]", "");
    }

    public static String format(String format, Object... args) {
        return String.format(format, args);
    }
}