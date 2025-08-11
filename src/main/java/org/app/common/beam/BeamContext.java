package org.app.common.beam;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;

public class BeamContext {
    public static Pipeline createPipeline() {
        PipelineOptions options = PipelineOptionsFactory.create();
        return Pipeline.create(options);
    }

    public static Pipeline createPipeline(String... beamArgs) {
        PipelineOptions options = PipelineOptionsFactory.fromArgs(beamArgs).create();
        return Pipeline.create(options);
    }
}
