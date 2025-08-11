package org.app.common.beam.io.file;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptor;
import org.app.common.beam.io.AbstractBeamIO;
import org.app.common.beam.io.IOOptions;

import java.util.function.Function;

/**
 * Implementation of BeamIO for file operations.
 * This class provides functionality to read from and write to files using Apache Beam.
 *
 * @param <T> The type of elements in the PCollection
 */
public class FileIO<T> extends AbstractBeamIO<T> {
    
    private static final String FILE_PATH = "filePath";
    private static final String PARSE_FN = "parseFn";
    private static final String FORMAT_FN = "formatFn";
    
    /**
     * Constructor with options.
     *
     * @param options Configuration options for the file IO operation
     */
    public FileIO(IOOptions options) {
        super(options);
    }
    
    /**
     * Create a new FileIO instance with the specified file path.
     *
     * @param filePath The path to the file
     * @param parseFn  Function to parse text lines to type T
     * @param <T>      The type of elements in the PCollection
     * @return A new FileIO instance
     */
    public static <T> FileIO<T> of(String filePath, Function<String, T> parseFn) {
        IOOptions options = new IOOptions()
                .set(FILE_PATH, filePath)
                .set(PARSE_FN, parseFn);
        return new FileIO<>(options);
    }
    
    /**
     * Create a new FileIO instance with the specified file path and format function.
     *
     * @param filePath The path to the file
     * @param parseFn  Function to parse text lines to type T
     * @param formatFn Function to format type T to text lines
     * @param <T>      The type of elements in the PCollection
     * @return A new FileIO instance
     */
    public static <T> FileIO<T> of(String filePath, Function<String, T> parseFn, Function<T, String> formatFn) {
        IOOptions options = new IOOptions()
                .set(FILE_PATH, filePath)
                .set(PARSE_FN, parseFn)
                .set(FORMAT_FN, formatFn);
        return new FileIO<>(options);
    }
    
    @Override
    public PCollection<T> read(Pipeline pipeline) {
        if (!validateOptions()) {
            throw new IllegalStateException("Invalid options for FileIO");
        }
        
        String filePath = options.get(FILE_PATH);
        Function<String, T> parseFn = options.get(PARSE_FN);
        
        return pipeline
                .apply("ReadFromFile", TextIO.read().from(filePath))
                .apply("ParseLines", MapElements
                        .into(TypeDescriptor.of(getTypeClass()))
                        .via(parseFn::apply));
    }
    
    @Override
    public void write(PCollection<T> input) {
        if (!validateOptions()) {
            throw new IllegalStateException("Invalid options for FileIO");
        }
        
        String filePath = options.get(FILE_PATH);
        Function<T, String> formatFn = options.get(FORMAT_FN);
        
        if (formatFn == null) {
            throw new IllegalStateException("Format function is required for writing to file");
        }
        
        input
                .apply("FormatElements", MapElements
                        .into(TypeDescriptor.of(String.class))
                        .via(formatFn::apply))
                .apply("WriteToFile", TextIO.write().to(filePath));
    }
    
    @Override
    protected boolean validateOptions() {
        return super.validateOptions() && 
                options.has(FILE_PATH) && 
                options.has(PARSE_FN);
    }
    
    /**
     * Get the class of type T.
     * This is a workaround for Java type erasure.
     *
     * @return The class of type T
     */
    @SuppressWarnings("unchecked")
    private Class<T> getTypeClass() {
        // This is a simplification. In a real implementation, you would need to
        // pass the class explicitly or use other techniques to handle type erasure.
        return (Class<T>) Object.class;
    }
}