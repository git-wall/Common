package org.app.common.jooq;

import org.jooq.codegen.GenerationTool;
import org.jooq.meta.jaxb.*;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

@Component
public class JooqCodeGen {

    private final Environment environment;

    private static final String SKIP_GENERATION_PROPERTY = "jooq.generator.skip";

    private static final String OUTPUT_DIRECTORY = "../jooq/src/main/java";

    private static final String PACKAGE_NAME = "org.app.jooq.entities";

    public JooqCodeGen(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationStartedEvent.class)
    public void generateJooqClasses() {
        if (shouldSkipGeneration()) {
            System.out.println("Skipping JOOQ code generation due to property setting");
            return;
        }

        try {
            Configuration configuration = createConfiguration();
            prepareOutputDirectory(configuration.getGenerator().getTarget().getDirectory());
            GenerationTool.generate(configuration);
            System.out.println("JOOQ code generation completed successfully");
        } catch (Exception e) {
            System.err.println("Failed to generate JOOQ classes: " + e.getMessage());
        }
    }

    private boolean shouldSkipGeneration() {
        return Boolean.parseBoolean(environment.getProperty(SKIP_GENERATION_PROPERTY, "false"));
    }

    private Configuration createConfiguration() {
        return new Configuration()
                .withJdbc(createJdbcConfig())
                .withGenerator(createGeneratorConfig());
    }

    private Jdbc createJdbcConfig() {
        return new Jdbc()
                .withDriver(environment.getProperty("spring.datasource.driver-class-name", "org.postgresql.Driver"))
                .withUrl(environment.getProperty("spring.datasource.hikari.jdbc-url", "spring.datasource.url"))
                .withUser(environment.getProperty("spring.datasource.hikari.username", "spring.datasource.username"))
                .withPassword(environment.getProperty("spring.datasource.hikari.password", "spring.datasource.password"));
    }

    private Generator createGeneratorConfig() {
        return new Generator()
                .withDatabase(new Database()
                        .withName("org.jooq.meta.postgres.PostgresDatabase")
                        .withIncludes(".*")
                        .withExcludes(""))
                .withGenerate(new Generate()
                        .withPojos(true))
                .withTarget(new Target()
                        .withPackageName(getPackageName())
                        .withDirectory(getOutputDirectory()));
    }

    private String getOutputDirectory() {
        return environment.getProperty("jooq.generator.directory", OUTPUT_DIRECTORY);
    }

    private String getPackageName() {
        return environment.getProperty("jooq.generator.packageName", PACKAGE_NAME);
    }


    private void prepareOutputDirectory(String directoryPath) throws Exception {
        Path directory = Path.of(directoryPath);

        if (Files.exists(directory)) {
            // Delete existing directory content
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder()) // Reverse order to delete files before directories
                    .map(Path::toFile)
                    .forEach(File::delete);
        } else {
            // Create new directory
            Files.createDirectories(directory);
        }
    }
}
