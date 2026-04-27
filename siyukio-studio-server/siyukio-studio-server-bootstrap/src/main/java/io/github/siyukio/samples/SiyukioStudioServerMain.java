package io.github.siyukio.samples;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SiyukioStudioServerMain {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SiyukioStudioServerMain.class)
                .build()
                .run(args);
    }
}
