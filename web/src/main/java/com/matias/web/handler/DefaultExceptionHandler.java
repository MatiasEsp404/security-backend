package com.matias.web.handler;

import com.matias.application.dto.response.ErrorResponse;
import com.matias.domain.exception.AccesoDenegadoException;
import com.matias.domain.exception.ConflictoException;
import com.matias.domain.exception.NoAutenticadoException;
import com.matias.domain.exception.OperacionNoPermitidaException;
import com.matias.domain.exception.RecursoNoEncontradoException;
import com.matias.domain.exception.ServicioExternoException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@ControllerAdvice
public class DefaultExceptionHandler {

    /**
     * Maneja errores de validación de datos de entrada (Bean Validation).
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        log.warn("Validación fallida: {}", e.getMessage());

        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();

        ErrorResponse response = new ErrorResponse("Datos de entrada no válidos", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Excepciones genéricas - 400 Bad Request
     */
    @ExceptionHandler(OperacionNoPermitidaException.class)
    public ResponseEntity<ErrorResponse> handleOperacionNoPermitida(OperacionNoPermitidaException e) {
        log.warn("Operación no permitida: {}", e.getMessage());
        return buildResponse("Operación no permitida", e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    /**
     * Excepciones genéricas - 401 Unauthorized
     */
    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<ErrorResponse> handleNoAutenticado(NoAutenticadoException e) {
        log.warn("Usuario no autenticado: {}", e.getMessage());
        return buildResponse("No autenticado", e.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Excepciones genéricas - 403 Forbidden
     */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorResponse> handleAccesoDenegado(AccesoDenegadoException e) {
        log.warn("Acceso denegado: {}", e.getMessage());
        return buildResponse("Acceso denegado", e.getMessage(), HttpStatus.FORBIDDEN);
    }

    /**
     * Excepciones genéricas - 404 Not Found
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(RecursoNoEncontradoException e) {
        log.warn("Recurso no encontrado: {}", e.getMessage());
        return buildResponse("Recurso no encontrado", e.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Excepciones genéricas - 409 Conflict
     */
    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<ErrorResponse> handleConflicto(ConflictoException e) {
        log.warn("Conflicto: {}", e.getMessage());
        return buildResponse("Conflicto", e.getMessage(), HttpStatus.CONFLICT);
    }

    /**
     * Errores de servicios externos (email, APIs externas, etc.)
     * 
     * Nota: Los errores de JWT y autenticación son manejados por el 
     * AuthenticationEntryPoint y AccessDeniedHandler configurados en SecurityConfig.
     */
    @ExceptionHandler(ServicioExternoException.class)
    public ResponseEntity<ErrorResponse> handleServicioExterno(ServicioExternoException e) {
        log.error("Error en servicio externo: {}", e.getMessage(), e);
        return buildResponse(
                "Servicio temporalmente no disponible",
                "No se pudo completar la operación. Por favor, intente nuevamente más tarde.",
                HttpStatus.SERVICE_UNAVAILABLE
        );
    }

    /**
     * Errores internos del servidor.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleInternalErrors(Exception e) {
        log.error("Error interno del servidor", e);
        return buildResponse(
                "Error interno del servidor",
                "Ocurrió un error inesperado. Por favor, contacte al soporte.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    /**
     * Cuando se intenta acceder a un endpoint inexistente.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(NoResourceFoundException e) {
        log.info("Endpoint inexistente: {}", e.getMessage());
        return buildResponse(
                "Recurso no encontrado",
                "El endpoint solicitado no existe.",
                HttpStatus.NOT_FOUND
        );
    }

    /**
     * Cuando se invoca un endpoint existente con un verbo HTTP no soportado.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.info("Método HTTP no soportado: {}", e.getMethod());
        return buildResponse(
                "Método no permitido",
                "El método HTTP utilizado no está permitido para este endpoint.",
                HttpStatus.METHOD_NOT_ALLOWED
        );
    }

    /**
     * Maneja errores de conversión de tipos en path/query params.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("Parámetro inválido '{}': {}", e.getName(), e.getValue());

        String detalle = String.format("El valor '%s' no es válido para el parámetro '%s'",
                e.getValue(), e.getName());

        return buildResponse("Parámetro inválido", detalle, HttpStatus.BAD_REQUEST);
    }

    /**
     * Fallback para cualquier excepción no manejada explícitamente.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception e) {
        log.error("Excepción no controlada: {}", e.getClass().getSimpleName(), e);
        return buildResponse(
                "Error inesperado",
                "Ocurrió un error inesperado. Por favor, intente nuevamente.",
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    private ResponseEntity<ErrorResponse> buildResponse(String title, String detail, HttpStatus status) {
        ErrorResponse error = new ErrorResponse(title, List.of(detail));
        return ResponseEntity.status(status).body(error);
    }
}
