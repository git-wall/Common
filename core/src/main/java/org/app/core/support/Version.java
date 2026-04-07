package org.app.core.support;

import lombok.NoArgsConstructor;

/**
 * Utility class for determining the Java runtime version.
 * <p>
 * This class provides static methods to check the current Java version
 * and compare it against specific versions such as Java 8, 11, 17, and 21.
 * </p>
 * <p>
 * The class is designed to be non-instantiable and provides static utility methods.
 * </p>
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Version {

    /**
     * The Java specification version retrieved from the system property "java.specification.version".
     */
    private static final String VER = System.getProperty("java.specification.version");

    /**
     * Checks if the current Java version matches the specified version.
     *
     * @param version the Java version to compare against (e.g., "11", "17")
     * @return {@code true} if the current Java version matches the specified version, {@code false} otherwise
     */
    public static boolean isJava(String version) {
        return VER.equals(version);
    }

    /**
     * Checks if the current Java version is Java 8.
     *
     * @return {@code true} if the current Java version starts with "1.", indicating Java 8, {@code false} otherwise
     */
    public static boolean isJava8() {
        return VER.startsWith("1.");
    }

    /**
     * Checks if the current Java version is Java 11.
     *
     * @return {@code true} if the current Java version is "11", {@code false} otherwise
     */
    public static boolean isJava11() {
        return VER.equals("11");
    }

    /**
     * Checks if the current Java version is Java 17.
     *
     * @return {@code true} if the current Java version is "17", {@code false} otherwise
     */
    public static boolean isJava17() {
        return VER.equals("17");
    }

    /**
     * Checks if the current Java version is Java 21.
     *
     * @return {@code true} if the current Java version is "21", {@code false} otherwise
     */
    public static boolean isJava21() {
        return VER.equals("21");
    }
}
