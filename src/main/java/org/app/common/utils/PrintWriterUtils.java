package org.app.common.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class PrintWriterUtils {

    public static final File STD_OUT = new File("");

    /**
     * Returns a print writer for the given file, or the standard output if
     * the file name is empty.
     */
    public static PrintWriter createPrintWriterOut(File outputFile)
            throws FileNotFoundException, UnsupportedEncodingException {
        return createPrintWriterOut(outputFile, false);
    }

    /**
     * Returns a print writer for the given file, or the standard output if
     * the file name is empty.
     */
    public static PrintWriter createPrintWriterOut(File outputFile, boolean append)
            throws FileNotFoundException, UnsupportedEncodingException {

        return createPrintWriter(outputFile, new PrintWriter(System.out, true), append);
    }


    /**
     * Returns a print writer for the given file, or the standard output if
     * the file name is empty.
     */
    public static PrintWriter createPrintWriterErr(File outputFile)
            throws FileNotFoundException, UnsupportedEncodingException {
        return createPrintWriter(outputFile, new PrintWriter(System.err, true));
    }


    /**
     * Returns a print writer for the given file, or the standard output if
     * the file name is empty.
     */
    public static PrintWriter createPrintWriter(File outputFile, PrintWriter console)
            throws FileNotFoundException, UnsupportedEncodingException {
        return createPrintWriter(outputFile, console, false);
    }


    /**
     * Returns a print writer for the given file, or the standard output if
     * the file name is empty.
     */
    public static PrintWriter createPrintWriter(File outputFile,
                                                PrintWriter console,
                                                boolean append)
            throws FileNotFoundException, UnsupportedEncodingException {
        return outputFile == STD_OUT ?
                console :
                new PrintWriter(
                        new BufferedWriter(
                                new OutputStreamWriter(
                                        new FileOutputStream(outputFile, append), StandardCharsets.UTF_8)));
    }

    /**
     * Closes the given print writer, or flushes it if is the standard output.
     */
    public static void closePrintWriter(File file, PrintWriter printWriter) {
        if (file == STD_OUT) {
            printWriter.flush();
        } else {
            printWriter.close();
        }
    }


    /**
     * Returns the canonical file name for the given file, or "standard output"
     * if the file name is empty.
     */
    public static String fileName(File file) {
        if (file == STD_OUT) {
            return "standard output";
        } else {
            try {
                return file.getCanonicalPath();
            } catch (IOException ex) {
                return file.getPath();
            }
        }
    }


    // Hide constructor for util class.
    private PrintWriterUtils() {
    }
}
