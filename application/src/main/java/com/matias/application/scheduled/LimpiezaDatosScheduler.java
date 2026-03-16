package com.matias.application.scheduled;

import com.matias.application.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LimpiezaDatosScheduler {

    private final AuthService authService;

    /**
     * Limpia datos obsoletos todos los días a las 2 AM.
     * Elimina tokens expirados, intentos de verificación antiguos y otros datos temporales.
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void limpiarDatosObsoletos() {
        log.info("Iniciando tarea programada de limpieza de datos obsoletos");
        
        try {
            authService.limpiarDatosObsoletos();
            log.info("Tarea programada de limpieza completada exitosamente");
        } catch (Exception e) {
            log.error("Error durante la tarea programada de limpieza: {}", e.getMessage(), e);
        }
    }
}
