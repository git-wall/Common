package org.app.common.beam.io;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.values.PCollection;

/**
 * Generic interface for Apache Beam IO operations.
 * This interface defines the contract for reading from and writing to different data sources.
 *
 * @param <T> The type of elements in the PCollection
 */
public interface BeamIO<T> {
    
    /**
     * Reads data from a source and returns a PCollection.
     *
     * @param pipeline The Beam pipeline to use
     * @return A PCollection containing the data read from the source
     */
    PCollection<T> read(Pipeline pipeline);
    
    /**
     * Writes a PCollection to a sink.
     *
     * @param input The PCollection to write
     */
    void write(PCollection<T> input);
}