package com.tmansonetto.proposta_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry regisry){
        regisry.addMapping("/**").allowedOrigins("http://localhost", "http://localhost:4200").allowedMethods("*").allowCredentials(true);
    }
}
