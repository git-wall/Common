package org.app.common.algorithms;

import org.springframework.lang.NonNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LevenshteinSearch {

    public static final int MAX_DISTANCE = 2;

    public static List<String> search(String query, List<String> labels) {
        return search(query, labels, String::toLowerCase);
    }

    // Search for labels with a maximum distance threshold
    private static <T> List<T> search(String query, List<T> elements, Function<T, String> function) {
        String qr = query.toLowerCase(); // Case-insensitive search

        return elements.stream()
                .filter(e -> {
                    String label = function.apply(e);
                    int distance = calculateDistance(qr, label);
                    return distance < LevenshteinSearch.MAX_DISTANCE;
                })
                .collect(Collectors.toList());
    }

    private static int calculateDistance(@NonNull String s1, @NonNull String s2) {
        int l1 = s1.length();
        int l2 = s2.length();
        int[][] dp = new int[l1 + 1][l2 + 1];

        for (int i = 0; i <= l1; i++) {
            for (int j = 0; j <= l2; j++) {
                if (i == 0) {
                    dp[i][j] = j; // Empty s1, insert all chars of s2
                } else if (j == 0) {
                    dp[i][j] = i; // Empty s2, insert all chars of s1
                } else {
                    int substitutionCost = ((int) s1.charAt(i - 1) == (int) s2.charAt(j - 1)) ? 0 : 1;
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1,          // Deletion
                                    dp[i][j - 1] + 1),          // Insertion
                            dp[i - 1][j - 1] + substitutionCost // Substitution
                    );
                }
            }
        }
        return dp[l1][l2];
    }
}
