package org.app.analyze.jol;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.vm.VM;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

/**
 * <table>
 *     <tr>
 *         <th>Role</th>
 *         <th>Use case</th>
 *     </tr>
 *     <tr>
 *         <td><b>Java Developer</b></td>
 *         <td>Analyze memory usage of objects and collections.</td>
 *     </tr>
 *     <tr>
 *         <td><b>Performance Engineer</b></td>
 *         <td>Optimize heap memory, GC tuning.</td>
 *     </tr>
 *     <tr>
 *         <td><b>Debugging Expert</b></td>
 *         <td>Detect memory leaks and analyze object bloat.</td>
 *     </tr>
 *     <tr>
 *         <td><b>Embedded Developer</b></td>
 *         <td>Optimize object sizes for low-memory devices.</td>
 *     </tr>
 *     <tr>
 *         <td><b>JVM Engineer</b></td>
 *         <td>Research & analyze JVM memory layout.</td>
 *     </tr>
 * </table>
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Analyze {
    /**
     * <b1><strong> Java Developer / Software Engineer
     * <pre>{@code
     * Understanding how objects consume memory can help optimize performance.
     *
     * Identifying unnecessary padding and alignment issues in objects.
     *
     * Checking if certain data structures are consuming excessive memory.
     * }
     * </pre>
     */
    public static void analyzeObj(Object object) {
        log.info(ClassLayout.parseInstance(object).toPrintable());
    }

    /**
     * <b1><strong> Performance Engineer / JVM Tuning Specialist
     * <pre>{@code
     * Heap size optimization – JOL helps determine the actual memory footprint of objects.
     *
     * Garbage Collection tuning – Understanding object sizes helps configure GC parameters better.
     *
     * Analyzing object alignment & padding – Helps minimize memory fragmentation.
     * }
     */
    public static void analyzeClass(Class<?> clazz) {
        log.info(ClassLayout.parseInstance(clazz).toPrintable());
    }

    /**
     * <b1><strong>Memory Leak Debugging & Profiling
     *
     * <pre>{@code
     * Finding memory leaks – JOL can be combined with profiling tools like VisualVM or YourKit.
     *
     * Analyzing memory bloat – Helps detect whether objects are larger than expected.
     * }
     */
    public static void analyzeVM() {
        log.info(VM.current().details());
    }

    /**
     * Put into class java have main then
     * java -cp /app PrintXmxXms
     */
    public static void analyzeMemory() {
        int mb = 1024 * 1024;
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long xmx = memoryBean.getHeapMemoryUsage().getMax() / mb;
        long xms = memoryBean.getHeapMemoryUsage().getInit() / mb;
        log.info("Initial Memory (xms): {}mb", xms);
        log.info("Max Memory (xmx): {}mb", xmx);
        log.info("Initial Memory (xms) : {}mb", xms);
        log.info("Max Memory (xmx) : {}mb", xmx);
    }
}
