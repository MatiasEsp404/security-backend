package com.matias.domain.model;

public enum Rol {
  USUARIO("USUARIO"),
  ADMINISTRADOR("ADMINISTRADOR"),
  MODERADOR("MODERADOR");

  private final String nombre;

  Rol(String nombre) {
    this.nombre = nombre;
  }

  public String getNombre() {
    return nombre;
  }
}
