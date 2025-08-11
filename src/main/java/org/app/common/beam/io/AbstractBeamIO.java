package org.app.common.beam.io;

import org.apache.beam.sdk.Pipeline;

/**
 * Abstract base class for Apache Beam IO operations.
 * This class provides common functionality for reading from and writing to different data sources.
 *
 * @param <T> The type of elements in the PCollection
 */
public abstract class AbstractBeamIO<T> implements BeamIO<T> {
    
    /**
     * Configuration options for the IO operation.
     */
    protected final IOOptions options;
    
    /**
     * Constructor with options.
     *
     * @param options Configuration options for the IO operation
     */
    protected AbstractBeamIO(IOOptions options) {
        this.options = options;
    }
    
    /**
     * Default constructor with empty options.
     */
    protected AbstractBeamIO() {
        this(new IOOptions());
    }
    
    /**
     * Validates the configuration options before performing IO operations.
     *
     * @return true if the options are valid, false otherwise
     */
    protected boolean validateOptions() {
        return options != null;
    }
    
    /**
     * Prepares the pipeline for IO operations.
     * This method can be overridden by subclasses to perform additional setup.
     *
     * @param pipeline The Beam pipeline to prepare
     */
    protected void preparePipeline(Pipeline pipeline) {
        // Default implementation does nothing
    }
    
    /**
     * Finalizes the pipeline after IO operations.
     * This method can be overridden by subclasses to perform additional cleanup.
     *
     * @param pipeline The Beam pipeline to finalize
     */
    protected void finalizePipeline(Pipeline pipeline) {
        // Default implementation does nothing
    }
}