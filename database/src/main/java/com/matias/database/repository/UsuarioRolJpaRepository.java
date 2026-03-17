package com.matias.database.repository;

import com.matias.database.entity.UsuarioRolEntity;
import com.matias.domain.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRolJpaRepository extends JpaRepository<UsuarioRolEntity, Integer> {

    /**
     * Busca todos los roles asignados a un usuario específico.
     *
     * @param usuarioId ID del usuario
     * @return Lista de roles asignados
     */
    List<UsuarioRolEntity> findByUsuarioId(Integer usuarioId);

    /**
     * Busca una asignación específica de rol para un usuario.
     *
     * @param usuarioId ID del usuario
     * @param rol Rol a buscar
     * @return Optional con la asignación si existe
     */
    Optional<UsuarioRolEntity> findByUsuarioIdAndRol(Integer usuarioId, Rol rol);

    /**
     * Verifica si un usuario tiene un rol específico asignado.
     *
     * @param usuarioId ID del usuario
     * @param rol Rol a verificar
     * @return true si el usuario tiene el rol asignado
     */
    boolean existsByUsuarioIdAndRol(Integer usuarioId, Rol rol);

    /**
     * Elimina todos los roles asignados a un usuario.
     *
     * @param usuarioId ID del usuario
     */
    void deleteByUsuarioId(Integer usuarioId);

    /**
     * Elimina una asignación específica de rol de un usuario.
     *
     * @param usuarioId ID del usuario
     * @param rol Rol a eliminar
     */
    void deleteByUsuarioIdAndRol(Integer usuarioId, Rol rol);
}
