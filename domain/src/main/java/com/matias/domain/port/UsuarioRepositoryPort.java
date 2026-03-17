package com.matias.domain.port;

import com.matias.domain.model.Rol;
import com.matias.domain.model.Usuario;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface UsuarioRepositoryPort {
    Optional<Usuario> findById(Integer id);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByEmailWithRoles(String email);
    Usuario save(Usuario usuario);
    void updateStatus(Integer id, boolean active);
    void assignRole(Integer userId, Rol rol);
    void unassignRole(Integer userId, Rol rol);
    boolean existsByUsuarioIdAndRol(Integer userId, Rol rol);
    long count();
    long countByActivo(boolean activo);
    long countByEmailVerificado(boolean verificado);
    long countByFechaCreacionAfter(Instant date);
    
    // Métodos para administración
    Map<Rol, Long> countUsuariosPorRol();
    
    // Objeto para encapsular la respuesta paginada
    record PageResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean isFirst,
        boolean isLast
    ) {}
    
    // Objeto para encapsular filtros de búsqueda
    record UsuarioFilter(
        String search,
        Boolean activo,
        Boolean emailVerificado,
        Set<Rol> roles,
        Instant fechaDesde,
        Instant fechaHasta
    ) {}
    
    // Objeto para encapsular opciones de paginación y ordenamiento
    record PageRequest(
        int page,
        int size,
        String sortBy,
        SortDirection direction
    ) {}
    
    enum SortDirection {
        ASC, DESC
    }
    
    PageResult<Usuario> findAllWithFilters(UsuarioFilter filter, PageRequest pageRequest);
}
