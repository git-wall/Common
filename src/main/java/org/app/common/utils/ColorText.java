package org.app.common.utils;

public class ColorText {
    // Reset
    public static final String RESET = "\u001B[0m";

    // Foreground (text) colors
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    // Background colors
    public static final String BG_BLACK = "\u001B[40m";
    public static final String BG_RED = "\u001B[41m";
    public static final String BG_GREEN = "\u001B[42m";
    public static final String BG_YELLOW = "\u001B[43m";
    public static final String BG_BLUE = "\u001B[44m";
    public static final String BG_PURPLE = "\u001B[45m";
    public static final String BG_CYAN = "\u001B[46m";
    public static final String BG_WHITE = "\u001B[47m";
    public static final String BG_ORANGE = "\u001B[48;5;208m";
    public static final String BG_GRAY = "\u001B[47;1m";

    // === Helper methods ===
    public static String color(String text, String colorCode) {
        return colorCode + text + RESET;
    }

    public static String bgRed(String text) { return BG_RED + text + RESET; }
    public static String bgGreen(String text) { return BG_GREEN + text + RESET; }
    public static String bgBlue(String text) { return BG_BLUE + text + RESET; }
    public static String bgYellow(String text) { return BG_YELLOW + text + RESET; }
    public static String bgBlack(String text) { return BG_BLACK + text + RESET; }
    public static String bgWhite(String text) { return BG_WHITE + text + RESET; }
    public static String bgPurple(String text) { return BG_PURPLE + text + RESET; }
    public static String bgCyan(String text) { return BG_CYAN + text + RESET; }
    public static String bgOrange(String text) { return BG_ORANGE + text + RESET; }
    public static String bgGray(String text) { return BG_GRAY + text + RESET; }

    public static String red(String text) { return RED + text + RESET; }
    public static String green(String text) { return GREEN + text + RESET; }
    public static String blue(String text) { return BLUE + text + RESET; }
    public static String yellow(String text) { return YELLOW + text + RESET; }
    public static String black(String text) { return BLACK + text + RESET; }
    public static String white(String text) { return WHITE + text + RESET; }
    public static String purple(String text) { return PURPLE + text + RESET; }
    public static String cyan(String text) { return CYAN + text + RESET; }
}
