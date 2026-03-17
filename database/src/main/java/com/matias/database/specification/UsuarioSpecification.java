package com.matias.database.specification;

import com.matias.database.entity.UsuarioEntity;
import com.matias.database.entity.UsuarioRolEntity;
import com.matias.domain.model.Rol;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Clase de utilidad para construir especificaciones JPA dinámicas para consultas de UsuarioEntity.
 */
public class UsuarioSpecification {

    private UsuarioSpecification() {
        // Clase de utilidad, no debe instanciarse
    }

    /**
     * Crea una especificación compuesta a partir de múltiples filtros opcionales.
     */
    public static Specification<UsuarioEntity> withFilters(
            String search,
            Boolean activo,
            Boolean emailVerificado,
            Set<Rol> roles,
            Instant fechaDesde,
            Instant fechaHasta
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Búsqueda por email o nombre completo
            if (search != null && !search.isBlank()) {
                String searchPattern = "%" + search.toLowerCase() + "%";
                Predicate emailLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        searchPattern
                );
                Predicate nombreLike = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nombreCompleto")),
                        searchPattern
                );
                predicates.add(criteriaBuilder.or(emailLike, nombreLike));
            }

            // Filtro por estado activo
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            // Filtro por email verificado
            if (emailVerificado != null) {
                predicates.add(criteriaBuilder.equal(root.get("emailVerificado"), emailVerificado));
            }

            // Filtro por roles (OR - al menos uno de los roles)
            if (roles != null && !roles.isEmpty()) {
                Join<UsuarioEntity, UsuarioRolEntity> rolesJoin = root.join("usuarioRoles", JoinType.LEFT);
                predicates.add(rolesJoin.get("rol").in(roles));
            }

            // Filtro por fecha de creación desde
            if (fechaDesde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("fechaCreacion"),
                        fechaDesde
                ));
            }

            // Filtro por fecha de creación hasta
            if (fechaHasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("fechaCreacion"),
                        fechaHasta
                ));
            }

            // Evitar duplicados cuando se filtra por roles
            if (roles != null && !roles.isEmpty()) {
                query.distinct(true);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
