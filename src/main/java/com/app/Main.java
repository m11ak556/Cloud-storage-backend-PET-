package com.app;

import com.app.configuration.FileSystemConfiguration;
import com.app.configuration.TrashbinInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableConfigurationProperties(FileSystemConfiguration.class)
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    // Создание объекта для настрокйи работы сервера
    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "PUT", "POST");
                    registry.addMapping("/delete")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "DELETE");
                registry.addMapping("/directory/delete")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "DELETE");
                registry.addMapping("/trashbin/destroy")
                        .allowedOrigins("*")
                        .allowedMethods("POST", "DELETE");
            }
        };
    }

    @Bean(initMethod = "initializeTrashbin")
    public TrashbinInitializer trashbinInitializer() {
        return new TrashbinInitializer();
    }
}
