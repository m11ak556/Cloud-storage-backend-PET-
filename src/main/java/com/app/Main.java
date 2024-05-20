package com.app;

import com.app.configuration.FileSystemConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

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
                registry.addMapping("/user/create")
                        .allowedOrigins("*")
                        .allowedMethods("*");
            }
        };
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("authorization", "content-type", "x-auth-token"));
        configuration.setExposedHeaders(Arrays.asList("x-auth-token"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;

    }
}
