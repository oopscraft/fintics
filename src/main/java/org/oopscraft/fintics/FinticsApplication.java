package org.oopscraft.fintics;

import org.oopscraft.arch4j.core.support.SpringApplicationInstaller;
import org.springframework.boot.WebApplicationType;
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
            SpringApplicationInstaller.install(FinticsApplication.class, args);
            System.exit(0);
        }

        // runs
        new SpringApplicationBuilder(FinticsApplication.class)
                .web(WebApplicationType.SERVLET)
                .registerShutdownHook(true)
                .run(args);
    }

}
