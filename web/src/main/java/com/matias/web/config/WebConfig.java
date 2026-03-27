package com.matias.web.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración global de rutas para controladores REST.
 * Aplica el prefijo /api a todos los controladores anotados con @RestController,
 * excepto aquellos de paquetes específicos como springdoc.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix("/api",
            c -> c.isAnnotationPresent(RestController.class)
                && !c.getPackageName().startsWith("org.springdoc"));
    }
}
