package org.app.common.support;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Flatten {

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
                    STREAM
    \*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

    public static <T1, T2> List<T2> flattenToLevel2(List<T1> input, Function<T1, List<T2>> level2Getter) {
        return input.stream()
                .flatMap(t1 -> {
                    List<T2> l2 = level2Getter.apply(t1);
                    return l2 != null ? l2.stream() : Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public static <T1, T2> Stream<T2> flattenToLevel2Stream(List<T1> input, Function<T1, List<T2>> level2Getter) {
        return input.stream()
                .flatMap(t1 -> {
                    List<T2> l2 = level2Getter.apply(t1);
                    return l2 != null ? l2.stream() : Stream.empty();
                });
    }

    public static <T1, T2, T3> List<T3> flattenToLevel3(List<T1> input,
                                                        Function<T1, List<T2>> level2Getter,
                                                        Function<T2, List<T3>> level3Getter) {
        return flattenToLevel2Stream(input, level2Getter)
                .flatMap(t2 -> {
                    List<T3> l3 = level3Getter.apply(t2);
                    return l3 != null ? l3.stream() : Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public static <T1, T2, T3> Stream<T3> flattenToLevel3Stream(List<T1> input,
                                                                Function<T1, List<T2>> level2Getter,
                                                                Function<T2, List<T3>> level3Getter) {
        return flattenToLevel2Stream(input, level2Getter)
                .flatMap(t2 -> {
                    List<T3> l3 = level3Getter.apply(t2);
                    return l3 != null ? l3.stream() : Stream.empty();
                });
    }

    public static <T1, T2, T3, T4> List<T4> flattenToLevel4(List<T1> input,
                                                            Function<T1, List<T2>> level2Getter,
                                                            Function<T2, List<T3>> level3Getter,
                                                            Function<T3, List<T4>> level4Getter) {
        return flattenToLevel3Stream(input, level2Getter, level3Getter)
                .flatMap(t3 -> {
                    List<T4> l4 = level4Getter.apply(t3);
                    return l4 != null ? l4.stream() : Stream.empty();
                })
                .collect(Collectors.toList());
    }

    public static <T1, T2, T3, T4> Stream<T4> flattenToLevel4Stream(List<T1> input,
                                                                    Function<T1, List<T2>> level2Getter,
                                                                    Function<T2, List<T3>> level3Getter,
                                                                    Function<T3, List<T4>> level4Getter) {
        return flattenToLevel3Stream(input, level2Getter, level3Getter)
                .flatMap(t3 -> {
                    List<T4> l4 = level4Getter.apply(t3);
                    return l4 != null ? l4.stream() : Stream.empty();
                });
    }

    /*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*\
                 SAVE MEMORY
    \*-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-*/

    public static <T1, T2> List<T2> flatTo2LV(List<T1> input, Function<T1, List<T2>> level2Getter) {
        List<T2> result = new ArrayList<>();
        for (T1 l1 : input) {
            List<T2> level2 = level2Getter.apply(l1);
            if (level2 == null) continue;
            result.addAll(level2);
        }
        return result;
    }

    public static <T1, T2, T3> List<T3> flatTo3LV(List<T1> input,
                                                  Function<T1, List<T2>> level2Getter,
                                                  Function<T2, List<T3>> level3Getter) {
        List<T3> result = new ArrayList<>();
        for (T1 l1 : input) {
            List<T2> level2 = level2Getter.apply(l1);
            if (level2 == null) continue;

            for (T2 l2 : level2) {
                List<T3> level3 = level3Getter.apply(l2);
                if (level3 == null) continue;
                result.addAll(level3);
            }
        }
        return result;
    }

    public static <T1, T2, T3, T4> List<T4> flatTo4LV(List<T1> input,
                                                      Function<T1, List<T2>> level2Getter,
                                                      Function<T2, List<T3>> level3Getter,
                                                      Function<T3, List<T4>> level4Getter) {
        List<T4> result = new ArrayList<>();
        for (T1 l1 : input) {
            List<T2> level2 = level2Getter.apply(l1);
            if (level2 == null) continue;

            for (T2 l2 : level2) {
                List<T3> level3 = level3Getter.apply(l2);
                if (level3 == null) continue;

                for (T3 l3 : level3) {
                    List<T4> level4 = level4Getter.apply(l3);
                    if (level4 != null) {
                        result.addAll(level4);
                    }
                }
            }
        }
        return result;
    }


}
