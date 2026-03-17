# Migración: Validadores Personalizados

## 📋 Resumen

Se han migrado validadores de anotaciones personalizadas desde el proyecto `seguridad-back` al proyecto `security-backend`, siguiendo la arquitectura hexagonal establecida.

## ✅ Validadores Implementados

Se crearon los siguientes validadores personalizados en el módulo `application`:

### 1. CharactersWithoutWhiteSpaces
- **Anotación**: `@CharactersWithoutWhiteSpaces`
- **Validador**: `CharactersWithoutWhiteSpacesValidator`
- **Regex**: `^\\p{L}+$`
- **Descripción**: Valida que el campo contenga solo caracteres alfabéticos sin espacios
- **Mensaje**: "Debe contener solo caracteres sin espacios"

### 2. AlphanumericWithoutWhiteSpaces
- **Anotación**: `@AlphanumericWithoutWhiteSpaces`
- **Validador**: `AlphanumericWithoutWhiteSpacesValidator`
- **Regex**: `^[\\p{L}0-9]+$`
- **Descripción**: Valida que el campo contenga solo caracteres alfanuméricos sin espacios
- **Mensaje**: "Debe contener solo caracteres alfanuméricos sin espacios"

### 3. AlphanumericWithWhiteSpaces
- **Anotación**: `@AlphanumericWithWhiteSpaces`
- **Validador**: `AlphanumericWithWhiteSpacesValidator`
- **Regex**: `^[\\p{L}0-9]+[\\p{L}0-9\\s]*$`
- **Descripción**: Valida que el campo contenga caracteres alfanuméricos y espacios (sin comenzar con espacio)
- **Mensaje**: "Debe contener solo caracteres alfanuméricos y espacios"

### 4. NumericWithoutWhiteSpaces
- **Anotación**: `@NumericWithoutWhiteSpaces`
- **Validador**: `NumericWithoutWhiteSpacesValidator`
- **Regex**: `^[0-9]+$`
- **Descripción**: Valida que el campo contenga solo dígitos numéricos sin espacios
- **Mensaje**: "Debe contener solo números sin espacios"

### 5. NumericWithWhiteSpaces
- **Anotación**: `@NumericWithWhiteSpaces`
- **Validador**: `NumericWithWhiteSpacesValidator`
- **Regex**: `^[0-9]+[0-9\\s]*$`
- **Descripción**: Valida que el campo contenga solo dígitos numéricos y espacios (sin comenzar con espacio)
- **Mensaje**: "Debe contener solo números y espacios"

## 📁 Ubicación de los Archivos

Todos los validadores fueron creados en:
```
application/src/main/java/com/matias/application/validation/
├── CharactersWithoutWhiteSpaces.java
├── CharactersWithoutWhiteSpacesValidator.java
├── AlphanumericWithoutWhiteSpaces.java
├── AlphanumericWithoutWhiteSpacesValidator.java
├── AlphanumericWithWhiteSpaces.java
├── AlphanumericWithWhiteSpacesValidator.java
├── NumericWithoutWhiteSpaces.java
├── NumericWithoutWhiteSpacesValidator.java
├── NumericWithWhiteSpaces.java
└── NumericWithWhiteSpacesValidator.java
```

## 🏗️ Arquitectura

### Principios Aplicados

1. **Módulo Correcto**: Los validadores fueron colocados en el módulo `application` ya que:
   - Son parte de la lógica de validación de la aplicación
   - Utilizan anotaciones de Jakarta Validation (`@Constraint`, `ConstraintValidator`)
   - Siguen el enfoque pragmático de la arquitectura permitiendo el uso de Spring Framework

2. **Separación de Responsabilidades**:
   - **Anotación**: Define el contrato de validación y mensaje de error
   - **Validador**: Implementa la lógica de validación usando expresiones regulares

3. **Compatibilidad**: 
   - Utilizan `jakarta.validation` (Jakarta EE)
   - Implementan `ConstraintValidator` de Jakarta Validation
   - Usan `StringUtils.hasText()` de Spring para validar que el valor no sea null o vacío

## 💡 Uso

### Ejemplo en DTOs

```java
public class UsuarioRequest {
    
    @CharactersWithoutWhiteSpaces
    @NotBlank
    private String apellido;
    
    @AlphanumericWithoutWhiteSpaces
    @NotBlank
    private String username;
    
    @AlphanumericWithWhiteSpaces
    private String direccion;
    
    @NumericWithoutWhiteSpaces
    private String codigoPostal;
    
    // getters y setters...
}
```

### Validación Automática

Los validadores se activan automáticamente cuando se utiliza `@Valid` o `@Validated` en los endpoints del controlador:

```java
@PostMapping("/usuarios")
public ResponseEntity<UsuarioResponse> crearUsuario(
    @Valid @RequestBody UsuarioRequest request
) {
    // Si la validación falla, Spring lanza MethodArgumentNotValidException
    // que es manejada por el DefaultExceptionHandler
    return ResponseEntity.ok(usuarioService.crear(request));
}
```

## 🔍 Detalles Técnicos

### Patrones Regex Utilizados

- `\\p{L}`: Coincide con cualquier letra Unicode (soporte multiidioma)
- `[0-9]`: Coincide con dígitos numéricos
- `\\s`: Coincide con espacios en blanco
- `+`: Una o más ocurrencias
- `*`: Cero o más ocurrencias
- `^` y `$`: Inicio y fin de la cadena

### Validación de Nulos

Todos los validadores utilizan `StringUtils.hasText(value)` que retorna `true` solo si:
- El valor no es `null`
- El valor no es una cadena vacía
- El valor contiene al menos un carácter que no sea espacio en blanco

**Nota**: Si necesitas permitir valores nulos, no combines estas anotaciones con `@NotNull` o `@NotBlank`.

## ✅ Verificación

La compilación del proyecto fue exitosa:
```bash
mvn clean compile -DskipTests
# BUILD SUCCESS
```

## 📝 Notas

1. **Validadores Pre-existentes**: Ya existían en el proyecto:
   - `@CharactersWithWhiteSpaces` y `CharactersWithWhiteSpacesValidator`
   - `@Password` y `PasswordValidator`

2. **Consistencia**: Todos los validadores siguen el mismo patrón de implementación para mantener consistencia en el código.

3. **Extensibilidad**: Es fácil agregar nuevos validadores siguiendo el mismo patrón establecido.

## 🎯 Próximos Pasos

Estos validadores están listos para ser utilizados en:
- DTOs de Request (módulo `web`)
- DTOs internos (módulo `application`)
- Cualquier clase que requiera validación de entrada de datos

---

**Fecha de Migración**: 17/03/2026  
**Estado**: ✅ Completado
