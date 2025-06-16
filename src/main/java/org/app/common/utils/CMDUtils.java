package org.app.common.utils;

public class CMDUtils {
    private CMDUtils() {
        // Utility class, prevent instantiation
    }

    /**
     * Executes a command in the system shell.
     *
     * @param command the command to execute
     * @return the output of the command
     */
    public static String executeCommand(String command) {
        StringBuilder output = new StringBuilder();
        try {
            Process process = Runtime.getRuntime().exec(command);
            java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            process.waitFor();
        } catch (Exception e) {
            MessageUtils.logWithTracing(e);
        }
        return output.toString();
    }
}
