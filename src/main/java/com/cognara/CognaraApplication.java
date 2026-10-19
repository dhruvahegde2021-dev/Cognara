package com.cognara;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Cognara backend application.
 *
 * <p>@SpringBootApplication is a convenience annotation that combines:
 * <ul>
 *   <li>@Configuration – marks this class as a source of Spring bean definitions</li>
 *   <li>@EnableAutoConfiguration – tells Spring Boot to auto-configure the application
 *       based on the dependencies on the classpath (e.g. sees PostgreSQL driver →
 *       configures a DataSource)</li>
 *   <li>@ComponentScan – tells Spring to scan this package and its sub-packages for
 *       components (@RestController, @Service, @Repository, etc.)</li>
 * </ul>
 */
@SpringBootApplication
public class CognaraApplication {

    public static void main(String[] args) {

        SpringApplication.run(CognaraApplication.class, args);
    }
}
