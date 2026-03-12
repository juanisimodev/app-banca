# App Banca

Aplicacion web bancaria construida con Spring Boot + Thymeleaf. El sistema permite autenticacion por RUT, visualizacion de cuentas y movimientos, transferencias entre clientes y paneles diferenciados por rol (`USER`, `GERENTE`, `SYS_ADMIN`).

## Documentacion

- Arquitectura tecnica: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)
- Endpoints y permisos: [docs/ENDPOINTS.md](docs/ENDPOINTS.md)
- Estrategia de testing: [docs/TESTING.md](docs/TESTING.md)

## Caracteristicas principales

- Login con RUT y password usando Spring Security.
- Dashboard dinamico por rol:
- `USER`: saldos, ultimos movimientos y transferencias.
- `GERENTE`: KPIs de operacion, supervision de clientes y monitoreo de movimientos.
- `SYS_ADMIN`: creacion de usuarios y vista consolidada del sistema.
- Transferencias atomicas con `@Transactional` y doble registro de trazabilidad.
- Persistencia en PostgreSQL (Supabase) con JPA/Hibernate.

## Stack tecnologico

- Java 25
- Spring Boot 4.x
- Spring Security
- Spring Data JPA + Hibernate
- PostgreSQL (Supabase)
- Thymeleaf + Bootstrap 5
- Maven

## Arquitectura del proyecto

La aplicacion usa una estructura en capas orientada a MVC:

- `domain`: entidades JPA y enums de negocio.
- `repository`: acceso a datos con Spring Data.
- `service`: logica de negocio y transacciones.
- `controller`: controladores web MVC.
- `dto`: objetos para formularios y vistas.
- `config`: seguridad y carga de datos iniciales.

## Modelo de datos

### User
- `id`: PK autogenerada.
- `rut`: unico, usado para login.
- `nombreCompleto`.
- `password`: hash BCrypt.
- `role`: `USER`, `GERENTE`, `SYS_ADMIN`.
- Relacion 1:N con `Account`.

### Account
- `id`: PK autogenerada.
- `numeroCuenta`: unico (`RUT-A` o `RUT-C`).
- `tipo`: `AHORRO` o `CORRIENTE`.
- `saldo`: decimal (`precision=15`, `scale=2`).
- Relacion N:1 con `User`.
- Relacion 1:N con `Movement`.

### Movement
- `id`: UUID.
- `fecha`.
- `descripcion`.
- `monto`: positivo (abono) / negativo (cargo).
- `saldoResultante`.
- Relacion N:1 con `Account`.

## Reglas de negocio implementadas

- No se permiten transferencias con monto menor o igual a cero.
- No se permite saldo negativo en cuenta de origen.
- Transferencias atomicas: si falla una parte, se revierte toda la operacion.
- Cada transferencia genera dos movimientos (origen y destino).
- En panel de gerente, se marcan como riesgo alto movimientos con monto absoluto mayor o igual a `$500.000`.

## Seguridad y autorizacion

- Ruta publica: `/login` y recursos estaticos.
- Rutas `/admin/**`: solo rol `SYS_ADMIN`.
- Resto de rutas: autenticadas.
- Login personalizado:
- `usernameParameter = rut`
- `passwordParameter = password`
- Redireccion post-login: `/dashboard`

## Endpoints principales

- `GET /login`: formulario de autenticacion.
- `GET /dashboard`: dashboard segun rol.
- `GET /transfer`: formulario de transferencia (USER).
- `POST /transfer`: procesa transferencia.
- `POST /admin/users`: creacion de usuario con cuentas por defecto (SYS_ADMIN).
- `POST /logout`: cierre de sesion.

## Datos iniciales (seed)

Se crean automaticamente si no existen:

- Admin
- RUT: `1-9`
- Password: `admin123`
- Rol: `SYS_ADMIN`

- Usuario de prueba
- RUT: `12.345.678-9`
- Password: `user123`
- Rol: `USER`
- Cuenta AHORRO con `$50.000`
- Cuenta CORRIENTE con `$100.000`

## Configuracion

Archivo: `src/main/resources/application.properties`

Variables principales:

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`
- `spring.jpa.hibernate.ddl-auto=update`

Nota: para produccion, se recomienda mover credenciales a variables de entorno.

## Ejecucion local

### Requisitos

- JDK 25
- Maven Wrapper (incluido)
- Acceso a PostgreSQL/Supabase

### Comandos

```bash
./mvnw clean spring-boot:run
```

En Windows CMD:

```cmd
mvnw.cmd clean spring-boot:run
```

Aplicacion disponible en:

- `http://localhost:8080/login`

## Pruebas

Ejecutar pruebas unitarias/integracion:

```bash
./mvnw test
```

## Estructura de carpetas

```text
src/main/java/com/banca/app
|- config
|- controller
|- domain
|- dto
|- exception
|- repository
|- service

src/main/resources
|- application.properties
|- templates
```

## Mejoras sugeridas

- Auditoria avanzada de accesos.
- Filtros por fecha, monto y RUT en monitoreo de gerente.
- Paginacion de tablas en dashboard.
- Integrar validacion formal de RUT (Modulo 11).