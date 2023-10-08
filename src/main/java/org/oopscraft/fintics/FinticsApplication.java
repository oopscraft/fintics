package org.oopscraft.fintics;

import org.oopscraft.arch4j.core.install.SpringBootInstaller;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.Arrays;


@SpringBootApplication(
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
public class FinticsApplication {

    public static void main(String[] args) {

        // install
        if(Arrays.asList(args).contains("install")) {
            SpringBootInstaller.install(FinticsApplication.class, args);
            System.exit(0);
        }

        // runs
        new SpringApplicationBuilder(FinticsApplication.class)
                .registerShutdownHook(true)
                .run(args);
    }

}
